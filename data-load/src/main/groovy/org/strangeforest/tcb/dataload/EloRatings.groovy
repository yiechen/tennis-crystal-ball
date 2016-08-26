package org.strangeforest.tcb.dataload

import java.time.temporal.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*

import org.strangeforest.tcb.util.*

import com.google.common.base.*
import groovy.sql.*

import static java.lang.Math.*
import static org.strangeforest.tcb.util.DateUtil.*

class EloRatings {

	final SqlPool sqlPool
	final LockManager<Integer> lockManager
	final Map<Integer, CompletableFuture> playerMatchFutures
	Map<Integer, EloRating> playerRatings
	volatile Date lastDate
	AtomicInteger saves, rankFetches
	AtomicInteger progress
	ExecutorService rankExecutor, saveExecutor
	Date saveFromDate

	static final String QUERY_MATCHES = //language=SQL
		"SELECT m.winner_id, m.loser_id, tournament_end(e.date, e.level, e.draw_size) AS end_date, e.level, m.round, m.best_of, m.outcome\n" +
		"FROM match m\n" +
		"INNER JOIN tournament_event e USING (tournament_event_id)\n" +
		"WHERE e.level IN ('G', 'F', 'M', 'O', 'A', 'B', 'D', 'T')\n" +
		"AND (m.outcome IS NULL OR m.outcome <> 'ABD')\n" +
		"ORDER BY end_date, m.round, m.winner_id, m.loser_id, m.match_num"

	static final String QUERY_LAST_DATE = //language=SQL
		"SELECT max(rank_date) AS last_date FROM player_elo_ranking"

	static final String QUERY_PLAYER_RANK = //language=SQL
		"SELECT player_rank(?, ?) AS rank"

	static final String MERGE_ELO_RANKING = //language=SQL
		"{call merge_elo_ranking(:rank_date, :player_id, :rank, :elo_rating)}"

	static final String DELETE_ALL = //language=SQL
		"DELETE FROM player_elo_ranking"

	static final int MIN_MATCHES = 10
	static final int MIN_MATCHES_PERIOD = 365
	static final int MIN_MATCHES_IN_PERIOD = 3

	static final double SAVE_RANK_THREAD_RATIO = 1.0
	static final int MATCHES_PER_DOT = 1000
	static final int SAVES_PER_PLUS = 20
	static final int PROGRESS_LINE_WRAP = 100
	static final int PLAYERS_TO_SAVE = 200

	static final def comparator = { a, b -> b <=> a }
	static final def bestComparator = { a, b -> b.bestRating <=> a.bestRating }
	static final def nullFuture = CompletableFuture.completedFuture(null)

	EloRatings(SqlPool sqlPool) {
		this.sqlPool = sqlPool
		lockManager = new LockManager<>()
		playerMatchFutures = new HashMap<>()
	}

	def compute(boolean save = false, boolean fullSave = true, Date saveFromDate = null) {
		def stopwatch = Stopwatch.createStarted()
		int matches = 0
		playerRatings = new ConcurrentHashMap<>()
		lastDate = null
		saves = new AtomicInteger()
		rankFetches = new AtomicInteger()
		progress = new AtomicInteger()
		def remainingPoolSize = sqlPool.size() - 1
		int saveThreads = save ? (SAVE_RANK_THREAD_RATIO ? remainingPoolSize * SAVE_RANK_THREAD_RATIO / (1 + SAVE_RANK_THREAD_RATIO) : remainingPoolSize) : 0
		int rankThreads = SAVE_RANK_THREAD_RATIO ? remainingPoolSize - saveThreads : 0
		if (rankThreads) {
			println "Using $rankThreads rank threads"
			rankExecutor = Executors.newFixedThreadPool(rankThreads)
		}
		if (save) {
			saveExecutor = Executors.newFixedThreadPool(saveThreads)
			println "Using $saveThreads saving threads"
			if (fullSave) {
				println 'Deleting all Elo ratings'
				deleteAll()
			}
			else
				this.saveFromDate = saveFromDate ? saveFromDate : lastDate()
		}
		println 'Processing matches'
		sqlPool.withSql { sql ->
			try {
				sql.eachRow(QUERY_MATCHES) { match ->
					def date = match.end_date
					if (lastDate && date != lastDate) {
						waitForAllMatchesToComplete()
						saveCurrentRatings()
					}
					processMatch(match)
					lastDate = date
					if (++matches % MATCHES_PER_DOT == 0)
						progressTick '.'
				}
				waitForAllMatchesToComplete()
				saveCurrentRatings()
			}
			finally {
				rankExecutor?.shutdownNow()
				saveExecutor?.shutdown()
				saveExecutor?.awaitTermination(1L, TimeUnit.DAYS)
			}
		}
		println "\nElo Ratings computed in $stopwatch"
		println "Rank fetches: $rankFetches"
		if (save)
			println "Saves: $saves"
		playerRatings
	}

	def waitForAllMatchesToComplete() {
		CompletableFuture.allOf(playerMatchFutures.values().toArray(new CompletableFuture[playerMatchFutures.size()])).join()
		playerMatchFutures.clear()
	}

	def current(int count, Date date = new Date()) {
		Date minDate = toDate(toLocalDate(date).minusYears(1))
		def i = 0
		playerRatings.values().findAll { it.matches >= MIN_MATCHES && it.lastDate >= minDate && it.getDaysSpan(date) <= MIN_MATCHES_PERIOD }
			.sort(comparator)
			.findAll { ++i <= count }
	}

	def allTime(int count) {
		def i = 0
		playerRatings.values().findAll {	it.bestRating }
			.sort(bestComparator)
			.findAll { ++i <= count }
	}

	def processMatch(match) {
		int winnerId = match.winner_id
		int loserId = match.loser_id
		def playerId1 = min(winnerId, loserId)
		def playerId2 = max(winnerId, loserId)
		lockManager.withLock(playerId1, playerId2) {
			String level = match.level
			String round = match.round
			short bestOf = match.best_of
			String outcome = match.outcome
			Date date = match.end_date
			def winnerRating = getRating(winnerId, date)
			def loserRating = getRating(loserId, date)
			boolean schedule = false;
			if (!(winnerRating && loserRating)) {
				if (rankExecutor)
					schedule = true
				else {
					winnerRating = winnerRating ?: newEloRating(winnerId)
					loserRating = loserRating ?: newEloRating(loserId)
				}
			}
			if (schedule) {
				def future = CompletableFuture.allOf(playerMatchFutures.get(playerId1) ?: nullFuture, playerMatchFutures.get(playerId2) ?: nullFuture).thenRunAsync({
					lockManager.withLock(playerId1, playerId2) {
						winnerRating = getRating(winnerId, date) ?: newEloRating(winnerId)
						loserRating = getRating(loserId, date) ?: newEloRating(loserId)
						double deltaRating = deltaRating(winnerRating, loserRating, level, round, bestOf, outcome)
						putNewRatings(winnerId, loserId, winnerRating, loserRating, deltaRating, date)
					}
				}, rankExecutor);
				playerMatchFutures.put(playerId1, future)
				playerMatchFutures.put(playerId2, future)
			}
			else {
				double deltaRating = deltaRating(winnerRating, loserRating, level, round, bestOf, outcome)
				putNewRatings(winnerId, loserId, winnerRating, loserRating, deltaRating, date)
			}
		}
	}

	private EloRating newEloRating(int playerId) {
		new EloRating(playerId, playerRank(playerId, lastDate))
	}

	private getRating(int playerId, Date date) {
		def rating = playerRatings.get(playerId)
		if (rating)
			rating.adjustRating(date)
		rating
	}

	private putNewRatings(int winnerId, int loserId, EloRating winnerRating, EloRating loserRating, double deltaRating, Date date) {
		playerRatings.put(winnerId, winnerRating.newRating(deltaRating, date))
		playerRatings.put(loserId, loserRating.newRating(-deltaRating, date))
	}

	private static double deltaRating(EloRating winnerRating, EloRating loserRating, String level, String round, short bestOf, String outcome) {
		double winnerQ = pow(10, winnerRating.rating / 400)
		double loserQ = pow(10, loserRating.rating / 400)
		double loserExpectedScore = loserQ / (winnerQ + loserQ)

		kFactor(level, round, bestOf, outcome) * loserExpectedScore
	}

	private static double kFactor(level, round, bestOf, outcome) {
		double kFactor = 100
		switch (level) {
			case 'G': break
			case 'F': kFactor *= 0.9; break
			case 'M': kFactor *= 0.8; break
			case 'A': kFactor *= 0.7; break
			default: kFactor *= 0.6; break
		}
		switch (round) {
			case 'F': break
			case 'BR': kFactor *= 0.975; break
			case 'SF': kFactor *= 0.95; break
			case 'QF': kFactor *= 0.90; break
			case 'R16': kFactor *= 0.85; break
			case 'R32': kFactor *= 0.80; break
			case 'R64': kFactor *= 0.75; break
			case 'R128': kFactor *= 0.70; break
			case 'RR': kFactor *= 0.90; break
		}
		if (bestOf < 5) kFactor *= 0.9
		if (outcome == 'W/O') kFactor *= 0.5
		kFactor
	}

	static class EloRating implements Comparable<EloRating> {

		volatile int playerId
		volatile double rating
		volatile int matches
		volatile Deque<Date> dates
		volatile EloRating bestRating

		private static final START_RATING_TABLE = [
			ratingPoint(1, 2365),
			ratingPoint(2, 2290),
			ratingPoint(3, 2235),
			ratingPoint(4, 2195),
			ratingPoint(5, 2160),
			ratingPoint(7, 2110),
			ratingPoint(10, 2060),
			ratingPoint(15, 2015),
			ratingPoint(20, 1980),
			ratingPoint(30, 1925),
			ratingPoint(50, 1840),
			ratingPoint(70, 1770),
			ratingPoint(100, 1695),
			ratingPoint(150, 1615),
			ratingPoint(200, 1555),
			ratingPoint(300, 1500)
		]
		static final int START_RATING = START_RATING_TABLE[START_RATING_TABLE.size() - 1].eloRating

		EloRating() {}

		EloRating(int playerId, Integer rank) {
			this.playerId = playerId
			rating = startRating(rank)
		}

		EloRating newRating(double delta, Date date) {
			def newRating = new EloRating(playerId: playerId, rating: rating + delta * kFunction(), matches: matches + 1, dates: new ArrayDeque<>(dates ?: []))
			newRating.bestRating = bestRating(newRating)
			newRating.addDate(date)
			newRating
		}

		Date getLastDate() {
			dates.peekLast()
		}

		Date getFirstDate() {
			dates.peekFirst()
		}

		private addDate(Date date) {
			dates.addLast(date)
			while (dates.size() > MIN_MATCHES_IN_PERIOD)
				dates.removeFirst()
		}

		long getDaysSpan(Date date) {
			ChronoUnit.DAYS.between(toLocalDate(firstDate), toLocalDate(date))
		}

		/**
		 * K-Function returns values from 1/2 to 1.
		 * For rating 0-1800 returns 1
		 * For rating 1800-2000 returns linearly decreased values from 1 to 1/2. For example, for 1900 return 3/4
		 * For rating 2000+ returns 1/2
		 * @return values from 1/2 to 1, depending on current rating
		 */
		private double kFunction() {
			if (rating <= 1800)
				1.0
			else if (rating <= 2000)
				1.0 - (rating - 1800) / 400.0
			else
				0.5
		}

		def adjustRating(Date date) {
			def lastDate = this.lastDate
			if (lastDate) {
				def daysSinceLastMatch = ChronoUnit.DAYS.between(toLocalDate(lastDate), toLocalDate(date))
				if (daysSinceLastMatch > 365)
					rating = ratingAdjusted(daysSinceLastMatch)
			}
		}

		private ratingAdjusted(long daysSinceLastMatch) {
			max(START_RATING, rating - (daysSinceLastMatch - 365) * 200 / 365)
		}

		def bestRating(EloRating newRating) {
			if (matches >= MIN_MATCHES)
				bestRating && bestRating >= newRating ? bestRating : newRating
			else
				null
		}

		private static ratingPoint(int rank, int eloRating) {
			return new RatingPoint(rank: rank, eloRating: eloRating)
		}

		static int startRating(Integer rank) {
			if (rank) {
				RatingPoint prevPoint
				for (RatingPoint point : START_RATING_TABLE) {
					if (rank == point.rank)
						return point.eloRating
					else if (rank < point.rank) {
						if (prevPoint != null)
							return prevPoint.eloRating - (prevPoint.eloRating - point.eloRating) * (rank - prevPoint.rank) / (point.rank - prevPoint.rank)
						else
							return point.eloRating
					}
					prevPoint = point
				}
			}
			START_RATING
		}

		static class RatingPoint {
			int rank;
			int eloRating;
		}

		String toString() {
			round rating
		}

		int compareTo(EloRating eloRating) {
			rating <=> eloRating.rating
		}
	}


	Date lastDate() {
		sqlPool.withSql { sql ->
			sql.firstRow(QUERY_LAST_DATE).last_date
		}
	}

	def deleteAll() {
		sqlPool.withSql { sql ->
			sql.execute(DELETE_ALL)
		}
	}

	def Integer playerRank(int playerId, Date date) {
		rankFetches.incrementAndGet()
		sqlPool.withSql { Sql sql ->
			sql.firstRow(QUERY_PLAYER_RANK, [playerId, date]).rank
		}
	}

	def saveCurrentRatings() {
		if (saveExecutor && playerRatings && (!saveFromDate || lastDate >= saveFromDate)) {
			def ratingsToSave = current(PLAYERS_TO_SAVE, lastDate).collectEntries() { [(it.playerId): it.rating] }
			def dateToSave = lastDate
			saveExecutor.execute { saveRatings(ratingsToSave, dateToSave) }
		}
	}

	def saveRatings(Map<Integer, Double> ratings, Date date) {
		sqlPool.withSql { sql ->
			sql.withBatch(MERGE_ELO_RANKING) { ps ->
				def i = 0
				ratings.each { it ->
					Map params = [:]
					params.rank_date = new java.sql.Date(date.time)
					params.player_id = it.key
					params.rank = ++i
					params.elo_rating = (int)round(it.value)
					ps.addBatch(params)
				}
			}
		}
		if (saves.incrementAndGet() % SAVES_PER_PLUS == 0)
			progressTick '+'
	}

	private progressTick(tick) {
		print tick
		if (progress.incrementAndGet() % PROGRESS_LINE_WRAP == 0)
			println()
	}
}

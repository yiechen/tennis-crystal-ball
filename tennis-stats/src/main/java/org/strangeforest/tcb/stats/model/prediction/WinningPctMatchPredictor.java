package org.strangeforest.tcb.stats.model.prediction;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.stats.model.core.*;

import com.google.common.collect.*;

import static java.lang.Math.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.prediction.MatchDataUtil.*;
import static org.strangeforest.tcb.stats.model.prediction.WinningPctPredictionItem.*;

public class WinningPctMatchPredictor implements MatchPredictor {

	private final List<MatchData> matchData1;
	private final List<MatchData> matchData2;
	private final Range<Integer> rankRange1;
	private final Range<Integer> rankRange2;
	private final PlayerData playerData1;
	private final PlayerData playerData2;
	private final LocalDate date1;
	private final LocalDate date2;
	private final Surface surface;
	private final TournamentLevel level;
	private final Round round;
	private final Integer tournamentId;
	private final short bestOf;

	private static final int MATCH_RECENT_PERIOD_YEARS = 2;
	private static final int SET_RECENT_PERIOD_YEARS = 1;

	public WinningPctMatchPredictor(List<MatchData> matchData1, List<MatchData> matchData2, RankingData rankingData1, RankingData rankingData2, PlayerData playerData1, PlayerData playerData2,
	                                LocalDate date1, LocalDate date2, Surface surface, TournamentLevel level, Round round, Integer tournamentId, short bestOf) {
		this.matchData1 = matchData1;
		this.matchData2 = matchData2;
		this.rankRange1 = rankRange(rankingData1.getRank());
		this.rankRange2 = rankRange(rankingData2.getRank());
		this.playerData1 = playerData1;
		this.playerData2 = playerData2;
		this.date1 = date1;
		this.date2 = date2;
		this.surface = surface;
		this.level = level;
		this.round = round;
		this.tournamentId = tournamentId;
		this.bestOf = bestOf;
	}

	@Override public PredictionArea getArea() {
		return PredictionArea.WINNING_PCT;
	}

	@Override public MatchPrediction predictMatch() {
		Period matchRecentPeriod = getMatchRecentPeriod();
		Period setRecentPeriod = getSetRecentPeriod();
		MatchPrediction prediction = new MatchPrediction();
		addItemProbabilities(prediction, OVERALL, ALWAYS_TRUE);
		addItemProbabilities(prediction, SURFACE, isSurface(surface));
		addItemProbabilities(prediction, LEVEL, isLevel(level));
		addItemProbabilities(prediction, TOURNAMENT, isTournament(tournamentId));
		addItemProbabilities(prediction, ROUND, isRound(round));
		addItemProbabilities(prediction, RECENT, isRecent(date1, matchRecentPeriod), isRecent(date2, matchRecentPeriod));
		addItemProbabilities(prediction, SURFACE_RECENT, isSurface(surface).and(isRecent(date1, matchRecentPeriod)), isSurface(surface).and(isRecent(date2, matchRecentPeriod)));
		addItemProbabilities(prediction, LEVEL_RECENT, isLevel(level).and(isRecent(date1, matchRecentPeriod)), isLevel(level).and(isRecent(date2, matchRecentPeriod)));
		addItemProbabilities(prediction, ROUND_RECENT, isRound(round).and(isRecent(date1, matchRecentPeriod)), isRound(round).and(isRecent(date2, matchRecentPeriod)));
		addItemProbabilities(prediction, VS_RANK, isOpponentRankInRange(rankRange2), isOpponentRankInRange(rankRange1));
		addItemProbabilities(prediction, VS_HAND, isOpponentHand(playerData2.getHand()), isOpponentHand(playerData1.getHand()));
		addItemProbabilities(prediction, VS_BACKHAND, isOpponentBackhand(playerData2.getBackhand()), isOpponentBackhand(playerData1.getBackhand()));
		addItemProbabilities(prediction, OVERALL_SET, ALWAYS_TRUE);
		addItemProbabilities(prediction, SURFACE_SET, isSurface(surface));
		addItemProbabilities(prediction, LEVEL_SET, isLevel(level));
		addItemProbabilities(prediction, TOURNAMENT_SET, isTournament(tournamentId));
		addItemProbabilities(prediction, ROUND_SET, isRound(round));
		addItemProbabilities(prediction, RECENT_SET, isRecent(date1, setRecentPeriod), isRecent(date2, setRecentPeriod));
		addItemProbabilities(prediction, SURFACE_RECENT_SET, isSurface(surface).and(isRecent(date1, setRecentPeriod)), isSurface(surface).and(isRecent(date2, setRecentPeriod)));
		addItemProbabilities(prediction, LEVEL_RECENT_SET, isLevel(level).and(isRecent(date1, setRecentPeriod)), isLevel(level).and(isRecent(date2, setRecentPeriod)));
		addItemProbabilities(prediction, ROUND_RECENT_SET, isRound(round).and(isRecent(date1, setRecentPeriod)), isRound(round).and(isRecent(date2, setRecentPeriod)));
		addItemProbabilities(prediction, VS_RANK_SET, isOpponentRankInRange(rankRange2), isOpponentRankInRange(rankRange1));
		addItemProbabilities(prediction, VS_HAND_SET, isOpponentHand(playerData2.getHand()), isOpponentHand(playerData1.getHand()));
		addItemProbabilities(prediction, VS_BACKHAND_SET, isOpponentBackhand(playerData2.getBackhand()), isOpponentBackhand(playerData1.getBackhand()));
		return prediction;
	}

	private Period getMatchRecentPeriod() {
		return Period.ofYears(PredictionConfig.getIntegerProperty("recent_period.match." + getArea()).orElse(MATCH_RECENT_PERIOD_YEARS));
	}

	private Period getSetRecentPeriod() {
		return Period.ofYears(PredictionConfig.getIntegerProperty("recent_period.set." + getArea()).orElse(SET_RECENT_PERIOD_YEARS));
	}

	private void addItemProbabilities(MatchPrediction prediction, WinningPctPredictionItem item, Predicate<MatchData> filter) {
		addItemProbabilities(prediction, item, filter, filter);
	}

	private void addItemProbabilities(MatchPrediction prediction, WinningPctPredictionItem item, Predicate<MatchData> filter1, Predicate<MatchData> filter2) {
		if (item.getWeight() > 0.0) {
			ToIntFunction<MatchData> wonDimension = item.isForSet() ? MatchData::getPSets : MatchData::getPMatches;
			ToIntFunction<MatchData> lostDimension = item.isForSet() ? MatchData::getOSets : MatchData::getOMatches;
			List<MatchData> filteredMatchData1 = matchData1.stream().filter(filter1).collect(toList());
			List<MatchData> filteredMatchData2 = matchData2.stream().filter(filter2).collect(toList());
			int won1 = filteredMatchData1.stream().mapToInt(wonDimension).sum();
			int lost1 = filteredMatchData1.stream().mapToInt(lostDimension).sum();
			int won2 = filteredMatchData2.stream().mapToInt(wonDimension).sum();
			int lost2 = filteredMatchData2.stream().mapToInt(lostDimension).sum();
			int total1 = won1 + lost1;
			int total2 = won2 + lost2;
			if (total1 > 0 && total2 > 0) {
				double weight = item.getWeight() * weight(total1, total2);
				double p1 = 1.0 * won1 / total1;
				double p2 = 1.0 * won2 / total2;
				if (p1 + p2 > 0.0) {
					p1 = pow(2, p1) - 1;
					p2 = pow(2, p2) - 1;
					double p12 = p1 + p2;
					DoubleUnaryOperator probabilityTransformer = probabilityTransformer(item.isForSet(), bestOf);
					prediction.addItemProbability1(item, weight, probabilityTransformer.applyAsDouble(p1 / p12));
					prediction.addItemProbability2(item, weight, probabilityTransformer.applyAsDouble(p2 / p12));
				}
			}
		}
	}
}

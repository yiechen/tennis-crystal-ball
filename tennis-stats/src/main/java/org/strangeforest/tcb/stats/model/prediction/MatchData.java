package org.strangeforest.tcb.stats.model.prediction;

import java.time.*;

import org.strangeforest.tcb.stats.model.core.*;

public final class MatchData {

	private final LocalDate date;
	private final int tournamentId;
	private final int tournamentEventId;
	private final boolean inProgress;
	private final String level;
	private final String surface;
	private final String round;
	private final int opponentId;
	private final Integer opponentRank;
	private final Integer opponentEloRating;
	private final String opponentHand;
	private final String opponentBackhand;
	private final String opponentEntry;
	private final int pMatches;
	private final int oMatches;
	private final int pSets;
	private final int oSets;
	private final Integer nextEloRating;

	public MatchData(LocalDate date, int tournamentId, int tournamentEventId, boolean inProgress, String level, String surface, String round,
	                 int opponentId, Integer opponentRank, Integer opponentEloRating, String opponentHand, String opponentBackhand, String opponentEntry,
	                 int pMatches, int oMatches, int pSets, int oSets, Integer nextEloRating) {
		this.date = date;
		this.tournamentId = tournamentId;
		this.tournamentEventId = tournamentEventId;
		this.inProgress = inProgress;
		this.level = level;
		this.surface = surface;
		this.round = round;
		this.opponentId = opponentId;
		this.opponentRank = opponentRank;
		this.opponentEloRating = opponentEloRating;
		this.opponentHand = opponentHand;
		this.opponentBackhand = opponentBackhand;
		this.opponentEntry = opponentEntry;
		this.pMatches = pMatches;
		this.oMatches = oMatches;
		this.pSets = pSets;
		this.oSets = oSets;
		this.nextEloRating = nextEloRating;
	}

	public LocalDate getDate() {
		return date;
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public int getTournamentEventId() {
		return tournamentEventId;
	}

	public boolean isInProgress() {
		return inProgress;
	}

	public String getLevel() {
		return level;
	}

	public String getSurface() {
		return surface;
	}

	public String getRound() {
		return round;
	}

	public int getOpponentId() {
		return opponentId;
	}

	public Integer getOpponentRank() {
		return opponentRank;
	}

	public Integer getOpponentEloRating() {
		return opponentEloRating;
	}

	public double getOpponentEloScore() {
		if (opponentEloRating != null) {
			int ratingDelta = opponentEloRating - Player.START_ELO_RATING;
			if (ratingDelta > 0) {
				if (oMatches > pMatches)
					ratingDelta = -ratingDelta;
				return ratingDelta / 400.0;
			}
		}
		return 0.0;
	}

	public String getOpponentHand() {
		return opponentHand;
	}

	public String getOpponentBackhand() {
		return opponentBackhand;
	}

	public String getOpponentEntry() {
		return opponentEntry;
	}

	public int getPMatches() {
		return pMatches;
	}

	public int getOMatches() {
		return oMatches;
	}

	public int getMatches() {
		return pMatches + oMatches;
	}

	public int getPSets() {
		return pSets;
	}

	public int getOSets() {
		return oSets;
	}

	public int getSets() {
		return pSets + oSets;
	}

	public Integer getNextEloRating() {
		return nextEloRating;
	}
}

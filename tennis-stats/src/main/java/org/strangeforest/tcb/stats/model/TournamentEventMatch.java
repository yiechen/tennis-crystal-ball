package org.strangeforest.tcb.stats.model;

import java.util.*;

public class TournamentEventMatch {

	private final long id;
	private final String round;
	private final MatchPlayerEx winner;
	private final MatchPlayerEx loser;
	private final List<SetScore> score;
	private final String outcome;

	public TournamentEventMatch(long id, String round, MatchPlayerEx winner, MatchPlayerEx loser, List<SetScore> score, String outcome) {
		this.id = id;
		this.round = round;
		this.winner = winner;
		this.loser = loser;
		this.score = score;
		this.outcome = outcome;
	}

	public long getId() {
		return id;
	}

	public String getRound() {
		return round;
	}

	public MatchPlayerEx getWinner() {
		return winner;
	}

	public MatchPlayerEx getLoser() {
		return loser;
	}

	public List<SetScore> getScore() {
		return score;
	}

	public String getOutcome() {
		return outcome;
	}
}

package org.strangeforest.tcb.stats.service;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;

public enum Opponent {

	NO_1(matchesRankCriterion(1), statsRankCriterion(1), false),
	TOP_5(matchesRankCriterion(5), statsRankCriterion(5), false),
	TOP_10(matchesRankCriterion(10), statsRankCriterion(10), false),
	TOP_20(matchesRankCriterion(20), statsRankCriterion(20), false),
	TOP_50(matchesRankCriterion(50), statsRankCriterion(50), false),
	TOP_100(matchesRankCriterion(100), statsRankCriterion(100), false),
	RIGHT_HANDED(matchesHandCriterion("R"), statsHandCriterion("R"), true),
	LEFT_HANDED(matchesHandCriterion("L"), statsHandCriterion("L"), true),
	BACKHAND_2(matchesBackhandCriterion("2"), statsBackhandCriterion("2"), true),
	BACKHAND_1(matchesBackhandCriterion("1"), statsBackhandCriterion("1"), true),
	SEEDED(matchesSeedCriterion("IS NOT NULL"), statsSeedCriterion("IS NOT NULL"), false),
	UNSEEDED(matchesSeedCriterion("IS NULL"), statsSeedCriterion("IS NULL"), false),
	QUALIFIER(matchesEntryCriterion("Q"), statsEntryCriterion("Q"), false),
	WILD_CARD(matchesEntryCriterion("WC"), statsEntryCriterion("WC"), false),
	LUCKY_LOSER(matchesEntryCriterion("LL"), statsEntryCriterion("LL"), false);

	public static Opponent forValue(String opponent) {
		return !isNullOrEmpty(opponent) ? Opponent.valueOf(opponent) : null;
	}

	private final String matchesCriterion;
	private final String statsCriterion;
	private final boolean opponentRequired;

	private static final String MATCHES_RANK_CRITERION = " AND ((m.winner_rank <= %1$d AND m.winner_id <> ?) OR (m.loser_rank <= %1$d AND m.loser_id <> ?))";
	private static final String MATCHES_SEED_CRITERION = " AND ((m.winner_seed %1$s AND m.winner_id <> ?) OR (m.loser_seed %1$s AND m.loser_id <> ?))";
	private static final String MATCHES_ENTRY_CRITERION = " AND ((m.winner_entry = '%1$s'::tournament_entry AND m.winner_id <> ?) OR (m.loser_entry = '%1$s'::tournament_entry AND m.loser_id <> ?))";
	private static final String MATCHES_HAND_CRITERION = " AND ((pw.hand = '%1$s' AND m.winner_id <> ?) OR (pl.hand = '%1$s' AND m.loser_id <> ?))";
	private static final String MATCHES_BACKHAND_CRITERION = " AND ((pw.backhand = '%1$s' AND m.winner_id <> ?) OR (pl.backhand = '%1$s' AND m.loser_id <> ?))";

	private static final String STATS_RANK_CRITERION = " AND opponent_rank <= %1$d";
	private static final String STATS_SEED_CRITERION = " AND opponent_seed %1$s";
	private static final String STATS_ENTRY_CRITERION = " AND opponent_entry = '%1$s'::tournament_entry";
	private static final String STATS_HAND_CRITERION = " AND o.hand = '%1$s'";
	private static final String STATS_BACKHAND_CRITERION = " AND o.backhand = '%1$s'";

	Opponent(String matchesCriterion, String statsCriterion, boolean opponentRequired) {
		this.matchesCriterion = matchesCriterion;
		this.statsCriterion = statsCriterion;
		this.opponentRequired = opponentRequired;
	}

	public String getMatchesCriterion() {
		return matchesCriterion;
	}

	public String getStatsCriterion() {
		return statsCriterion;
	}

	public boolean isOpponentRequired() {
		return opponentRequired;
	}

	private static String matchesRankCriterion(int rank) {
		return format(MATCHES_RANK_CRITERION, rank);
	}

	private static String matchesSeedCriterion(String seedExpression) {
		return format(MATCHES_SEED_CRITERION, seedExpression);
	}

	private static String matchesEntryCriterion(String entry) {
		return format(MATCHES_ENTRY_CRITERION, entry);
	}

	private static String matchesHandCriterion(String hand) {
		return format(MATCHES_HAND_CRITERION, hand);
	}

	private static String matchesBackhandCriterion(String backhand) {
		return format(MATCHES_BACKHAND_CRITERION, backhand);
	}

	private static String statsRankCriterion(int rank) {
		return format(STATS_RANK_CRITERION, rank);
	}

	private static String statsSeedCriterion(String seedExpression) {
		return format(STATS_SEED_CRITERION, seedExpression);
	}

	private static String statsEntryCriterion(String entry) {
		return format(STATS_ENTRY_CRITERION, entry);
	}

	private static String statsHandCriterion(String hand) {
		return format(STATS_HAND_CRITERION, hand);
	}

	private static String statsBackhandCriterion(String backhand) {
		return format(STATS_BACKHAND_CRITERION, backhand);
	}
}
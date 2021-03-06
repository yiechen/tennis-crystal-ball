package org.strangeforest.tcb.stats.model;

import java.time.*;
import java.util.*;

import org.strangeforest.tcb.stats.model.core.*;

import com.google.common.collect.*;

import static org.strangeforest.tcb.stats.model.core.Surface.*;

public class DominanceSeason {

	private static final double DOMINANCE_RATIO_COEFFICIENT = 1500.0;
	private static final Map<Surface, Double> SURFACE_DOMINANCE_RATIO_COEFFICIENT = ImmutableMap.<Surface, Double>builder()
		.put(HARD,   400.0)
		.put(CLAY,   130.0)
		.put(GRASS,   60.0)
		.put(CARPET, 110.0)
	.build();

	public static double getDominanceRatioCoefficient(Surface surface) {
		return surface == null ? DOMINANCE_RATIO_COEFFICIENT : SURFACE_DOMINANCE_RATIO_COEFFICIENT.get(surface);
	}

	private final int season;
	private final Surface surface;
	private final boolean eligibleForEra;
	private final boolean ongoing;
	private int dominanceRatioPoints;
	private PlayerDominanceTimeline bestPlayer;
	private PlayerDominanceTimeline eraPlayer;
	private int bestPlayerPoints;
	private Map<Integer, Integer> averageEloRatings;

	public DominanceSeason(int season, Surface surface) {
		this.season = season;
		this.surface = surface;
		LocalDate today = LocalDate.now();
		int year = today.getYear();
		ongoing = season == year && today.getMonth().compareTo(Month.NOVEMBER) < 0;
		eligibleForEra = season < year || !ongoing;
		averageEloRatings = new HashMap<>();
	}

	public int getSeason() {
		return season;
	}

	public boolean isEligibleForEra() {
		return eligibleForEra;
	}

	public boolean isOngoingSeason() {
		return ongoing;
	}

	public double getDominanceRatio() {
		return dominanceRatioPoints / getDominanceRatioCoefficient(surface);
	}

	public int getDominanceRatioRounded() {
		return DominanceTimeline.roundDominanceRatio(getDominanceRatio());
	}

	public PlayerDominanceTimeline getBestPlayer() {
		return bestPlayer;
	}

	PlayerDominanceTimeline getEraPlayer() {
		return eraPlayer;
	}

	void setEraPlayer(PlayerDominanceTimeline eraPlayer) {
		this.eraPlayer = eraPlayer;
	}

	Map<Integer, Integer> getAverageEloRatings() {
		return averageEloRatings;
	}

	void setAverageEloRatings(Map<Integer, Integer> averageEloRatings) {
		this.averageEloRatings = averageEloRatings;
	}

	public int getAverageEloRating(int topN) {
		return averageEloRatings.get(topN);
	}

	public int getAverageEloRatingPoints(int topN) {
		return 10 * ((getAverageEloRating(topN) - 1700) / 100);
	}

	public void addAverageEloRating(int topN, int eloRating) {
		averageEloRatings.put(topN, eloRating);
	}

	void processPlayer(PlayerDominanceTimeline player) {
		SeasonPoints seasonPoints = player.getSeasonPoints(season);
		if (seasonPoints != null) {
			int playerPoints = seasonPoints.getPoints();
			dominanceRatioPoints += playerPoints * player.getGoatPoints();
			if (bestPlayer == null || playerPoints > bestPlayerPoints || (playerPoints == bestPlayerPoints && player.getGoatPoints() > bestPlayer.getGoatPoints())) {
				bestPlayer = player;
				bestPlayerPoints = playerPoints;
			}
		}
	}
}

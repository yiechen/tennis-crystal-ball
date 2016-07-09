package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;

import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.RecordFilter.*;
import static org.strangeforest.tcb.stats.model.records.categories.YoungestOldestTournamentResultCategory.RecordType.*;
import static org.strangeforest.tcb.stats.model.records.categories.YoungestOldestTournamentResultCategory.ResultType.*;

public class YoungestOldestTournamentResultCategory extends RecordCategory {

	enum RecordType {
		YOUNGEST("Youngest", "r.age"),
		OLDEST("Oldest", "r.age DESC");

		final String name;
		final String order;

		RecordType(String name, String order) {
			this.name = name;
			this.order = order;
		}
	}

	enum ResultType {
		CHAMPION("Champion", TITLES),
		FINALIST("Finalist", FINALS);

		final String name;
		final String condition;

		ResultType(String name, String condition) {
			this.name = name;
			this.condition = condition;
		}
	}

	private static final String AGE_WIDTH =        "130";
	private static final String TOURNAMENT_WIDTH = "100";
	private static final String SEASON_WIDTH =      "60";

	public YoungestOldestTournamentResultCategory() {
		super("Youngest/Oldest Tournament Champion/Finalist");
		registerAgeTournamentResults(YOUNGEST, CHAMPION);
		registerAgeTournamentResults(OLDEST, CHAMPION);
		registerAgeTournamentResults(YOUNGEST, FINALIST);
		registerAgeTournamentResults(OLDEST, FINALIST);
	}

	private void registerAgeTournamentResults(RecordType recordType, ResultType resultType) {
		register(ageTournamentResult(recordType, resultType, TOURNAMENT, TOURNAMENT, ALL_TOURNAMENTS));
		register(ageTournamentResult(recordType, resultType, GRAND_SLAM));
		register(ageTournamentResult(recordType, resultType, TOUR_FINALS));
		register(ageTournamentResult(recordType, resultType, MASTERS));
		register(ageTournamentResult(recordType, resultType, OLYMPICS));
		register(ageTournamentResult(recordType, resultType, ATP_500));
		register(ageTournamentResult(recordType, resultType, ATP_250));
		register(ageTournamentResult(recordType, resultType, HARD));
		register(ageTournamentResult(recordType, resultType, CLAY));
		register(ageTournamentResult(recordType, resultType, GRASS));
		register(ageTournamentResult(recordType, resultType, CARPET));
	}

	private static Record ageTournamentResult(RecordType type, ResultType resultType, RecordFilter filter) {
		return ageTournamentResult(type, resultType, filter.id, filter.name, filter.condition);
	}

	private static Record ageTournamentResult(RecordType type, ResultType resultType, String id, String name, String condition) {
		return new Record(
			type.name + id + resultType.name, suffix(type.name, " ") + suffix(name, " ") + resultType.name,
			/* language=SQL */
			"SELECT player_id, tournament_event_id, e.name AS tournament, e.level, e.season, e.date, age(e.date, p.dob) AS age\n" +
			"FROM player_tournament_event_result r INNER JOIN player p USING (player_id) INNER JOIN tournament_event e USING (tournament_event_id)\n" +
			"WHERE p.dob IS NOT NULL AND r." + resultType.condition + prefix(condition, " AND e."),
			"r.age, r.tournament_event_id, r.tournament, r.level, r.season", type.order, type.order + ", r.date", RecordDetailFactory.TOURNAMENT_EVENT_AGE,
			asList(
				new RecordColumn("age", null, null, AGE_WIDTH, "left", "Age"),
				new RecordColumn("season", "numeric", null, SEASON_WIDTH, "center", "Season"),
				new RecordColumn("tournament", null, "tournamentEvent", TOURNAMENT_WIDTH, "left", "Tournament")
			)
		);
	}
}

package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.util.*;

import com.google.common.collect.*;

import static org.strangeforest.tcb.stats.model.core.TournamentLevel.*;
import static org.strangeforest.tcb.stats.controller.ParamsUtil.*;

@Controller
public class TimelinesController extends PageController {

	@Autowired private DominanceTimelineService timelineService;
	@Autowired private TournamentLevelService tournamentLevelService;
	@Autowired private RankingsService rankingsService;
	@Autowired private SurfaceService surfaceService;

	@GetMapping("dominanceTimeline")
	public ModelAndView dominanceTimeline(
		@RequestParam(name = "fromSeason", required = false) Integer fromSeason,
		@RequestParam(name = "toSeason", required = false) Integer toSeason,
		@RequestParam(name = "surface", required = false) String surface,
		@RequestParam(name = "averageElo", defaultValue = F) boolean averageElo
	) {
		Range<Integer> seasonRange = RangeUtil.toRange(fromSeason, toSeason);
		Surface aSurface = Surface.safeDecode(surface);
		DominanceTimeline timeline = timelineService.getDominanceTimeline(aSurface).filterSeasons(seasonRange);
		int minGOATPoints = timelineService.getMinGOATPoints(aSurface);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("seasons", dataService.getSeasons());
		modelMap.addAttribute("surfaces", Surface.values());
		modelMap.addAttribute("fromSeason", seasonRange.hasLowerBound() ? seasonRange.lowerEndpoint() : null);
		modelMap.addAttribute("toSeason", seasonRange.hasUpperBound() ? seasonRange.upperEndpoint() : null);
		modelMap.addAttribute("surface", surface);
		modelMap.addAttribute("averageElo", averageElo);
		modelMap.addAttribute("timeline", timeline);
		modelMap.addAttribute("minGOATPoints", minGOATPoints);
		modelMap.addAttribute("dominanceRatioCoefficient", (int)DominanceSeason.getDominanceRatioCoefficient(aSurface));
		return new ModelAndView("dominanceTimeline", modelMap);
	}

	@GetMapping("/tournamentLevelTimeline")
	public ModelAndView tournamentLevelTimeline(
      @RequestParam(name = "level") String level
	) {
		TournamentLevel tournamentLevel = TournamentLevel.decode(level);
		TournamentLevelTimeline timeline = tournamentLevelService.getTournamentLevelTimeline(level, tournamentLevel != MASTERS);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("level", tournamentLevel);
		modelMap.addAttribute("timeline", timeline);
		return new ModelAndView("tournamentLevelTimeline", modelMap);
	}

	@GetMapping("/tournamentLevelGroupTimeline")
	public ModelAndView tournamentLevelGroupTimeline(
      @RequestParam(name = "level") String level
	) {
		TournamentLevelGroup tournamentLevelGroup = TournamentLevelGroup.decode(level);
		TournamentLevel tournamentLevel = tournamentLevelGroup.getLevels().iterator().next();
		TournamentLevelTimeline timeline = tournamentLevelService.getTournamentLevelGroupTimeline(tournamentLevelGroup, tournamentLevel != MASTERS);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("level", tournamentLevel);
		modelMap.addAttribute("levelGroup", tournamentLevelGroup);
		modelMap.addAttribute("timeline", timeline);
		return new ModelAndView("tournamentLevelTimeline", modelMap);
	}

	@GetMapping("/teamTournamentLevelTimeline")
	public ModelAndView teamTournamentLevelTimeline(
      @RequestParam(name = "level") String level
	) {
		TournamentLevel tournamentLevel = TournamentLevel.decode(level);
		List<TeamTournamentLevelTimelineItem> timeline = tournamentLevelService.getTeamTournamentLevelTimeline(level);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("level", tournamentLevel);
		modelMap.addAttribute("timeline", timeline);
		return new ModelAndView("teamTournamentLevelTimeline", modelMap);
	}

	@GetMapping("/topRankingsTimeline")
	public ModelAndView topRankingsTimeline(
		@RequestParam(name = "rankType", defaultValue = "POINTS") RankType rankType
	) {
		TopRankingsTimeline timeline = rankingsService.getTopRankingsTimeline(rankType);

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("timeline", timeline);
		modelMap.addAttribute("rankType", rankType);
		modelMap.addAttribute("surfaces", Surface.values());
		return new ModelAndView("topRankingsTimeline", modelMap);
	}

	@GetMapping("/surfaceTimeline")
	public ModelAndView surfaceTimeline(
		@RequestParam(name = "indoor", defaultValue = "false") boolean indoor
	) {
		List<SurfaceTimelineItem> timeline = surfaceService.getSurfaceTimeline();

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("indoor", indoor);
		modelMap.addAttribute("timeline", timeline);
		return new ModelAndView("surfaceTimeline", modelMap);
	}
}

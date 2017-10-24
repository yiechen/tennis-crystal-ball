 package org.strangeforest.tcb.stats.controller;

 import org.springframework.beans.factory.annotation.*;
 import org.springframework.stereotype.*;
 import org.springframework.ui.*;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.*;
 import org.strangeforest.tcb.stats.service.*;

@Controller
public class GOATLegendController extends BaseController {

	@Autowired private GOATLegendService goatLegendService;

	@GetMapping("/goatLegend")
	public ModelAndView goatLegend(
		@RequestParam(name = "forSeason", required = false) boolean forSeason
	) {
		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("forSeason", forSeason);
		// Tournament
		modelMap.addAttribute("tournamentGOATPoints", goatLegendService.getTournamentGOATPoints());
		// Ranking
		modelMap.addAttribute("yearEndRankGOATPoints", goatLegendService.getYearEndRankGOATPoints());
		modelMap.addAttribute("bestRankGOATPoints", goatLegendService.getBestRankGOATPoints());
		modelMap.addAttribute("weeksAtNo1ForGOATPoint", goatLegendService.getWeeksAtNo1ForGOATPoint());
		modelMap.addAttribute("weeksAtEloTopNForGOATPoint", goatLegendService.getWeeksAtEloTopNGOATPoint());
		modelMap.addAttribute("bestEloRatingGOATPoints", goatLegendService.getBestEloRatingGOATPoints());
		modelMap.addAttribute("bestSurfaceEloRatingGOATPoints", goatLegendService.getBestSurfaceEloRatingGOATPoints());
		// Achievements
		modelMap.addAttribute("careerGrandSlamGOATPoints", goatLegendService.getCareerGrandSlamGOATPoints());
		modelMap.addAttribute("seasonGrandSlamGOATPoints", goatLegendService.getSeasonGrandSlamGOATPoints());
		modelMap.addAttribute("grandSlamHolderGOATPoints", goatLegendService.getGrandSlamHolderGOATPoints());
		modelMap.addAttribute("consecutiveGrandSlamOnSameEventGOATPoints", goatLegendService.getConsecutiveGrandSlamOnSameEventGOATPoints());
		modelMap.addAttribute("grandSlamOnSameEventGOATPointsDivider", (int)(1.0 / goatLegendService.getGrandSlamOnSameEventGOATPoints()));
		modelMap.addAttribute("bigWinMatchFactors", goatLegendService.getBigWinMatchFactors());
		modelMap.addAttribute("bigWinRankFactors", goatLegendService.getBigWinRankFactors());
		modelMap.addAttribute("h2hRankFactors", goatLegendService.getH2hRankFactors());
		modelMap.addAttribute("bestSeasonGOATPoints", goatLegendService.getBestSeasonGOATPoints());
		modelMap.addAttribute("greatestRivalriesGOATPoints", goatLegendService.getGreatestRivalriesGOATPoints());
		return new ModelAndView("goatLegend", modelMap);
	}

	@GetMapping("/recordsGOATPointsLegend")
	public ModelAndView recordsGOATPointsLegend() {
		return new ModelAndView("recordsGOATPointsLegend", "recordsGOATPoints", goatLegendService.getRecordsGOATPoints());
	}

	@GetMapping("/performanceGOATPointsLegend")
	public ModelAndView performanceGOATPointsLegend() {
		return new ModelAndView("performanceGOATPointsLegend", "performanceGOATPoints", goatLegendService.getPerformanceGOATPoints());
	}

	@GetMapping("/statisticsGOATPointsLegend")
	public ModelAndView statisticsGOATPointsLegend() {
		return new ModelAndView("statisticsGOATPointsLegend", "statisticsGOATPoints", goatLegendService.getStatisticsGOATPoints());
	}
}
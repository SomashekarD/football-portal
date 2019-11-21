package com.som.footBallPortal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.som.footBallPortal.services.StandingsService;

@RestController
@RequestMapping("/")
@Validated
public class FootBallController {
	private static final Logger logger = LogManager.getLogger(FootBallController.class);

	@Autowired
	private StandingsService service;

	@RequestMapping(value = "/standings", method = RequestMethod.GET)
	public @ResponseBody String get(@RequestParam(required = true) String countryName,
			@RequestParam(required = true) String leagueName, @RequestParam(required = true) String teamName) {

		try {
			return service.getTeamStandings(countryName, leagueName, teamName);
		} catch (Exception e) {
			logger.error("Error while fetching data from service", e);
			return "{ 'error': '" + e.getMessage() + "'}";
		}
	}

}

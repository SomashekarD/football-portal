package com.som.footBallPortal.services;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.som.footBallPortal.model.Standings;

@Component
public class StandingsService {

	private DefaultHttpClient httpclient;
	private Standings standings;
	private ObjectMapper mapper;

	@PostConstruct
	void init() {
		httpclient = new DefaultHttpClient();
		standings = new Standings();
		mapper = new ObjectMapper();
	}

	public String getTeamStandings(String countryName, String leagueName, String teamName) throws Exception {

		String response = "";
		try {
			HttpHost target = getTarget();

			HttpGet getRequest = new HttpGet(
					"/?action=get_standings&APIkey=9bb66184e0c8145384fd2cc0f7b914ada57b4e8fd2e4d6d586adcc27c257a978&league_id="
							+ getLeagueId(leagueName, countryName));

			String output = excecuteGetRequestAndGetJSONString(target, getRequest);

			JSONObject jsonObj = getIdValue(teamName, "team_id", "team_name", output);

			if (jsonObj == null)
				new Exception("No matching league and team in country found");

			standings.setTeamId((String) jsonObj.get("team_id"));
			standings.setTeamName(teamName);
			standings.setOverAllLeaguePosition((String) jsonObj.get("overall_league_position"));

			response = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(standings);

		} catch (Exception e) {
			throw e;
		}

		return response;
	}

	private String excecuteGetRequestAndGetJSONString(HttpHost target, HttpGet getRequest)
			throws IOException, ClientProtocolException {
		HttpResponse httpResponse = httpclient.execute(target, getRequest);

		checkHTTPStatusForSuccess(httpResponse);

		String output = getJSONString(httpResponse);
		return output;
	}

	private String getLeagueId(String leagueName, String countryName) throws Exception {
		String result = "";
		try {
			HttpHost target = getTarget();

			HttpGet getRequest = new HttpGet(
					"/?action=get_leagues&APIkey=9bb66184e0c8145384fd2cc0f7b914ada57b4e8fd2e4d6d586adcc27c257a978&country_id="
							+ getCountryId(countryName));

			String output = excecuteGetRequestAndGetJSONString(target, getRequest);

			result = (String) getIdValue(leagueName, "league_id", "league_name", output).get("league_id");

			if (result == null || result.isEmpty())
				new Exception("No matching league in country found");

			standings.setLeagueId(result);
			standings.setLeagueName(leagueName);

		} catch (Exception e) {
			throw e;
		}

		return result;
	}

	private String getCountryId(String countryName) throws Exception {
		String result = "";
		try {
			HttpHost target = getTarget();

			HttpGet getRequest = new HttpGet(
					"/?action=get_countries&APIkey=9bb66184e0c8145384fd2cc0f7b914ada57b4e8fd2e4d6d586adcc27c257a978");

			String output = excecuteGetRequestAndGetJSONString(target, getRequest);

			result = (String) getIdValue(countryName, "country_id", "country_name", output).get("country_id");

			if (result == null || result.isEmpty())
				new Exception("No matching country name found");

			standings.setCountryId(result);
			standings.setCountryName(countryName);

		} catch (Exception e) {
			throw e;
		}

		return result;
	}

	private void checkHTTPStatusForSuccess(HttpResponse httpResponse) {
		if (httpResponse.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + httpResponse.getStatusLine().getStatusCode());
		}
	}

	private JSONObject getIdValue(String valueToCompare, String idName, String valueName, String jsonString)
			throws ParseException {
		JSONParser parse = new JSONParser();

		JSONArray jsonarr_1 = (JSONArray) parse.parse(jsonString);

		for (int i = 0; i < jsonarr_1.size(); i++) {
			JSONObject jsonobj_1 = (JSONObject) jsonarr_1.get(i);
			if (jsonobj_1.get(valueName).equals(valueToCompare)) {
				return jsonobj_1;
			}
		}

		return null;
	}

	private String getJSONString(HttpResponse httpResponse) throws IOException {
		Scanner sc = new Scanner(new InputStreamReader((httpResponse.getEntity().getContent())));
		String output = "";
		while (sc.hasNext()) {
			output += sc.nextLine();
		}
		sc.close();
		return output;
	}

	private HttpHost getTarget() {
		HttpHost target = new HttpHost("apiv2.apifootball.com", 80, "http");
		return target;
	}

	@PreDestroy
	void cleanup() {
		httpclient.getConnectionManager().shutdown();
	}

}

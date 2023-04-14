/**
 * Copyright 2023 John Aronson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aronsonhome.diyadvisor;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.aronsonhome.diyadvisor.data.TradeData;

/**
 * Utilities for calling the RiaPro financial website
 * 
 * @author trav3
 */
public class RiaUtils
{
	//TODO add logging
	private static final String KEY_PORTFOLIO_PATH = "PORTFOLIO_PATH";

	private static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
	{
		public java.security.cert.X509Certificate[] getAcceptedIssuers()
		{
			return null;
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
		{
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
		{
		}
	} };

	private static final String TRANSACTION_BODY = "TRANSACTION_BODY";
	private static final String KEY_API_HOST = "API_HOST";
	private static final String KEY_LOGIN_PATH = "LOGIN_PATH";
	private static final String KEY_TRANSACTION_PATH = "TRANSACTION_PATH";
	private static final String KEY_PASSWORD = "PASSWORD";
	private static final String KEY_USERNAME = "USERNAME";
	private static final String PROPS_FILE = "ria.properties";

	private SSLContext sslContext;
	private Gson gson;
	private NumberFormat priceFormat;
	private NumberFormat quantityFormat;

	// map of portfolio names to portfolio ids
	private Map<String,String> portfolioMap;

	private static Properties props;

	public RiaUtils() throws Exception
	{		
		gson = new Gson();
		try
		{
			// Setup SSL context to ignore all certs (simplevisor isn't recognized by Java
			// 11 OOTB)
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new SecureRandom());
			
			priceFormat = new DecimalFormat("0.00");
			quantityFormat = new DecimalFormat("0.##");

			// load properties
			props = new Properties();
			InputStream inputStream = JmapUtils.class.getResourceAsStream("/" +PROPS_FILE);
			props.load(inputStream);
			String propOverrides = DIYUtils.get().fetchS3File(PROPS_FILE);
			props.load(new StringReader(propOverrides));
			if (!props.containsKey(KEY_API_HOST))
				throw new Exception("missing expected property: API_HOST");
		} catch (Exception e)
		{
			System.err.println(e);
			throw new Exception("failed to initialize RiaUtils", e);
		}
		DIYUtils.checkForPropertyKeys(props, List.of(KEY_USERNAME, KEY_PASSWORD, KEY_LOGIN_PATH, 
			KEY_PORTFOLIO_PATH, KEY_API_HOST, TRANSACTION_BODY, KEY_TRANSACTION_PATH), PROPS_FILE);
	}

	/**
	 * Log in to the RIA Pro website with username and password
	 * 
	 * @return auth key for this session to be used in later API calls
	 * @throws Exception
	 */
	public String login() throws Exception
	{
		JsonObject body = new JsonObject();
		body.addProperty("username", props.getProperty(KEY_USERNAME));
		body.addProperty("password", props.getProperty(KEY_PASSWORD));

		HttpRequest request = HttpRequest.newBuilder(
			new URI(props.getProperty(KEY_API_HOST) +props.getProperty(KEY_LOGIN_PATH)))
			.header("Content-Type", "application/json")
			.POST(BodyPublishers.ofString(gson.toJson(body)))
			.build();
		HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		if (response.statusCode() > 299)
			throw new Exception("Unexpected response from RIA Pro login call: " + response.statusCode() + " | " + response.body());

		JsonObject jsonBody = JsonParser.parseString(response.body()).getAsJsonObject();

		return jsonBody.get("auth_token").getAsString();
	}


	private String getPortfolioId(String portfolioName, String authToken) throws Exception
	{
		if(portfolioMap == null)
			portfolioMap = fetchPortfolioIds(authToken);
		return portfolioMap.get(portfolioName);
	}


	private Map<String,String> fetchPortfolioIds(String authToken) throws Exception
	{
		if(authToken == null)
			throw new Exception("missing expected args: authToken");
		
		HttpRequest request = HttpRequest.newBuilder(
			new URI(props.getProperty(KEY_API_HOST) +props.getProperty(KEY_PORTFOLIO_PATH)))
			.header("Authorization", "Bearer " +authToken)
			.GET()
			.build();
		HttpClient client = HttpClient.newBuilder()
				.sslContext(sslContext)
				.build();
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		if(response.statusCode() > 299)
			throw new Exception("Unexpected response from jmap session call: " +response.statusCode() +" | " +response.body());
		
		JsonArray resultBody = JsonParser.parseString(response.body()).getAsJsonArray();
		Map<String,String> portfolioMap = new HashMap<>(resultBody.size());
		for (int i = 0; i < resultBody.size(); i++)
		{
			JsonObject portfolio = resultBody.get(i).getAsJsonObject();
			portfolioMap.put(portfolio.get("name").getAsString(), portfolio.get("id").getAsString());
		}
		return portfolioMap;
	}

	/**
	 * add a trade to an RIA Portfolio
	 * 
	 * @param info trade info
	 * @param authToken auth token for the API call
	 * @throws Exception
	 */
	public void addTransaction(TradeData info, String authToken) throws Exception
	{
		String body = props.getProperty(TRANSACTION_BODY);
		String portfolioId = getPortfolioId(info.getAccount(), authToken);
		if(portfolioId == null)
			throw new Exception("unable to find portfolio id for portfolio: " +info.getAccount() +".\nDoes this model portfolio exist in RIAPro?");
		Map<String, String> vars = Map.of("ACCOUNT", info.getAccount(), 
				"ACCOUNT_ID", portfolioId,
				"ACTION", info.getAction().toString(),
				"SYMBOL", info.getSymbol(),
				"PRICE",  priceFormat.format(info.getPrice()),
				"TRADE_DATE",  info.getTradeDate().toString(),
				"QUANTITY", quantityFormat.format(info.getQuantity()));
		body = DIYUtils.regexSubs(body, vars);

		HttpRequest request = HttpRequest.newBuilder(
			new URI(props.getProperty(KEY_API_HOST) +props.getProperty(KEY_TRANSACTION_PATH)))
			.header("Authorization", "Bearer " +authToken)
			.header("Content-Type", "application/json")
			.POST(BodyPublishers.ofString(body))
			.build();
		HttpClient client = HttpClient.newBuilder()
				.sslContext(sslContext)
				.build();
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		if (response.statusCode() > 299)
			throw new Exception("Unexpected response from RIA Pro add transaction call: " + response.statusCode() + " | " + response.body());
	}
}

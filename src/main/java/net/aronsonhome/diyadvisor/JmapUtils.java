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
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.jsoup.Jsoup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.aronsonhome.diyadvisor.data.EmailFilter;
import net.aronsonhome.diyadvisor.data.MessageData;

/**
 * Utilities for JMAP Email aPI
 * 
 * @author trav3
 */
public class JmapUtils
{
	private static final String USERNAME = "USERNAME";
	private static final String ACCOUNT = "ACCOUNT";
	private static final String BLOB_URL = "BLOB_URL";
	public static final String API_BODY = "API_BODY";
	public static final String API_URL = "API_URL";
	public static final String AUTH_TOKEN = "AUTH_TOKEN";
	public static final String SESSION_URL = "SESSION_URL";
	private static final String PROPS_FILE = "fastmail.properties";
	private static Properties props;

	/**
	 * @throws Exception 
	 */
	public JmapUtils() throws Exception
	{
		//TODO add logging
		// load properties
		InputStream inputStream = JmapUtils.class.getResourceAsStream("/" +PROPS_FILE);
		
		try
		{
			props = new Properties();
			props.load(inputStream);
			String propOverrides = DIYUtils.get().fetchS3File(PROPS_FILE);
			props.load(new StringReader(propOverrides));
		} catch (Exception e)
		{
			System.err.println(e);
			throw new Exception("failed to initialize JMapUtils", e);
		}
		DIYUtils.checkForPropertyKeys(props, List.of(AUTH_TOKEN, API_URL, ACCOUNT, 
			BLOB_URL, ACCOUNT, USERNAME), PROPS_FILE);
		System.out.println("JmapUtils initialized. AccountId: " +props.getProperty(ACCOUNT));
	}

	/**
	 * fetch the session data from the JMap server
	 * 
	 * @return JsonObject containing the session properties for the service and the account associated with the AUTH_TOKEN
	 * @throws Exception
	 */
	@Deprecated
	public JsonObject fetchSession() throws Exception
	{
		if(!props.containsKey(SESSION_URL))
			throw new Exception("missing expected property: SESSION_URL");
		
		HttpRequest request = HttpRequest.newBuilder(new URI(props.getProperty(SESSION_URL)))
			.header("Authorization", "Bearer " +props.getProperty(AUTH_TOKEN))
			.GET()
			.build();
		HttpClient client = HttpClient.newBuilder().build();
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		if(response.statusCode() > 299)
			throw new Exception("Unexpected response from jmap session call: " +response.statusCode() +" | " +response.body());
		
		return JsonParser.parseString(response.body()).getAsJsonObject();
	}

	/**
	 * fetch the blobIds for all messages that match the subject string of interest and that were received after the startDate for the query
	 * 
	 * @param startDate messages more recent than the startDate will be fetched
	 * @param filters email filters for all the different types of email queries that should be made to the JMap API
	 * @return map of email message ids -> MessageData objects
	 * @throws Exception
	 */
	public Map<String, MessageData> fetchMessages(Instant startDate, Collection<EmailFilter> filters) throws Exception
	{		
		Map<String,MessageData> blobIds = new HashMap<>();
		
		//TODO change this subject to a data object and add the from field as a query
		//loop based on SUBJECT properties and call fastmail once per subject
		for (EmailFilter filter : filters)
		{		
			String body = props.getProperty(API_BODY);
			String accountId = props.getProperty(ACCOUNT);
			Map<String, String> vars = Map.of("ACCT_ID", accountId, "START_DATE", 
				startDate.toString(), "FROM", filter.getFrom(), "EMAIL_SUBJECT", filter.getSubject());
			body = DIYUtils.regexSubs(body, vars);

			HttpRequest request = HttpRequest.newBuilder(new URI(props.getProperty(API_URL)))
				.header("Authorization", "Bearer " +props.getProperty(AUTH_TOKEN))
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(body))
				.build();
			HttpClient client = HttpClient.newBuilder().build();
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			if(response.statusCode() > 299)
				throw new Exception("Unexpected response from jmap session call: " +response.statusCode() +" | " +response.body());
			
			JsonObject jsonBody = JsonParser.parseString(response.body()).getAsJsonObject();
	
			JsonArray methodResponses = jsonBody.getAsJsonArray("methodResponses");
	
			JsonArray firstResponse = methodResponses.get(0).getAsJsonArray();
			if("error".equals(firstResponse.get(0).getAsString()))
				throw new Exception("error response from jmap session call: " +firstResponse);
	
			if(methodResponses.size() != 4)
				throw new Exception("wrong number of jmap method responses from email api. Expected 4, actual responses:" +methodResponses.size());
			JsonArray lastResponse = methodResponses.get(3).getAsJsonArray();
			JsonArray emailList = lastResponse.get(1).getAsJsonObject().get("list").getAsJsonArray();
			
			for (int i = 0; i < emailList.size(); i++)
			{
				JsonObject email = emailList.get(i).getAsJsonObject();
				MessageData messageData = new MessageData(email);
				//subject should be something that matches what MessageHandlers are expecting
				messageData.setHandler(filter.getHandler());
				blobIds.put(messageData.getMessageId(), messageData);
			}
		}
		return blobIds;
	}

	/**
	 * Fetch the message bodies for a list of blobids from the JMap service
	 * @param messages map of messageid -> MessageData. This is the source of of the blobIds for JMAP call to 
	 * fetch the text body of the messages
	 * @return The same input list of MessageData with the value objects enriched with the textBody field 
	 * @throws Exception
	 */
	public Map<String, MessageData> fetchMsgBodys(Map<String, MessageData> messages) throws Exception
	{
		for (Entry<String,MessageData> blobEntry : messages.entrySet())
		{
			String blobUrl = props.getProperty(BLOB_URL);
			MessageData message = blobEntry.getValue();
			String accountId = props.getProperty(ACCOUNT);
			String username = props.getProperty(USERNAME);
			Map<String, String> vars = Map.of("ACCT_ID", accountId, 
					USERNAME, username,
					"BLOB_ID", message.getBodyBlobId());
			blobUrl = DIYUtils.regexSubs(blobUrl, vars);
			
			HttpRequest request = HttpRequest.newBuilder(new URI(blobUrl))
				.header("Authorization", "Bearer " +props.getProperty(AUTH_TOKEN))
				.GET()
				.build();
			HttpClient client = HttpClient.newBuilder().build();
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			if(response.statusCode() > 299)
				throw new Exception("Unexpected response from jmap session call: " +response.statusCode() +" | " +response.body());
			
			String bodyText = response.body();
			if(bodyText.contains("<html") && bodyText.contains("<head>"))
				bodyText = Jsoup.parse(bodyText).wholeText();
			message.setTextBody(bodyText);
			messages.put(blobEntry.getKey(), message);
		}
		
		return messages;
	}
}
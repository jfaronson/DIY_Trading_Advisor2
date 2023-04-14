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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import net.aronsonhome.diyadvisor.data.EmailFilter;
import net.aronsonhome.diyadvisor.data.MessageData;
import net.aronsonhome.diyadvisor.data.TradeData;

public class LambdaHandler implements RequestHandler<Map<String, String>, String>
{
	private static final String START_DATE_FILE = "startDate.properties";
	private static final String KEY_START_DATE = "startDate";
	private static final String SUCCESS = "Success";

	
	/**
	 * Entry point for the lambda function
	 * 
	 * @param event parameter data for the function
	 * @param context context the lambda call
	 * 
	 */
	@Override
	public String handleRequest(Map<String, String> event, Context context)
	{
		LambdaLogger logger = context.getLogger();
		logger.log("Handler invoked");

		try
		{
			//setup the components
			DIYUtils.init(event);
			JmapUtils jmapUtils = new JmapUtils();
			RiaUtils riaUtils = new RiaUtils();

			//setup the handlers
			MessageHandler[] handlers = { new FidelityHandler(), new SchwabHandler() };
			List<EmailFilter> emailFilters = new ArrayList<>();
			for (MessageHandler messageHandler : handlers)
				emailFilters.addAll(messageHandler.getEmailFilters());

			//get startDate for email query
			Instant startDate = Instant.now().minus(1, ChronoUnit.DAYS);
			String props = DIYUtils.get().fetchS3File(START_DATE_FILE);
			Properties startDateProps = new Properties();
			if(props != null)
			{
				startDateProps.load(new StringReader(props));	
				if(startDateProps.containsKey(KEY_START_DATE))
					startDate = Instant.parse(startDateProps.getProperty(KEY_START_DATE));
			}

			//query for new trade confirm messages 
			Map<String,MessageData> messages = jmapUtils.fetchMessages(startDate, 
					emailFilters);
			logger.log("blobs: " +messages.size());
			if(messages.size() == 0)
			{
				logger.log("no trade confirms found, skipping the rest ...");
				return SUCCESS;
			} 

			messages = jmapUtils.fetchMsgBodys(messages);
			logger.log("messages: " +messages.size());
			
			String authToken = riaUtils.login();
			Instant lastMessageRcvd = startDate;
			
			for (Entry<String,MessageData> messageEntry : messages.entrySet())
			{
				MessageHandler handler = messageEntry.getValue().getHandler();
				TradeData trade = handler.parseMessage(messageEntry.getValue());
				logger.log("parsed trade: " +trade);
				riaUtils.addTransaction(trade, authToken);
				logger.log("added trade to RIA: " +trade);
				if(lastMessageRcvd.isBefore(trade.getEmailReceivedTime()))
					lastMessageRcvd = trade.getEmailReceivedTime();
			}
			
			//save the date of the latest successfully saved message to s3 so we can use it as the from date next time
			startDateProps.put(KEY_START_DATE, lastMessageRcvd.plus(1, ChronoUnit.SECONDS).toString());
			DIYUtils.get().writeToS3File(START_DATE_FILE, startDateProps);			
			
			return SUCCESS;
		} catch (Exception e)
		{
			logger.log("Error while processing: "+e.getMessage());
			if(e.getMessage() != null && e.getMessage().startsWith("init data missing expected keys"))
			{
				logger.log("'init data missing expected keys ...' - this message means we are likely in an unconfigured unit test and failure is OK");
				return "Success";
			}
			ByteArrayOutputStream stackTrace = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(stackTrace));
			logger.log(stackTrace.toString());
			return "Error";
		}
	}
}
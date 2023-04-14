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

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aronsonhome.diyadvisor.data.EmailFilter;
import net.aronsonhome.diyadvisor.data.MessageData;
import net.aronsonhome.diyadvisor.data.TradeData;
import net.aronsonhome.diyadvisor.data.TransactionType;

/**
 * Utilities for handling Schwab email messages
 * 
 * @author trav3
 */
public class SchwabHandler implements MessageHandler
{
	private static final String KEY_SUBJECT = "SUBJECT";
	private static final String KEY_FROM = "FROM";
	private static final String PROPS_FILE = "/schwab.properties";
	private static final String DATE_FORMAT = "DATE_FORMAT";
	private static final String ACCOUNT_PREFIX = "ACCOUNT_PREFIX";
	private static final String SYMBOL_REGEX = "SYMBOL_REGEX";
	private static final String ACTION_REGEX = "ACTION_REGEX";
	private static final String ACCOUNT_REGEX = "ACCOUNT_REGEX";
	private static final String TRADE_REGEX = "TRADE_REGEX";
	
	private Properties props = new Properties();
	private Pattern tradeRegexp;
	private Pattern accountRegex;
	private Pattern actionRegex;
	private Pattern symbolRegex;
	private DateFormat df;

	
	public SchwabHandler() throws Exception
	{
		InputStream inputStream = FidelityHandler.class.getResourceAsStream(PROPS_FILE);
		try
		{
			props.load(inputStream);
			DIYUtils.checkForPropertyKeys(props, List.of(ACCOUNT_PREFIX, KEY_FROM, KEY_SUBJECT, ACCOUNT_REGEX,
				ACTION_REGEX, SYMBOL_REGEX, TRADE_REGEX, DATE_FORMAT), PROPS_FILE);
			accountRegex = Pattern.compile(props.getProperty(ACCOUNT_REGEX), Pattern.CASE_INSENSITIVE 
					| Pattern.MULTILINE);
			actionRegex = Pattern.compile(props.getProperty(ACTION_REGEX), Pattern.CASE_INSENSITIVE 
					| Pattern.MULTILINE);
			symbolRegex = Pattern.compile(props.getProperty(SYMBOL_REGEX), Pattern.CASE_INSENSITIVE 
					| Pattern.MULTILINE);
			tradeRegexp = Pattern.compile(props.getProperty(TRADE_REGEX), Pattern.CASE_INSENSITIVE 
					| Pattern.MULTILINE);
			df = new SimpleDateFormat(props.getProperty(DATE_FORMAT));
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}	
	}



	@Override
	public TradeData parseMessage(MessageData message) throws ParseException
	{
		TradeData data = null;
		
		Matcher m = accountRegex.matcher(message.getTextBody());		
		if(m.find())
		{
			data = new TradeData();
			data.setAccount(props.getProperty(ACCOUNT_PREFIX) +m.group("account"));
			data.setTradeDate(df.parse(m.group("date")).toInstant());
		}
		
		m = actionRegex.matcher(message.getTextBody());		
		if(m.find() && data != null)
			data.setAction(toTransactionType(m));
		
		m = symbolRegex.matcher(message.getTextBody());		
		if(m.find() && data != null)
			data.setSymbol(m.group("symbol"));
		
		m = tradeRegexp.matcher(message.getTextBody());		
		if(m.find() && data != null)
		{
			data.setQuantity(Double.parseDouble(m.group("quantity")));
			data.setPrice(Double.parseDouble(m.group("price")));
		}
		data.setEmailReceivedTime(message.getEmailReceivedTime());

		return data;
	}



	private TransactionType toTransactionType(Matcher m)
	{
		switch(m.group("action"))
		{
		case "Sale":
			return TransactionType.Sell;
		case "Purchase":
			return TransactionType.Buy;
		default:
			return null;
		}
	}	

	@Override
	public Collection<EmailFilter> getEmailFilters()
	{
		return List.of(new EmailFilter(props.getProperty(KEY_FROM), 
			props.getProperty(KEY_SUBJECT), this));
	}
}

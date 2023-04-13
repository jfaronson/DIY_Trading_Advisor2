/**
 * 
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
 * Utilities for handling Trade ConfirmationMessages from Fidelity
 * 
 * @author trav3
 */
public class FidelityHandler implements MessageHandler
{
	private static final String PROPS_FILE = "/fidelity.properties";
	private static final String DATE_FORMAT = "DATE_FORMAT";
	private static final String ACCOUNT_PREFIX = "ACCOUNT_PREFIX";
	private static final String TRADE_REGEX = "TRADE_REGEX";
	private Properties props = new Properties();
	private Pattern tradeRegexp;
	private DateFormat df;
		
	public FidelityHandler()
	{
		InputStream inputStream = FidelityHandler.class.getResourceAsStream(PROPS_FILE);
		try
		{
			props.load(inputStream);
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
		Matcher m = tradeRegexp.matcher(message.getTextBody());
		
		TradeData data = null;
		if(m.find())
		{
			data = new TradeData();
			data.setSymbol(m.group("symbol"));
			data.setAction(toTransactionType(m));
			data.setAccount(props.getProperty(ACCOUNT_PREFIX) +m.group("account"));
			data.setQuantity(Double.parseDouble(m.group("quantity")));
			data.setPrice(Double.parseDouble(m.group("price")));
			data.setTradeDate(df.parse(m.group("date")).toInstant());
			data.setEmailReceivedTime(message.getEmailReceivedTime());
		}
		
		return data;
	}

	private TransactionType toTransactionType(Matcher m)
	{
		switch(m.group("action"))
		{
		case "SELL":
			return TransactionType.Sell;
		case "BUY":
			return TransactionType.Buy;
		default:
			return null;
		}
	}

	@Override
	public Collection<EmailFilter> getEmailFilters()
	{
		String from = props.getProperty("FROM");
		return List.of( new EmailFilter(from, props.getProperty("SUBJECT_1"), this), 
				new EmailFilter(from, props.getProperty("SUBJECT_2"), this) );
	}
}

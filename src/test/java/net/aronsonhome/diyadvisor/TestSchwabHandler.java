/**
 * 
 */
package net.aronsonhome.diyadvisor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.aronsonhome.diyadvisor.data.EmailFilter;
import net.aronsonhome.diyadvisor.data.MessageData;
import net.aronsonhome.diyadvisor.data.TradeData;

/**
 * @author trav3
 */
class TestSchwabHandler
{
	private static MessageHandler util;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
		util = new SchwabHandler();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterAll
	static void tearDownAfterClass() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception
	{
	}

	/**
	 * Test method for {@link net.aronsonhome.diyadvisor.FidelityHandler#parseMessage(MessageData)}.
	 * @throws Exception 
	 */
	@Test
	void testParseMessage() throws Exception
	{
		//read in message body
		StringBuffer sb = readMessageBody("/schwab_body.txt");
		EmailFilter filter = util.getEmailFilters().iterator().next();
		String id = UUID.randomUUID().toString();
		MessageData message = new MessageData(id, filter.getSubject(), id, sb.toString(), 
			filter.getFrom(), util, Instant.now());
		
		//call testParseMessage
		TradeData result = util.parseMessage(message);
		
		//test results 
		assertNotNull(result);
		assertNotNull(result.getSymbol());
		assertNotNull(result.getPrice());
		assertNotNull(result.getQuantity());
		assertNotNull(result.getAccount());
		assertNotNull(result.getAction());
		assertNotNull(result.getTradeDate());
	}

	private StringBuffer readMessageBody(String fileName) throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(fileName)));
		StringBuffer sb = new StringBuffer();
		for(String line = in.readLine(); line != null; line = in.readLine())
		{
			sb.append(line);
			sb.append("\n");
		}
		return sb;
	}

}

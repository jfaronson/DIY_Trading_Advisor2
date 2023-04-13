/**
 * 
 */
package net.aronsonhome.diyadvisor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.aronsonhome.diyadvisor.data.TradeData;
import net.aronsonhome.diyadvisor.data.TransactionType;

/**
 * @author trav3
 *
 */
class TestRiaUtils
{
	private static Map<String,String> event;
	private static RiaUtils utils;

	/**
	 * @throws java.lang.Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
		String aws_properties = System.getProperty("AWS_PROPERTIES");
		if(aws_properties != null)
		{
			Properties props = new Properties();
			InputStream inputStream = JmapUtils.class.getResourceAsStream(aws_properties);
			if(inputStream == null)
				throw new Exception("No properties file found at Java System Property named AWS_PROPERTIES, value : " +aws_properties);
			props.load(inputStream);
			event = Map.copyOf((Map)props);
			DIYUtils.init(event);
			utils = new RiaUtils();
		}
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
	 * Test method for {@link net.aronsonhome.diyadvisor.RiaUtils#addTransaction(net.aronsonhome.diyadvisor.data.TradeData, java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	void testAddTransaction() throws Exception
	{
		if(event == null)
		{
			System.out.println("no event set, skipping s3 test");
			return;
		}
		long startTime = System.currentTimeMillis();
		String authToken = utils.login();
		System.out.println("ria login time:  " +(System.currentTimeMillis() -startTime));
		System.out.println("auth_token: " + authToken);
		assertNotNull(authToken);
		
		Instant emailReceivedDate = Instant.now();
		Instant tradeDate = Instant.ofEpochMilli(emailReceivedDate.toEpochMilli() -24*60*60*1000);
		TradeData info = new TradeData("XOM", TransactionType.Buy, "TestPortfolio_XXXX1234", 1, 114.55, 
			tradeDate, emailReceivedDate);
		startTime = System.currentTimeMillis();
		assertTrue(utils.addTransaction(info, authToken));
		System.out.println("ria add transaction time:  " +(System.currentTimeMillis() -startTime));
		System.out.println("successfully added trade: " +info);
		
		info = new TradeData("XOM", TransactionType.Buy, "TestPortfolio_XXXX4321", 1, 114.55, 
			tradeDate, emailReceivedDate);
		startTime = System.currentTimeMillis();
		assertTrue(utils.addTransaction(info, authToken));
		System.out.println("ria add transaction time:  " +(System.currentTimeMillis() -startTime));
		System.out.println("successfully added trade: " +info);
		
		info = new TradeData("XOM", TransactionType.Sell, "TestPortfolio_XXXX1234", 1, 114.55, 
			emailReceivedDate, emailReceivedDate);
		startTime = System.currentTimeMillis();
		assertTrue(utils.addTransaction(info, authToken));
		System.out.println("ria add transaction time:  " +(System.currentTimeMillis() -startTime));
		System.out.println("successfully added trade: " +info);
		
		info = new TradeData("XOM", TransactionType.Sell, "TestPortfolio_XXXX4321", 1, 114.55, 
			emailReceivedDate, emailReceivedDate);
		startTime = System.currentTimeMillis();
		assertTrue(utils.addTransaction(info, authToken));
		System.out.println("ria add transaction time:  " +(System.currentTimeMillis() -startTime));
		System.out.println("successfully added trade: " +info);
	}
}

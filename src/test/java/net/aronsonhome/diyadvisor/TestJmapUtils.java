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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.aronsonhome.diyadvisor.data.EmailFilter;
import net.aronsonhome.diyadvisor.data.MessageData;

/**
 * @author trav3
 *
 */
class TestJmapUtils
{
	private static Map<String,String> event;
	private static JmapUtils utils;
	private static List<EmailFilter> filterList;

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
			utils = new JmapUtils();
		}
		MessageHandler handler1 = new FidelityHandler();
		MessageHandler handler2 = new SchwabHandler();
		EmailFilter filter1 = handler1.getEmailFilters().iterator().next();
		EmailFilter filter2 = handler2.getEmailFilters().iterator().next();
		filterList = List.of(filter2, filter1);
		
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
	 * Test method for {@link net.aronsonhome.diyadvisor.JmapUtils#fetchMsgBodys(java.util.Map)}.
	 * @throws Exception 
	 */
	@Test
	void testFetchMsgBodys() throws Exception
	{
		if(event == null)
		{
			System.out.println("no event set, skipping s3 test");
			return;
		}
		long startTime = System.currentTimeMillis();

		startTime = System.currentTimeMillis();
		int daysToFetch = Integer.parseInt(System.getProperty("daysToFetch", "1"));
		Map<String,MessageData> messages = utils.fetchMessages(Instant.now().minus(daysToFetch, ChronoUnit.DAYS), 
			filterList);
		System.out.println("blobIds fetch time:  " +(System.currentTimeMillis() -startTime));
		System.out.println("blobs: " +messages.size());

		startTime = System.currentTimeMillis();
		messages = utils.fetchMsgBodys(messages);
		System.out.println("bodys fetch time:  " +(System.currentTimeMillis() -startTime));
		System.out.println("bodys: " +messages.size());
		if(messages.size() > 0)
		{
			String firstKey = messages.keySet().iterator().next();
			assertNotNull(messages.get(firstKey).getTextBody());			
		}
	}
}

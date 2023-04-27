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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.s3.model.AmazonS3Exception;

/**
 * @author trav3
 *
 */
class TestDIYUtils
{
	private static final String TEST_PROPERTIES = "fastmail.properties";
	private static Map<String,String> event;

	/**
	 * @throws java.lang.Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
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

	@Test
	void testGetS3File() throws Exception
	{
		if(event == null)
		{
			System.out.println("no event set, skipping s3 test");
			return;
		}
		String s = DIYUtils.get().fetchS3File(TEST_PROPERTIES);
		System.out.println(s);
		assertNotNull(s);
		
		//try getting a file that shouldn't exist
		try
		{
			s = DIYUtils.get().fetchS3File("foobar.txt");
			assertNull(s, "expected failure");
		} catch (AmazonS3Exception e)
		{
			System.out.println("caught expected exception: " +e);
			assertEquals("NoSuchKey", e.getErrorCode());
		}
	}

	@Test
	void writeGetS3File() throws IOException
	{
		if(event == null)
		{
			System.out.println("no event set, skipping s3 test");
			return;
		}
		
		InputStream inputStream = JmapUtils.class.getResourceAsStream("/" +TEST_PROPERTIES);
		
		Properties props = new Properties();
		props.load(inputStream);
		
		//this should work or throw an exception to fail
		DIYUtils.get().writeToS3File("test.properties", props);
	}
}

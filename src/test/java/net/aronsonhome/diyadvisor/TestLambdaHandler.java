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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Map; 

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

/**
 * Tests for LambdaHandler
 * 
 * @author trav3
 */
class TestLambdaHandler
{
	@Mock Context context;
	@Mock LambdaLogger logger;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
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
	@SuppressWarnings("rawtypes")
	@BeforeEach
	void setUp() throws Exception
	{
		MockitoAnnotations.openMocks(this);
		when(context.getLogger()).thenReturn(logger);
		doAnswer(new Answer() {
		     public Object answer(InvocationOnMock invocation) {
		         Object[] args = invocation.getArguments();
		         System.out.println(args[0]);
				return null;
		    }}).when(logger).log(anyString());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception
	{
	}

	/**
	 * Test method for {@link net.aronsonhome.diyadvisor.LambdaHandler#handleRequest(java.util.Map, com.amazonaws.services.lambda.runtime.Context)}.
	 */
	@Test
	void testHandleRequest()
	{
		LambdaHandler handler = new LambdaHandler();
		Map<String, String> event = Map.of();
		assertEquals("Success", handler.handleRequest(event, context));
	}

}

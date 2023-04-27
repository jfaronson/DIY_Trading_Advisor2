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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

/**
 * General Utilities for the DIY Advisor project
 * 
 * @author trav3
 */
public class DIYUtils
{
	private static Logger logger = LoggerFactory.getLogger(DIYUtils.class);
	
	private static final String BUCKET_NAME = "bucketName";
	private static final String AWS_REGION = "awsRegion";
	private static final String SECRET_KEY = "secretKey";
	private static final String ACCESS_KEY = "accessKey";
	
	private static DIYUtils instance = null;
	private static String accessKey = null;
	private static String bucketName = null;
	private static String secretKey = null;
	private static Regions region = null;
	
	private AmazonS3 s3client = null;

	/**
	 * Initialize the S3 client
	 * CALL THIS ONE TIME BEFORE ANYTHING ELSE 
	 * 
	 * @param data map should contain: ACCESS_KEY, SECRET_KEY, AWS_REGION, BUCKET_NAME  
	 * @throws Exception
	 */
	public static void init(Map<String, String> data) throws Exception
	{
		if(!data.containsKey(ACCESS_KEY) || !data.containsKey(SECRET_KEY) || !data.containsKey(AWS_REGION) || !data.containsKey(BUCKET_NAME))
			throw new Exception("init data missing expected keys: " +ACCESS_KEY +", " +SECRET_KEY +", " +AWS_REGION +", " +BUCKET_NAME);
		accessKey = data.get(ACCESS_KEY);
		bucketName = data.get(BUCKET_NAME);
		secretKey = data.get(SECRET_KEY);
		region = Regions.fromName(data.get(AWS_REGION).trim());
	} 
	
	/**
	 * @return the instance of DIY Utils
	 */
	public static DIYUtils get()
	{
		if(instance == null)
		{
			synchronized(DIYUtils.class)
			{
				if(instance == null)
					instance = new DIYUtils();
			}
		}
		return instance;
	}
	
	private DIYUtils()
	{
		if(accessKey == null || secretKey == null || region == null)
			throw new RuntimeException("DIYUtils: must call init(Map<String, String> data) before first use!");
		
		AWSCredentials credentials = new BasicAWSCredentials(
			  accessKey, secretKey);

		s3client = AmazonS3ClientBuilder
			  .standard()
			  .withCredentials(new AWSStaticCredentialsProvider(credentials))
			  .withRegion(region)
			  .build();
	}
	
	/**
	 * Write a property set to S3 storage 
	 * 
	 * @param filename 
	 * @param props
	 * @throws IOException
	 */
	public void writeToS3File(String filename, Properties props) throws IOException 
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		props.store(new PrintStream(baos), null);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentLength(baos.size());
	 	PutObjectResult result = s3client.putObject(bucketName, filename, 
	 		new ByteArrayInputStream(baos.toByteArray()), meta);
	 	
	}
	
	/**
	 * Get file contents from S3 storage
	 * 
	 * @param filename
	 * @return file contents as a string
	 * @throws Exception
	 */
	public String fetchS3File(String filename) throws Exception
	{
		try
		{
			S3Object s3object = s3client.getObject(bucketName, filename);
			S3ObjectInputStream inputStream = s3object.getObjectContent();
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter out = new PrintWriter(baos);

			for (String line = in.readLine(); line != null; line = in.readLine())
			{
				out.write(line);
				out.write('\n');
			}
			out.close();

			return baos.toString();
		} catch (AmazonS3Exception e)
		{
			//if the file doesn't exist return null
			if("NoSuchKey".equals(e.getErrorCode()))
				return null;
			
			logger.error("caught exception in fetchS3File", e);
			throw e;
		}
	}
	
	/**
	 * Perform substitutions on a template string
	 * 
	 * @param template starting template, substitutions look like: {{VAR_NAME}}
	 * @param variables map of variable name to variable value
	 * @return template after the substitutions
	 */
	public static String regexSubs(String template, Map<String, String> variables)
	{
		for (String key : variables.keySet())
			template = template.replaceAll("\\{\\{" +key +"\\}\\}", variables.get(key));
	
		return template;
	}
	
	/**
	 * check a properties set and ensure that all the expected/required property keys are present
	 * 
	 * @param properties 
	 * @param keys
	 * @param filename TODO
	 * @throws Exception
	 */
	public static void checkForPropertyKeys(Properties properties, Collection<String> keys, String filename) throws Exception
	{
		for (String key : keys)
		{
			if(!properties.containsKey(key))
				throw new Exception(filename +" missing expected property: " +key);
		}
	}
}

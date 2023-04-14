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

import java.text.ParseException;
import java.util.Collection;

import net.aronsonhome.diyadvisor.data.EmailFilter;
import net.aronsonhome.diyadvisor.data.MessageData;
import net.aronsonhome.diyadvisor.data.TradeData;

public interface MessageHandler
{
	/**
	 * Get data from the handler about how messages that it can handle can be queried from email
	 * 
	 * @return collection of EmailFilters
	 */
	public Collection<EmailFilter> getEmailFilters();
	
	/**
	 * Parse a message and return a TradeData with the details found in the body
	 * 
	 * @param message email message data, the textBody will be parsed
	 * @return TradeData with the details of the trade in the email
	 * @throws ParseException
	 */
	public TradeData parseMessage(MessageData message) throws ParseException;

}
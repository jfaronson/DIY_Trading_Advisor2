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
package net.aronsonhome.diyadvisor.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.aronsonhome.diyadvisor.MessageHandler;

/**
 * Data object for Jmap email queries
 * 
 * @author trav3
 */
@Data
@AllArgsConstructor
public class EmailFilter
{
	/** filter for messages from this sender */
	private String from;
	/** filter for messages with a subject matching  */
	private String subject;
	/** the handler that should be able to parse and messsages returned from the query */
	private MessageHandler handler;
}

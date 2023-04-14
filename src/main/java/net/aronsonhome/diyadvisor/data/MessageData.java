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

import java.time.Instant;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.aronsonhome.diyadvisor.MessageHandler;

/**
 * Data object for email messages from the JMAP API
 * 
 * @author trav3
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class MessageData
{
	/** id for the message returned by the JMAP source */
	@NonNull
	private String messageId;
	/** subject of the email */
	@NonNull
	private String subject;
	/** id of the blob records that holds the body */
	@NonNull
	private String bodyBlobId;
	/** text body of the message */
	private String textBody;
	/** email address of the sender of the email */
	private String from;
	/** message handler that can parse the text body */
	@NonNull
	private MessageHandler handler;
	/** time the email was received */
	private Instant emailReceivedTime;
	
	/**
	 * Constructor from a Json object
	 * 
	 * @param json
	 */
	public MessageData(JsonObject json)
	{
		JsonObject textBody = json.get("textBody").getAsJsonArray().get(0).getAsJsonObject();
		messageId = json.get("messageId").getAsString();
		subject = json.get("subject").getAsString();
		bodyBlobId = textBody.get("blobId").getAsString();
		emailReceivedTime = Instant.parse(json.get("receivedAt").getAsString());
	}
}

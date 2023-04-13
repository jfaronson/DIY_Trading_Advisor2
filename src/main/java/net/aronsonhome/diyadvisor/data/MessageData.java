/**
 * 
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
 * 
 * @author trav3
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class MessageData
{
	@NonNull
	private String messageId;
	@NonNull
	private String subject;
	@NonNull
	private String bodyBlobId;
	private String textBody;
	private String from;
	@NonNull
	private MessageHandler handler;
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

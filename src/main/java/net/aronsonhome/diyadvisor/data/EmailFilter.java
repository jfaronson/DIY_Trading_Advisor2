/**
 * 
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
	private String from;
	private String subject;
	private MessageHandler handler;
}

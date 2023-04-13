package net.aronsonhome.diyadvisor;

import java.text.ParseException;
import java.util.Collection;

import net.aronsonhome.diyadvisor.data.EmailFilter;
import net.aronsonhome.diyadvisor.data.MessageData;
import net.aronsonhome.diyadvisor.data.TradeData;

public interface MessageHandler
{
	public Collection<EmailFilter> getEmailFilters();
	public TradeData parseMessage(MessageData message) throws ParseException;

}
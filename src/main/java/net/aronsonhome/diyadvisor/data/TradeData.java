/**
 * 
 */
package net.aronsonhome.diyadvisor.data;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data object for a trade
 * 
 * @author trav3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeData
{
	private String symbol;
	private TransactionType action;
	private String account;
	private double quantity;
	private double price;
	private Instant tradeDate;
	private Instant emailReceivedTime;
}

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
	/** stock symbol for the trade */
	private String symbol;
	/** Buy or Sell Action */
	private TransactionType action;
	/** Portfolio name in RIA Pro */
	private String account;
	/** quantity for the trade */
	private double quantity;
	/** price the trade */
	private double price;
	/** date that the trade took place */
	private Instant tradeDate;
	/** time when the email notification for the trade was received */
	private Instant emailReceivedTime;
}

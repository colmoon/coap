package com.coap.core.network;

import com.coap.core.coap.Token;
import com.coap.core.network.Exchange.KeyMID;

/**
 * The remove handler can be set to an {@link Exchange} and will be invoked
 * for release the exchange from the exchange store.
 */
public interface RemoveHandler {

	/**
	 * Remove exchange from store.
	 * 
	 * @param exchange exchange to remove from store
	 * @param token token to remove exchange. Maybe {@code null}.
	 * @param key mid key to remove exchange. Maybe {@code null}.
	 */
	void remove(Exchange exchange, Token token, KeyMID key);
}

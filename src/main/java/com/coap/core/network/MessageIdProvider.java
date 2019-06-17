package com.coap.core.network;

import java.net.InetSocketAddress;

/**
 * A provider of CoAP message IDs.
 */
public interface MessageIdProvider {

	/**
	 * Gets a message ID for a destination endpoint.
	 * <p>
	 * Message IDs are guaranteed to not being issued twice within EXCHANGE_LIFETIME
	 * as defined by the <a href="https://tools.ietf.org/html/rfc7252#section-4.4">CoAP spec</a>.
	 * 
	 * @param destination the destination that the message ID must be <em>free to use</em> for.
	 *        This means that the message ID returned must not have been used in a message
	 *        to this destination for at least EXCHANGE_LIFETIME.
	 * @return a message ID or {@code -1} if there is no message ID available for the given destination
	 *         at the moment.
	 */
	int getNextMessageId(InetSocketAddress destination);
}

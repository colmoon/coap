package com.coap.core.network;

/**
 * A interface helper for keeping track of message IDs.
 */
public interface MessageIdTracker {

	/**
	 * Total number of MIDs.
	 */
	final int TOTAL_NO_OF_MIDS = 1 << 16;

	/**
	 * Gets the next usable message ID.
	 * 
	 * @return a message ID or {@code -1} if all message IDs are in use
	 *         currently.
	 */
	int getNextMessageId();
}

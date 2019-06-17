package com.coap.core.network;

import com.coap.core.coap.Message;
import com.coap.core.network.config.NetworkConfig;
import com.coap.elements.util.ClockUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A helper for keeping track of message IDs using a map.
 * <p>
 * According to the
 * <a href="https://tools.ietf.org/html/rfc7252#section-4.4">CoAP spec</a>
 * 
 * <pre>
 * The same Message ID MUST NOT be reused (in communicating with the
   same endpoint) within the EXCHANGE_LIFETIME (Section 4.8.2).
 * </pre>
 */
public class MapBasedMessageIdTracker implements MessageIdTracker {

	private final Map<Integer, Long> messageIds;
	private final long exchangeLifetimeNanos; // milliseconds
	private final int min;
	private final int range;
	private int counter;

	/**
	 * Creates a new tracker based on configuration values.
	 * 
	 * The following configuration value is used:
	 * <ul>
	 * <li>{@link com.coap.core.network.config.NetworkConfig.Keys#EXCHANGE_LIFETIME}
	 * - each message ID returned by <em>getNextMessageId</em> is marked as
	 * <em>in use</em> for this amount of time (ms).</li>
	 * </ul>
	 * 
	 * @param initialMid initial MID
	 * @param minMid minimal MID (inclusive).
	 * @param maxMid maximal MID (exclusive).
	 * @param config configuration
	 * @throws IllegalArgumentException if minMid is not smaller than maxMid or
	 *             initialMid is not in the range of minMid and maxMid
	 */
	public MapBasedMessageIdTracker(int initialMid, int minMid, int maxMid, NetworkConfig config) {
		if (minMid >= maxMid) {
			throw new IllegalArgumentException("max. MID " + maxMid + " must be larger than min. MID " + minMid + "!");
		}
		if (initialMid < minMid || maxMid <= initialMid) {
			throw new IllegalArgumentException(
					"initial MID " + initialMid + " must be in range [" + minMid + "-" + maxMid + ")!");
		}
		exchangeLifetimeNanos = TimeUnit.MILLISECONDS.toNanos(config.getLong(NetworkConfig.Keys.EXCHANGE_LIFETIME));
		counter = initialMid - minMid;
		min = minMid;
		range = maxMid - minMid;
		messageIds = new HashMap<>(range);
	}

	/**
	 * Gets the next usable message ID.
	 * 
	 * @return a message ID or {@code Message.NONE} if all message IDs are in
	 *         use currently.
	 */
	public int getNextMessageId() {
		int result = Message.NONE;
		boolean wrapped = false;
		final long now = ClockUtil.nanoRealtime();
		synchronized (messageIds) {
			// mask mid to the range
			counter = (counter & 0xffff) % range;
			int startIdx = counter;
			while (result < 0 && !wrapped) {
				// mask mid to the range
				int idx = counter++ % range;
				Long earliestUsage = messageIds.get(idx);
				if (earliestUsage == null || (earliestUsage - now) <= 0) {
					// message Id can be safely re-used
					result = idx + min;
					messageIds.put(idx, now + exchangeLifetimeNanos);
				}
				wrapped = (counter % range) == startIdx;
			}
		}
		return result;
	}
}

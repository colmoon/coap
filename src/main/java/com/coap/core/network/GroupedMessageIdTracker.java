package com.coap.core.network;

import com.coap.core.coap.Message;
import com.coap.core.network.config.NetworkConfig;
import com.coap.elements.util.ClockUtil;

import java.util.concurrent.TimeUnit;

/**
 * A helper for keeping track of message IDs.
 * <p>
 * According to the
 * <a href="https://tools.ietf.org/html/rfc7252#section-4.4">CoAP spec</a>
 * 
 * <pre>
 * The same Message ID MUST NOT be reused (in communicating with the
   same endpoint) within the EXCHANGE_LIFETIME (Section 4.8.2).
 * </pre>
 * 
 * This implementation groups the MIDs and only keeps the lease time for the
 * last used MID of the group. This reduces the amount of memory but may take a
 * little longer to use the first MIDs of a group because they freed with the
 * lease of the last MID of the group.
 */
public class GroupedMessageIdTracker implements MessageIdTracker {

	/**
	 * Number of groups.
	 */
	private final int numberOfGroups;
	/**
	 * Size of groups. Number of MIDs per group.
	 */
	private final int sizeOfGroups;
	/**
	 * Minimal MID:
	 */
	private final int min;
	/**
	 * Range of MIDs
	 */
	private final int range;
	/**
	 * Exchange lifetime. Value in nanoseconds.
	 * 
	 * @see ClockUtil#nanoRealtime()
	 */
	private final long exchangeLifetimeNanos;
	/**
	 * Array with end of lease for MID groups. MID divided by
	 * {@link #sizeOfGroups} is used as index. Values in nanoseconds.
	 * 
	 * @see ClockUtil#nanoRealtime()
	 */
	private final long midLease[];
	/**
	 * Current MID.
	 */
	private int currentMID;

	/**
	 * Creates a new MID group based tracker.
	 * 
	 * The following configuration values are used:
	 * <ul>
	 * <li>{@link com.coap.core.network.config.NetworkConfig.Keys#MID_TRACKER_GROUPS}
	 * - determine the group size for the message IDs. Each group is marked as
	 * <em>in use</em>, if a MID within the group is used.</li>
	 * <li>{@link com.coap.core.network.config.NetworkConfig.Keys#EXCHANGE_LIFETIME}
	 * - each group of a message ID returned by <em>getNextMessageId</em> is
	 * marked as <em>in use</em> for this amount of time (ms).</li>
	 * </ul>
	 * 
	 * @param initialMid initial MID
	 * @param minMid minimal MID (inclusive).
	 * @param maxMid maximal MID (exclusive).
	 * @param config configuration
	 * @throws IllegalArgumentException if minMid is not smaller than maxMid or
	 *             initialMid is not in the range of minMid and maxMid
	 */
	public GroupedMessageIdTracker(int initialMid, int minMid, int maxMid, NetworkConfig config) {
		if (minMid >= maxMid) {
			throw new IllegalArgumentException("max. MID " + maxMid + " must be larger than min. MID " + minMid + "!");
		}
		if (initialMid < minMid || maxMid <= initialMid) {
			throw new IllegalArgumentException(
					"initial MID " + initialMid + " must be in range [" + minMid + "-" + maxMid + ")!");
		}
		exchangeLifetimeNanos = TimeUnit.MILLISECONDS.toNanos(config.getLong(NetworkConfig.Keys.EXCHANGE_LIFETIME));
		currentMID = initialMid - minMid;
		this.min = minMid;
		this.range = maxMid - minMid;
		this.numberOfGroups = config.getInt(NetworkConfig.Keys.MID_TRACKER_GROUPS);
		this.sizeOfGroups = (range + numberOfGroups - 1) / numberOfGroups;
		midLease = new long[numberOfGroups];
	}

	/**
	 * Gets the next usable message ID.
	 * 
	 * @return a message ID or {@code -1} if all message IDs are in use
	 *         currently.
	 */
	public int getNextMessageId() {
		final long now = ClockUtil.nanoRealtime();
		synchronized (this) {
			// mask mid to the min-max range
			int mid = (currentMID & 0xffff) % range;
			int index = mid / sizeOfGroups;
			int nextIndex = (index + 1) % numberOfGroups;
			if (midLease[nextIndex] - now < 0) {
				midLease[index] = now + exchangeLifetimeNanos;
				currentMID = mid + 1;
				return mid + min;
			}
		}
		return Message.NONE;
	}

	/**
	 * Get number of MIDs per group.
	 * 
	 * @return size of groups
	 * @see #sizeOfGroups
	 */
	public int getGroupSize() {
		return sizeOfGroups;
	}
}

package com.coap.core.network.deduplication;

import com.coap.core.network.Exchange;
import com.coap.core.network.Exchange.KeyMID;

/**
 * The deduplicator has to detect duplicates. Notice that CONs and NONs can be
 * duplicates.
 */
public interface Deduplicator {

	/**
	 * Starts the deduplicator
	 */
	void start();

	/**
	 * Stops the deduplicator. The deduplicator should NOT clear its state.
	 */
	void stop();

	/**
	 * Checks if the specified key is already associated with a previous
	 * exchange and otherwise associates the key with the exchange specified. 
	 * This method can also be though of as 'put if absent'. This is equivalent 
	 * to
     * <pre>
     *   if (!duplicator.containsKey(key))
     *       return duplicator.put(key, value);
     *   else
     *       return duplicator.get(key);
     * </pre>
     * except that the action is performed atomically.
	 * 
	 * @param key the key
	 * @param exchange the exchange
	 * @return the previous exchange associated with the specified key, or
     *         <tt>null</tt> if there was no mapping for the key.
	 */
	Exchange findPrevious(KeyMID key, Exchange exchange);

	Exchange find(KeyMID key);

	boolean isEmpty();

	int size();

	/**
	 * Clears the state of this deduplicator.
	 */
	void clear();
}

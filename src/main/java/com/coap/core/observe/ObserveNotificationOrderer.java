package com.coap.core.observe;

import com.coap.core.coap.Response;
import com.coap.elements.util.ClockUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The ObservingNotificationOrderer holds the state of an observe relation such
 * as the timeout of the last notification and the current number.
 */
public class ObserveNotificationOrderer {

	/** The counter for observe numbers */
	private final AtomicInteger number = new AtomicInteger();
	
	/** The timestamp of the last response */
	private long nanoTimestamp;
	
	/**
	 * Creates a new notification orderer.
	 */
	public ObserveNotificationOrderer() {
	}

	/**
	 * Creates a new notification orderer for a given notification.
	 * 
	 * @throws NullPointerException, if observe is {@code null}
	 */
	public ObserveNotificationOrderer(Integer observe) {
		if (observe == null) {
			throw new NullPointerException("observe option must not be null!");
		}
		number.set(observe);
		nanoTimestamp = ClockUtil.nanoRealtime();
	}
	
	/**
	 * Return a new observe option number. This method is thread-safe as it
	 * increases the option number atomically.
	 * 
	 * @return a new observe option number
	 */
	public int getNextObserveNumber() {
		int next = number.incrementAndGet();
		while (next >= 1<<24) {
			number.compareAndSet(next, 0);
			next = number.incrementAndGet();
		}
		// assert 0 <= next && next < 1<<24;
		return next;
	}
	
	/**
	 * Returns the current notification number.
	 * @return the current notification number
	 */
	public int getCurrent() {
		return number.get();
	}
	
	/**
	 * Returns true if the specified notification is newer than the current one.
	 * @param response the notification
	 * @return true if the notification is new
	 */
	public synchronized boolean isNew(Response response) {
		
		Integer observe = response.getOptions().getObserve();
		if (observe == null) {
			// this is a final response, e.g., error or proactive cancellation
			return true;
		}
		
		// Multiple responses with different notification numbers might
		// arrive and be processed by different threads. We have to
		// ensure that only the most fresh one is being delivered.
		// We use the notation from the observe draft-08.
		long T1 = nanoTimestamp;
		int V1 = number.get();
		long T2 = ClockUtil.nanoRealtime();
		int V2 = observe;
		if (V1 < V2 && (V2 - V1) < (1L<<23)
				|| V1 > V2 && (V1 - V2) > (1L<<23)
				|| T2 > (T1 + TimeUnit.SECONDS.toNanos(128))) {

			nanoTimestamp = T2;
			number.set(V2);
			return true;
		} else {
			return false;
		}
	}
}

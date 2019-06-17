package com.coap.core.network.deduplication;

import com.coap.core.network.Exchange;
import com.coap.core.network.Exchange.KeyMID;
import com.coap.core.network.config.NetworkConfig;
import com.coap.elements.util.ExecutorsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This deduplicator is probably inferior to the {@link SweepDeduplicator}. This
 * deduplicator holds three hash maps, two of which are always active and one is
 * passive. After an EXCHANGE_LIFECYCLE, the hash maps switch their places by
 * one. When a message arrives, the deduplicator adds it to the two active hash
 * maps. Therefore, it is remembered for at least one lifecycle and at most two.
 * This deduplicator adds most messages to two hash maps but does not need to
 * remove them one-by-one. Instead, it clears all entries of the passive hash
 * map at once.
 */
public class CropRotation implements Deduplicator {

	private final static Logger LOGGER = LoggerFactory.getLogger(CropRotation.class.getCanonicalName());
	private volatile ScheduledFuture<?> jobStatus;

	private final ExchangeMap maps[];
	private volatile int first;
	private volatile int second;

	private final long period;
	private final Rotation rotation;

	/**
	 * Creates a new crop rotation deduplicator for configuration properties.
	 * <p>
	 * Uses the value of the
	 * {@link com.coap.core.network.config.NetworkConfig.Keys#CROP_ROTATION_PERIOD}
	 * param from the given configuration as the waiting period between crop
	 * rotation (in milliseconds).
	 * 
	 * @param config The configuration properties.
	 */
	public CropRotation(NetworkConfig config) {
		this.rotation = new Rotation();
		maps = new ExchangeMap[3];
		maps[0] = new ExchangeMap();
		maps[1] = new ExchangeMap();
		maps[2] = new ExchangeMap();
		first = 0;
		second = 1;
		period = config.getLong(NetworkConfig.Keys.CROP_ROTATION_PERIOD);
	}

	@Override
	public synchronized void start() {
		if (jobStatus == null) {
			jobStatus = ExecutorsUtil.getScheduledExecutor().scheduleAtFixedRate(rotation, period, period,
					TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public synchronized void stop() {
		if (jobStatus != null) {
			jobStatus.cancel(false);
			jobStatus = null;
			clear();
		}
	}

	@Override
	public Exchange findPrevious(KeyMID key, Exchange exchange) {
		int f = first;
		int s = second;
		Exchange prev = maps[f].putIfAbsent(key, exchange);
		if (prev != null || f == s)
			return prev;
		prev = maps[s].putIfAbsent(key, exchange);
		return prev;
	}

	@Override
	public Exchange find(KeyMID key) {
		int f = first;
		int s = second;
		Exchange prev = maps[f].get(key);
		if (prev != null || f == s)
			return prev;
		prev = maps[s].get(key);
		return prev;
	}

	@Override
	public void clear() {
		synchronized (maps) {
			maps[0].clear();
			maps[1].clear();
			maps[2].clear();
		}
	}

	@Override
	public int size() {
		synchronized (maps) {
			return maps[0].size() + maps[1].size() + maps[2].size();
		}
	}

	@Override
	public boolean isEmpty() {
		for (ExchangeMap map : maps) {
			if (!map.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private class Rotation implements Runnable {

		public void run() {
			try {
				rotation();
			} catch (Throwable t) {
				LOGGER.warn("Exception in Crop-Rotation algorithm", t);
			}
		}

		private void rotation() {
			synchronized (maps) {
				int third = first;
				first = second;
				second = (second + 1) % 3;
				maps[third].clear();
			}
		}
	}

	private static class ExchangeMap extends ConcurrentHashMap<KeyMID, Exchange> {

		private static final long serialVersionUID = 1504940670839294042L;
	}
}

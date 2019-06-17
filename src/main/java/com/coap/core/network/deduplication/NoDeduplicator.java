package com.coap.core.network.deduplication;

import com.coap.core.network.Exchange;
import com.coap.core.network.Exchange.KeyMID;

/**
 * This is a dummy implementation that does no deduplication. If a matcher
 * does not want to deduplicate incoming messages, it should use this
 * deduplicator instead of 'null'.
 */
public class NoDeduplicator implements Deduplicator {

	@Override
	public void start() { }

	@Override
	public void stop() { }

	@Override
	public Exchange findPrevious(KeyMID key, Exchange exchange) {
		return null;
	}

	@Override
	public Exchange find(KeyMID key) {
		return null;
	}

	@Override
	public void clear() { }

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public int size() {
		return 0;
	}
}

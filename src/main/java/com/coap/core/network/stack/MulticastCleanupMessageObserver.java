package com.coap.core.network.stack;

import com.coap.core.network.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Cleanup multicast exchange by time to enable request for multiple responses.
 */
public class MulticastCleanupMessageObserver extends CleanupMessageObserver {

	static final Logger LOGGER = LoggerFactory.getLogger(MulticastCleanupMessageObserver.class.getName());

	/**
	 * Scheduler for time based exchange completion.
	 */
	private final ScheduledExecutorService scheduledExecutor;
	/**
	 * Multicast lifetime in milliseconds.
	 */
	private final long multicastLifetime;
	/**
	 * Future for cleanup task.
	 */
	private volatile ScheduledFuture<?> cleanup;

	/**
	 * Create multicast cleanup observer.
	 * 
	 * @param exchange exchange for multicast request
	 * @param scheduledExecutor scheduler for time based cleanup
	 * @param multicastLifetime multicast lifetime in milliseconds.
	 */
	public MulticastCleanupMessageObserver(Exchange exchange, ScheduledExecutorService scheduledExecutor,
										   long multicastLifetime) {
		super(exchange);
		this.scheduledExecutor = scheduledExecutor;
		this.multicastLifetime = multicastLifetime;
	}

	@Override
	public void onSent() {
		cleanup = scheduledExecutor.schedule(new Runnable() {

			@Override
			public void run() {
				exchange.execute(new Runnable() {
					@Override
					public void run() {
						if (exchange.getResponse() == null) {
							exchange.getRequest().setCanceled(true);
						} else {
							exchange.setComplete();
							exchange.getRequest().onComplete();
						}
					}
				});
			}
		}, multicastLifetime, TimeUnit.MILLISECONDS);
	}

	@Override
	protected void complete(final String action) {
		ScheduledFuture<?> cleanup = this.cleanup;
		if (cleanup != null) {
			cleanup.cancel(false);
		}
		super.complete(action);
	}
}

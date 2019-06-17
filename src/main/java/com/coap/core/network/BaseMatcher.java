package com.coap.core.network;

import com.coap.core.coap.*;
import com.coap.core.network.Exchange.Origin;
import com.coap.core.network.config.NetworkConfig;
import com.coap.core.observe.NotificationListener;
import com.coap.core.observe.Observation;
import com.coap.core.observe.ObservationStore;
import com.coap.elements.EndpointContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A base class for implementing Matchers that provides support for using a
 * {@code MessageExchangeStore}.
 */
public abstract class BaseMatcher implements Matcher {

	private static final Logger LOG = LoggerFactory.getLogger(BaseMatcher.class.getName());
	protected final NetworkConfig config;
	protected final ObservationStore observationStore;
	protected final MessageExchangeStore exchangeStore;
	protected final TokenGenerator tokenGenerator;
	protected final Executor executor;
	protected boolean running = false;
	private final NotificationListener notificationListener;

	/**
	 * Creates a new matcher based on configuration values.
	 * 
	 * @param config the configuration to use.
	 * @param notificationListener the callback to invoke for notifications
	 *            received from peers.
	 * @param tokenGenerator token generator to create tokens for observations
	 *            created by the endpoint this matcher is part of.
	 * @param observationStore the object to use for keeping track of
	 *            observations created by the endpoint this matcher is part of.
	 * @param exchangeStore the exchange store to use for keeping track of
	 *            message exchanges with endpoints.
	 * @param executor executor to be used for exchanges.
	 * @throws NullPointerException if one of the parameters is {@code null}.
	 */
	public BaseMatcher( NetworkConfig config,  NotificationListener notificationListener,
			 TokenGenerator tokenGenerator,  ObservationStore observationStore,
			 MessageExchangeStore exchangeStore, Executor executor) {
		if (config == null) {
			throw new NullPointerException("Config must not be null");
		} else if (notificationListener == null) {
			throw new NullPointerException("NotificationListener must not be null");
		} else if (tokenGenerator == null) {
			throw new NullPointerException("TokenGenerator must not be null");
		} else if (exchangeStore == null) {
			throw new NullPointerException("MessageExchangeStore must not be null");
		} else if (observationStore == null) {
			throw new NullPointerException("ObservationStore must not be null");
		} else {
			this.config = config;
			this.notificationListener = notificationListener;
			this.exchangeStore = exchangeStore;
			this.observationStore = observationStore;
			this.tokenGenerator = tokenGenerator;
			this.executor = executor;
		}
	}

	@Override
	public synchronized void start() {
		if (!running) {
			exchangeStore.start();
			observationStore.start();
			running = true;
		}
	}

	@Override
	public synchronized void stop() {
		if (running) {
			exchangeStore.stop();
			observationStore.stop();
			clear();
			running = false;
		}
	}

	/**
	 * This method does nothing.
	 * <p>
	 * Subclasses should override this method in order to clear any internal
	 * state.
	 */
	@Override
	public void clear() {
	}

	/**
	 * Register observe request.
	 * 
	 * Add observe request to the {@link #observationStore} and set a message
	 * observer.
	 * 
	 * @param request observe request.
	 */
	protected final void registerObserve(final Request request) {

		// Ignore follow-up blockwise request
		if (!request.getOptions().hasBlock2() || request.getOptions().getBlock2().getNum() == 0) {
			// add request to the store
			LOG.debug("registering observe request {}", request);
			Token token = request.getToken();
			if (token == null) {
				do {
					token = tokenGenerator.createToken(true);
					request.setToken(token);
				} while (observationStore.putIfAbsent(token, new Observation(request, null)) != null);
			} else {
				observationStore.put(token, new Observation(request, null));
			}
			// Add observer to remove observation, if the request is cancelled,
			// rejected, timed out, or send error is reported
			request.addMessageObserver(new ObservationObserverAdapter(token) {

				@Override
				public void onCancel() {
					remove();
				}

				@Override
				protected void failed() {
					remove();
				}

				@Override
				public void onContextEstablished(EndpointContext endpointContext) {
					observationStore.setContext(token, endpointContext);
				}
			});
		}
	}

	/**
	 * Special matching for notify responses. Check, is a observe is stored in
	 * {@link #observationStore} and if found, recreate a exchange.
	 * 
	 * @param response notify response
	 * @return exchange, if a new one is create of the stored observe
	 *         informations, null, otherwise.
	 */
	protected final Exchange matchNotifyResponse(final Response response) {

		Exchange exchange = null;
		if (!CoAP.ResponseCode.isSuccess(response.getCode()) || response.getOptions().hasObserve()) {
			Token token = response.getToken();
			Observation obs = observationStore.get(token);
			if (obs != null) {
				// there is an observation for the token from the response
				// re-create a corresponding Exchange object for it so
				// that the "upper" layers can correctly process the
				// notification response
				final Request request = obs.getRequest();
				exchange = new Exchange(request, Origin.LOCAL, executor, obs.getContext(), true);
				LOG.debug("re-created exchange from original observe request: {}", request);
				request.addMessageObserver(new ObservationObserverAdapter(token) {

					@Override
					public void onResponse(Response response) {
						try {
							notificationListener.onNotification(request, response);
						} finally {
							if (!response.isNotification()) {
								// Observe response received with no observe
								// option set. It could be that the Client was
								// not able to establish the observe. So remove
								// the observe relation from observation store,
								// which was stored earlier when the request was
								// sent.
								LOG.debug("observation with token {} removed, removing from observation store", token);
								remove();
							}
						}
					}
				});
			}
		}
		return exchange;
	}

	/**
	 * Cancels all pending blockwise requests that have been induced by a
	 * notification we have received indicating a blockwise transfer of the
	 * resource.
	 * 
	 * @param token the token of the observation.
	 */
	@Override
	public void cancelObserve(Token token) {
		// Note: the initial observe exchanges is not longer stored with
		// the original token but a pending blockwise notifies may still
		// have a request with that token.
		boolean found = false;
		for (Exchange exchange : exchangeStore.findByToken(token)) {
			Request request = exchange.getRequest();
			if (request.isObserve()) {
				// cancel only observe requests,
				// not "token" related proactive cancel observe request!
				request.cancel();
				if (!exchange.isNotification()) {
					// Message.cancel() already released the token
					found = true;
				}
				exchange.executeComplete();
			}
		}
		if (!found) {
			// if a exchange was found,
			// the request.cancel() has already removed the observation
			observationStore.remove(token);
		}
	}

	/**
	 * Message observer removing observations. May be shared by multiple (block)
	 * request and will call {@link ObservationStore#remove(Token)} only once.
	 */
	private class ObservationObserverAdapter extends MessageObserverAdapter {

		/**
		 * Flag to suppress multiple observation store remove calls.
		 */
		protected final AtomicBoolean removed = new AtomicBoolean();
		/**
		 * Token to remove.
		 */
		protected final Token token;

		/**
		 * Create observer.
		 * 
		 * @param token token to remove
		 */
		public ObservationObserverAdapter(Token token) {
			this.token = token;
		}

		@Override
		public void onResponse(Response response) {
			Observation observation = observationStore.get(token);
			if (observation != null) {
				if (response.isError() || !response.isNotification()) {
					LOG.debug("observation with token {} not established, removing from observation store", token);
					remove();
				}
			}
		}

		/**
		 * Remove token from observation store. Mostly called once.
		 */
		protected void remove() {
			if (removed.compareAndSet(false, true)) {
				observationStore.remove(token);
			}
		}
	}
}

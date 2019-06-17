package com.coap.core.observe;

/**
 * An observation store that keeps all observations in-memory.
 */
public final class InMemoryObservationStore implements ObservationStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryObservationStore.class.getName());
	private static final Logger HEALTH_LOGGER = LoggerFactory.getLogger(LOGGER.getName() + ".health");
	private final ConcurrentMap<Token, Observation> map = new ConcurrentHashMap<>();
	private volatile boolean enableStatus;
	private final NetworkConfig config;
	private ScheduledFuture<?> statusLogger;

	public InMemoryObservationStore(NetworkConfig config) {
		this.config = config;
	}

	@Override
	public Observation putIfAbsent(Token key, Observation obs) {
		if (key == null) {
			throw new NullPointerException("token must not be null");
		} else if (obs == null) {
			throw new NullPointerException("observation must not be null");
		} else {
			enableStatus = true;
			Observation result = map.putIfAbsent(key, obs);
			if (result == null) {
				LOGGER.debug("added observation for {}", key);
			} else {
				LOGGER.debug("kept observation {} for {}", result, key);
			}
			return result;
		}
	}

	@Override
	public Observation put(Token key, Observation obs) {
		if (key == null) {
			throw new NullPointerException("token must not be null");
		} else if (obs == null) {
			throw new NullPointerException("observation must not be null");
		} else {
			enableStatus = true;
			Observation result = map.put(key, obs);
			if (result == null) {
				LOGGER.debug("added observation for {}", key);
			} else {
				LOGGER.debug("replaced observation {} for {}", result, key);
			}
			return result;
		}
	}

	@Override
	public Observation get(Token token) {
		if (token == null) {
			return null;
		} else {
			Observation obs = map.get(token);
			LOGGER.debug("looking up observation for token {}: {}", token, obs);
			// clone request in order to prevent accumulation of
			// message observers on original request
			return ObservationUtil.shallowClone(obs);
		}
	}

	@Override
	public void remove(Token token) {
		if (token != null) {
			if (map.remove(token) != null) {
				LOGGER.debug("removed observation for token {}", token);
			} else {
				LOGGER.debug("Already removed observation for token {}", token);
			}
		}
	}

	/**
	 * Checks if this store is empty.
	 * 
	 * @return {@code true} if this store does not contain any observations.
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * Gets the number of observations currently held in this store.
	 * 
	 * @return The number of observations.
	 */
	public int getSize() {
		return map.size();
	}

	/**
	 * Removes all observations from this store.
	 */
	public void clear() {
		map.clear();
	}

	@Override
	public void setContext(Token token, final EndpointContext ctx) {

		if (token != null && ctx != null) {
			Observation obs = map.get(token);
			if (obs != null) {
				map.replace(token, obs, new Observation(obs.getRequest(), ctx));
			}
		}
	}

	@Override
	public synchronized void start() {
		int healthStatusInterval = config.getInt(NetworkConfig.Keys.HEALTH_STATUS_INTERVAL,
				NetworkConfigDefaults.DEFAULT_HEALTH_STATUS_INTERVAL); // seconds

		if (healthStatusInterval > 0 && HEALTH_LOGGER.isDebugEnabled()) {
			statusLogger = ExecutorsUtil.getScheduledExecutor().scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					if (enableStatus) {
						HEALTH_LOGGER.debug("{} observes", map.size());
						Iterator<Token> iterator = map.keySet().iterator();
						int max = 5;
						while (iterator.hasNext()) {
							HEALTH_LOGGER.debug("   observe {}", iterator.next());
							--max;
							if (max == 0) {
								break;
							}
						}
					}
				}
			}, healthStatusInterval, healthStatusInterval, TimeUnit.SECONDS);
		}
	}

	@Override
	public synchronized void stop() {
		if (statusLogger != null) {
			statusLogger.cancel(false);
			statusLogger = null;
		}
	}
}

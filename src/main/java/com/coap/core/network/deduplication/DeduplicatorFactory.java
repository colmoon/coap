package com.coap.core.network.deduplication;

import com.coap.core.network.config.NetworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The deduplication factory creates the deduplicator for a {@link Matcher}. If
 * a server wants to use another deduplicator than the three standard
 * deduplicators, it can create its own factory and install it with
 * {@link #setDeduplicatorFactory(DeduplicatorFactory)}.
 */
public class DeduplicatorFactory {

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DeduplicatorFactory.class.getCanonicalName());

	/** The factory. */
	private static DeduplicatorFactory factory;

	/**
	 * Returns the installed deduplicator factory.
	 * @return the deduplicator factory
	 */
	public static synchronized DeduplicatorFactory getDeduplicatorFactory() {

		if (factory == null) {
			factory = new DeduplicatorFactory();
		}
		return factory;
	}

	/**
	 * Installs the specified deduplicator factory.
	 * @param factory the factory
	 */
	public static synchronized void setDeduplicatorFactory(DeduplicatorFactory factory) {
		DeduplicatorFactory.factory = factory;
	}

	/**
	 * Creates a new deduplicator based on the value of the
	 * {@link com.coap.core.network.config.NetworkConfig.Keys#DEDUPLICATOR} configuration property.
	 * 
	 * @param config The configuration properties.
	 * @return The deduplicator to use.
	 */
	public Deduplicator createDeduplicator(final NetworkConfig config) {

		String type = config.getString(NetworkConfig.Keys.DEDUPLICATOR, NetworkConfig.Keys.NO_DEDUPLICATOR);
		switch(type) {
		case NetworkConfig.Keys.DEDUPLICATOR_MARK_AND_SWEEP:
			return new SweepDeduplicator(config);
		case NetworkConfig.Keys.DEDUPLICATOR_CROP_ROTATION:
			return new CropRotation(config);
		case NetworkConfig.Keys.NO_DEDUPLICATOR:
			return new NoDeduplicator();
		default:
			LOGGER.warn("configuration contains unsupported deduplicator type, duplicate detection will be turned off");
			return new NoDeduplicator();
		}
	}
}

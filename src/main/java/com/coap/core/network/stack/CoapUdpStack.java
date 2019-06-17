package com.coap.core.network.stack;

/**
 * The CoAPStack builds up the stack of CoAP layers that process the CoAP
 * protocol.
 * <p>
 * The complete process for incoming and outgoing messages is visualized below.
 * The class <code>CoapStack</code> builds up the part between the Stack Top and
 * Bottom.
 * <hr><blockquote><pre>
 * +--------------------------+
 * | {@link MessageDeliverer}         |
 * +--------------A-----------+
 *                A
 *              * A
 * +------------+-A-----------+
 * |       CoAPEndpoint       |
 * |            v A           |
 * |            v A           |
 * | +----------v-+---------+ |
 * | | Stack Top            | |
 * | +----------------------+ |
 * | | {@link ExchangeCleanupLayer} | |
 * | +----------------------+ |
 * | | {@link ObserveLayer}         | |
 * | +----------------------+ |
 * | | {@link BlockwiseLayer}       | |
 * | +----------------------+ |
 * | | {@link ReliabilityLayer}     | |
 * | +----------------------+ |
 * | | Stack Bottom         | |
 * | +----------+-A---------+ |
 * |            v A           |
 * |          Matcher         |
 * |            v A           |
 * |        Interceptor       |
 * |            v A           |
 * +------------v-A-----------+
 *              v A 
 *              v A 
 * +------------v-+-----------+
 * | {@link Connector}                |
 * +--------------------------+
 * </pre></blockquote><hr>
 */
public class CoapUdpStack extends BaseCoapStack {

	/** The LOGGER. */
	private final static Logger LOGGER = LoggerFactory.getLogger(CoapStack.class.getCanonicalName());

	/**
	 * Creates a new stack for UDP as the transport.
	 * 
	 * @param config The configuration values to use.
	 * @param outbox The adapter for submitting outbound messages to the transport.
	 */
	public CoapUdpStack(final NetworkConfig config, final Outbox outbox) {
		super(outbox);
		Layer layers[] = new Layer[] {
				createExchangeCleanupLayer(config),
				createObserveLayer(config),
				createBlockwiseLayer(config),
				createReliabilityLayer(config)};

		setLayers(layers);
	}

	protected Layer createExchangeCleanupLayer(NetworkConfig config) {
		return new ExchangeCleanupLayer(config);
	}

	protected Layer createObserveLayer(NetworkConfig config) {
		return new ObserveLayer(config);
	}

	protected Layer createBlockwiseLayer(NetworkConfig config) {
		return new BlockwiseLayer(config);
	}

	protected Layer createReliabilityLayer(NetworkConfig config) {
		ReliabilityLayer reliabilityLayer;
		if (config.getBoolean(NetworkConfig.Keys.USE_CONGESTION_CONTROL) == true) {
			reliabilityLayer = CongestionControlLayer.newImplementation(config);
			LOGGER.info("Enabling congestion control: {}", reliabilityLayer.getClass().getSimpleName());
		} else {
			reliabilityLayer = new ReliabilityLayer(config);
		}
		return reliabilityLayer;
	}
}

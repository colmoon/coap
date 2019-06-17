package com.coap.core.network.stack;

import com.coap.core.network.Outbox;
import com.coap.core.network.config.NetworkConfig;

/**
 * The CoapTcpStack builds up the stack of CoAP layers that process the CoAP
 * protocol when running over TCP connection.
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
 * |        CoAPEndpoint      |
 * |            v A           |
 * |            v A           |
 * | +----------v-+---------+ |
 * | | Stack Top            | |
 * | +----------------------+ |
 * | | {@link TcpExchangeCleanupLayer} | |
 * | +----------------------+ |
 * | | {@link TcpObserveLayer}      | |
 * | +----------------------+ |
 * | | {@link BlockwiseLayer}       | |
 * | +----------------------+ |
 * | | {@link TcpAdaptionLayer}     | |
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
public class CoapTcpStack extends BaseCoapStack {

	/**
	 * Creates a new stack using TCP as the transport.
	 * 
	 * @param config The configuration values to use.
	 * @param outbox The adapter for submitting outbound messages to the transport.
	 */
	public CoapTcpStack(final NetworkConfig config, final Outbox outbox) {
		super(outbox);

		Layer layers[] = new Layer[] {
				new TcpExchangeCleanupLayer(),
				new TcpObserveLayer(config),
				new BlockwiseLayer(config),
				new TcpAdaptionLayer() };

		setLayers(layers);

		// make sure the endpoint sets a MessageDeliverer
	}
}

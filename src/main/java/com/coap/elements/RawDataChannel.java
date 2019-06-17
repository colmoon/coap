package com.coap.elements;

/**
 * A processor for messages received from the network layer.
 * 
 * Applications should register an implementation of this interface with
 * a <code>Connector</code> via its {@link Connector#setRawDataReceiver(RawDataChannel)}
 * method in order to get notified about incoming messages.
 * 
 * Applications should use the {@link Connector#send(RawData)} method to send
 * messages to receivers connected via the network.
 */
public interface RawDataChannel {

	/**
	 * Processes a raw message received from the network.
	 * 
	 * It is assumed that an implementation can either derive the message format
	 * by introspection or knows upfront about the message format to expect.
	 * 
	 * An implementation of this method should return quickly in order to improve
	 * message processing throughput. In cases where processing of a message is expected
	 * to take some time, implementations should consider off-loading the processing of the
	 * messages to a separate <code>Thread</code>, e.g. by employing a
	 * <code>java.util.concurrent.ExecutorService</code>.
	 * 
	 * @param raw
	 *            the raw message to process
	 */
	public void receiveData(RawData raw);

}

package com.coap.core.network.serialization;

import com.coap.elements.util.DatagramWriter;

import static com.coap.core.coap.CoAP.MessageFormat.*;

/**
 * The DataSerialized serializes outgoing messages to byte arrays.
 */
public final class UdpDataSerializer extends DataSerializer {

	@Override protected void serializeHeader(final DatagramWriter writer, final MessageHeader header) {
		writer.write(VERSION, VERSION_BITS);
		writer.write(header.getType().value, TYPE_BITS);
		writer.write(header.getToken().length(), TOKEN_LENGTH_BITS);
		writer.write(header.getCode(), CODE_BITS);
		writer.write(header.getMID(), MESSAGE_ID_BITS);
		writer.writeBytes(header.getToken().getBytes());
	}
}

package com.coap.core.network.serialization;

import com.coap.elements.util.DatagramWriter;

import static com.coap.core.coap.CoAP.MessageFormat.*;

/**
 * The DataSerialized serializes outgoing messages to byte arrays based on CoAP TCP/TLS spec:
 * <a href="https://tools.ietf.org/html/draft-ietf-core-coap-tcp-tls"/>
 */
public final class TcpDataSerializer extends DataSerializer {

	@Override protected void serializeHeader(final DatagramWriter writer, final MessageHeader header) {
		// Variable length encoding per: https://tools.ietf.org/html/draft-ietf-core-coap-tcp-tls-02
		if (header.getBodyLength() < 13) {
			writer.write(header.getBodyLength(), LENGTH_NIBBLE_BITS);
			writer.write(header.getToken().length(), TOKEN_LENGTH_BITS);
		} else if (header.getBodyLength() < (1 << 8) + 13) {
			writer.write(13, LENGTH_NIBBLE_BITS);
			writer.write(header.getToken().length(), TOKEN_LENGTH_BITS);
			writer.write(header.getBodyLength() - 13, Byte.SIZE);
		} else if (header.getBodyLength() < (1 << 16) + 269) {
			writer.write(14, LENGTH_NIBBLE_BITS);
			writer.write(header.getToken().length(), TOKEN_LENGTH_BITS);
			writer.write(header.getBodyLength() - 269, 2 * Byte.SIZE);
		} else {
			writer.write(15, LENGTH_NIBBLE_BITS);
			writer.write(header.getToken().length(), TOKEN_LENGTH_BITS);
			writer.write(header.getBodyLength() - 65805, 4 * Byte.SIZE);
		}

		writer.write(header.getCode(), CODE_BITS);
		writer.writeBytes(header.getToken().getBytes());
	}
}

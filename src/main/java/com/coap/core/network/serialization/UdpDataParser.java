package com.coap.core.network.serialization;

import com.coap.core.coap.CoAP;
import com.coap.core.coap.MessageFormatException;
import com.coap.core.coap.Token;
import com.coap.elements.util.DatagramReader;

import static com.coap.core.coap.CoAP.MessageFormat.*;

/**
 * A parser for messages encoded following the standard CoAP encoding.
 */
public final class UdpDataParser extends DataParser {

	@Override
	protected MessageHeader parseHeader(final DatagramReader reader) {
		int version = reader.read(VERSION_BITS);
		assertCorrectVersion(version);
		int type = reader.read(TYPE_BITS);
		int tokenLength = reader.read(TOKEN_LENGTH_BITS);
		assertValidTokenLength(tokenLength);
		int code = reader.read(CODE_BITS);
		int mid = reader.read(MESSAGE_ID_BITS);
		Token token = Token.fromProvider(reader.readBytes(tokenLength));

		return new MessageHeader(version, CoAP.Type.valueOf(type), token, code, mid, 0);
	}

	private void assertCorrectVersion(int version) {
		if (version != CoAP.VERSION) {
			throw new MessageFormatException("Message has invalid version: " + version);
		}
	}
}

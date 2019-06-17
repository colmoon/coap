package com.coap.core.network.serialization;

import com.coap.core.coap.CoAP;
import com.coap.core.coap.Message;
import com.coap.core.coap.Token;
import com.coap.elements.util.DatagramReader;

import static com.coap.core.coap.CoAP.MessageFormat.*;

/**
 * A parser for messages encoded following the encoding defined by the
 * <a href="https://tools.ietf.org/html/draft-ietf-core-coap-tcp-tls-03">CoAP-over-TCP draft</a>.
 */
public final class TcpDataParser extends DataParser {

	@Override
	protected MessageHeader parseHeader(final DatagramReader reader) {
		int len = reader.read(LENGTH_NIBBLE_BITS);
		int tokenLength = reader.read(TOKEN_LENGTH_BITS);
		int lengthSize = 0;
		assertValidTokenLength(tokenLength);

		if (len == 13) {
			lengthSize = 1;
		} else if (len == 14) {
			lengthSize = 2;
		} else if (len == 15) {
			lengthSize = 4;
		}
		reader.readBytes(lengthSize);
		int code = reader.read(CODE_BITS);
		Token token = Token.fromProvider(reader.readBytes(tokenLength));

		// No MID/Type/VERSION in TCP message. Use defaults.
		return new MessageHeader(CoAP.VERSION, CoAP.Type.CON, token, code, Message.NONE, 0);
	}
}

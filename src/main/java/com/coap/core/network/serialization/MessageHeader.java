package com.coap.core.network.serialization;

import com.coap.core.coap.CoAP;
import com.coap.core.coap.Token;

/**
 * Message header common to all messages.
 */
public class MessageHeader {

	private final int version;
	private final CoAP.Type type;
	private final Token token;
	private final int code;
	private final int mid;
	private final int bodyLength;

	MessageHeader(int version, CoAP.Type type, Token token, int code, int mid, int bodyLength) {
		this.version = version;
		this.type = type;
		this.token = token;
		this.code = code;
		this.mid = mid;
		this.bodyLength = bodyLength;
	}

	/** Options + payload marker + payload length. */
	public int getBodyLength() {
		return bodyLength;
	}

	public int getVersion() {
		return version;
	}

	public CoAP.Type getType() {
		return type;
	}

	public Token getToken() {
		return token;
	}

	public int getCode() {
		return code;
	}

	public int getMID() {
		return mid;
	}
}

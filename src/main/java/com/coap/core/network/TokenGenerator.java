package com.coap.core.network;

import com.coap.core.coap.Token;

/**
 * Token generator.
 *
 * Implementations of {@link TokenGenerator} MUST be thread-safe.
 */
public interface TokenGenerator {

	/**
	 * Creates a token for the provided request.
	 * 
	 * Intended to generate tokens with separate scopes for standard- and
	 * observe requests. Due to the potential long term nature of observe
	 * tokens, these tokens are required to be maintained separated and
	 * therefore such token must be generated in way, which ensures, that they
	 * have different values as the other tokens for standard-requests.
	 * 
	 * One idea of implementation would therefore be to set the token length
	 * different or to set bit 0 of byte 0 fix to 0 for standard and 1 for
	 * observe requests.
	 * 
	 * The caller must take care to use only unique tokens. In cases where the
	 * generated token is already in use, it's intended to create a next token
	 * calling this method again.
	 * 
	 * @param longTermScope {@code true} for observe request within the
	 *            long-term scope, {@code false} for normal request with
	 *            short-term scope.
	 * @return the generated token
	 */
	Token createToken(boolean longTermScope);

}

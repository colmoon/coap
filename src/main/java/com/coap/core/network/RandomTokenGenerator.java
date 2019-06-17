package com.coap.core.network;

import com.coap.core.coap.Token;
import com.coap.core.network.config.NetworkConfig;
import com.coap.core.network.config.NetworkConfig.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

/**
 * {@link TokenGenerator} that uses random tokens and set bit 0 of byte
 * according the required scope of the provided request.
 *
 * This implementation is thread-safe.
 */
public class RandomTokenGenerator implements TokenGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(RandomTokenGenerator.class.getName());
	private static final int DEFAULT_TOKEN_LENGTH = 8; // bytes

	private final int tokenSize;
	private final SecureRandom rng;

	/**
	 * Creates a new {@link RandomTokenGenerator}.
	 * 
	 * @param networkConfig used to obtain the configured token size
	 */
	public RandomTokenGenerator(final NetworkConfig networkConfig) {

		if (networkConfig == null) {
			throw new NullPointerException("NetworkConfig must not be null");
		}
		this.rng = new SecureRandom();
		// trigger self-seeding of the PRNG, may "take a while"
		this.rng.nextInt(10);
		this.tokenSize = networkConfig.getInt(Keys.TOKEN_SIZE_LIMIT, DEFAULT_TOKEN_LENGTH);
		LOGGER.info("using tokens of {} bytes in length", this.tokenSize);
	}

	@Override
	public Token createToken(boolean longTermScope) {
		byte[] token = new byte[tokenSize];
		rng.nextBytes(token);
		if (longTermScope) {
			// set bit 0 to 1
			token[0] |= 0x1;
		} else {
			// set bit 0 to 0
			token[0] &= 0xfe;
		}
		return Token.fromProvider(token);
	}
}

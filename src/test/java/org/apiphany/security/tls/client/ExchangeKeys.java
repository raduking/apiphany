package org.apiphany.security.tls.client;

import java.nio.ByteBuffer;

import org.apiphany.lang.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExchangeKeys {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeKeys.class);

    public static final int KEY_LENGTH = 32; // 256-bit AES
    public static final int IV_LENGTH = 4;

	public enum Type {
		AEAD
	}

	private final byte[] clientWriteKey = new byte[KEY_LENGTH];
	private final byte[] serverWriteKey = new byte[KEY_LENGTH];
	private final byte[] clientIV = new byte[IV_LENGTH];
	private final byte[] serverIV = new byte[IV_LENGTH];

	public static ExchangeKeys from(final byte[] keyBlock, final Type type) {
		if (Type.AEAD != type) {
			throw new UnsupportedOperationException("Unsupported exchange key type");
		}
		LOGGER.debug("Key block length: {}", keyBlock.length);
		LOGGER.debug("Key block:\n{}", Hex.dump(keyBlock));

		ExchangeKeys exchangeKeys = new ExchangeKeys();

		ByteBuffer buffer = ByteBuffer.wrap(keyBlock);
		buffer.get(exchangeKeys.clientWriteKey);
		buffer.get(exchangeKeys.serverWriteKey);
		buffer.get(exchangeKeys.clientIV);
		buffer.get(exchangeKeys.serverIV);

		LOGGER.debug("Client write key:\n{}", Hex.dump(exchangeKeys.clientWriteKey));
		LOGGER.debug("Server write key:\n{}", Hex.dump(exchangeKeys.serverWriteKey));
		LOGGER.debug("Client IV:\n{}", Hex.dump(exchangeKeys.clientIV));
		LOGGER.debug("Server IV:\n{}", Hex.dump(exchangeKeys.serverIV));

		return exchangeKeys;
	}

	public byte[] getClientWriteKey() {
		return clientWriteKey;
	}

	public byte[] getServerWriteKey() {
		return serverWriteKey;
	}

	public byte[] getClientIV() {
		return clientIV;
	}

	public byte[] getServerIV() {
		return serverIV;
	}
}

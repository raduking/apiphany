package org.apiphany.security.ssl.client;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExchangeKeys {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeKeys.class);

	public enum Type {
		AHEAD;
	}

	private byte[] clientWriteKey = new byte[16];
	private byte[] serverWriteKey = new byte[16];
	private byte[] clientIV = new byte[4];
	private byte[] serverIV = new byte[4];

	public static ExchangeKeys from(final byte[] keyBlock, final Type type) {
		if (Type.AHEAD != type) {
			throw new UnsupportedOperationException("Unsupported exchange key type");
		}
		LOGGER.debug("Key block length: {}", keyBlock.length);
		LOGGER.debug("Key block: {}", Bytes.hexString(keyBlock));

		ExchangeKeys exchangeKeys = new ExchangeKeys();

		ByteBuffer buffer = ByteBuffer.wrap(keyBlock);
		buffer.get(exchangeKeys.serverWriteKey);
		buffer.get(exchangeKeys.clientWriteKey);
		buffer.get(exchangeKeys.serverIV);
		buffer.get(exchangeKeys.clientIV);

		LOGGER.debug("Client write key: {}", Bytes.hexString(exchangeKeys.clientWriteKey));
		LOGGER.debug("Server write key: {}", Bytes.hexString(exchangeKeys.serverWriteKey));
		LOGGER.debug("Client IV: {}", Bytes.hexString(exchangeKeys.clientIV));
		LOGGER.debug("Server IV: {}", Bytes.hexString(exchangeKeys.serverIV));

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

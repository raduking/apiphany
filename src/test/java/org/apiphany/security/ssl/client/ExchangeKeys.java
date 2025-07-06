package org.apiphany.security.ssl.client;

import java.nio.ByteBuffer;

/**
 * TODO: extract fields into their own classes with length.
 */
public class ExchangeKeys {

	public enum Type {
		AHEAD,
		OTHER;
	}

	private byte[] clientMACKey = new byte[20];
	private byte[] serverMACKey = new byte[20];
	private byte[] clientWriteKey = new byte[16];
	private byte[] serverWriteKey = new byte[16];
	private byte[] clientIV;
	private byte[] serverIV;

	public static ExchangeKeys from(final byte[] keyBlock, final Type type) {
		ExchangeKeys exchangeKeys = new ExchangeKeys();

		ByteBuffer buffer = ByteBuffer.wrap(keyBlock);
		if (Type.AHEAD != type) {
			buffer.get(exchangeKeys.serverMACKey);
			buffer.get(exchangeKeys.clientMACKey);
		}
		buffer.get(exchangeKeys.serverWriteKey);
		buffer.get(exchangeKeys.clientWriteKey);
		if (keyBlock.length > 72) {
			exchangeKeys.serverIV = new byte[16];
			buffer.get(exchangeKeys.serverIV);
			exchangeKeys.clientIV = new byte[16];
			buffer.get(exchangeKeys.clientIV);
		} else if (Type.AHEAD == type) {
			exchangeKeys.serverIV = new byte[4];
			buffer.get(exchangeKeys.serverIV);
			exchangeKeys.clientIV = new byte[4];
			buffer.get(exchangeKeys.clientIV);
		}

		return exchangeKeys;
	}

	public byte[] getClientMACKey() {
		return clientMACKey;
	}

	public byte[] getServerMACKey() {
		return serverMACKey;
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

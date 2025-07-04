package org.apiphany.security.ssl.client;

import java.nio.ByteBuffer;

public class ExchangeKeys {

	private byte[] clientMACKey = new byte[20];
	private byte[] serverMACKey = new byte[20];
	private byte[] clientWriteKey = new byte[16];
	private byte[] serverWriteKey = new byte[16];
	private byte[] clientIV = new byte[16];
	private byte[] serverIV = new byte[16];

	public static ExchangeKeys from(byte[] keyBlock) {
		ExchangeKeys exchangeKeys = new ExchangeKeys();

		ByteBuffer buffer = ByteBuffer.wrap(keyBlock);
		buffer.get(exchangeKeys.clientMACKey);
		buffer.get(exchangeKeys.serverMACKey);
		buffer.get(exchangeKeys.clientWriteKey);
		buffer.get(exchangeKeys.serverWriteKey);
		if (keyBlock.length > 72) {
			buffer.get(exchangeKeys.clientIV);
			buffer.get(exchangeKeys.serverIV);
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

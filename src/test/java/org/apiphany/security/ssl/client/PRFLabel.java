package org.apiphany.security.ssl.client;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apiphany.lang.BinaryRepresentable;

public enum PRFLabel implements BinaryRepresentable {

	MASTER_SECRET("master secret"),
	KEY_EXPANSION("key expansion"),
	CLIENT_FINISHED("client finished"),
	SERVER_FINISHED("server finished"),
	CLIENT_WRITE_KEY("client write key"),
	SERVER_WRITE_KEY("server write key"),
	CLIENT_WRITE_IV("client write iv"),
	SERVER_WRITE_IV("server write iv");

	private final String label;

	PRFLabel(final String label) {
		this.label = Objects.requireNonNull(label);
	}

	@Override
	public byte[] toByteArray() {
		return label.getBytes(StandardCharsets.US_ASCII);
	}

	@Override
	public String toString() {
		return getLabel();
	}

	public String getLabel() {
		return label;
	}

}

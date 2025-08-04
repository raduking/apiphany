package org.apiphany.security.tls;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apiphany.lang.BinaryRepresentable;

/**
 * Represents labels used in TLS Pseudorandom Function (PRF) operations.
 * <p>
 * These labels are used as part of the key derivation process in TLS protocols.
 */
public enum PRFLabel implements BinaryRepresentable {

	/**
	 * Label for deriving the master secret.
	 */
	MASTER_SECRET("master secret"),

	/**
	 * Label for key expansion operations.
	 */
	KEY_EXPANSION("key expansion"),

	/**
	 * Label for client finished message.
	 */
	CLIENT_FINISHED("client finished"),

	/**
	 * Label for server finished message.
	 */
	SERVER_FINISHED("server finished"),

	/**
	 * Label for client write key derivation.
	 */
	CLIENT_WRITE_KEY("client write key"),

	/**
	 * Label for server write key derivation.
	 */
	SERVER_WRITE_KEY("server write key"),

	/**
	 * Label for client write IV derivation.
	 */
	CLIENT_WRITE_IV("client write iv"),

	/**
	 * Label for server write IV derivation.
	 */
	SERVER_WRITE_IV("server write iv");

	/**
	 * The ASCII string representation of the label.
	 */
	private final String label;

	/**
	 * Constructs a PRF label enum constant.
	 *
	 * @param label the ASCII string representation of the label
	 * @throws NullPointerException if label is null
	 */
	PRFLabel(final String label) {
		this.label = Objects.requireNonNull(label);
	}

	/**
	 * Returns the ASCII byte representation of the label.
	 *
	 * @return byte array containing the ASCII bytes of the label
	 */
	@Override
	public byte[] toByteArray() {
		return label.getBytes(StandardCharsets.US_ASCII);
	}

	/**
	 * Returns the string representation of the label.
	 *
	 * @return the label string
	 */
	@Override
	public String toString() {
		return getLabel();
	}

	/**
	 * Returns the label string.
	 *
	 * @return the label string
	 */
	public String getLabel() {
		return label;
	}
}

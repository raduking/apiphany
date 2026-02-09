package org.apiphany.security.ssl;

import java.util.Map;
import java.util.Objects;

import org.morphix.lang.Enums;
import org.morphix.lang.function.ToStringFunction;

/**
 * This enum represents the supported key store types.
 *
 * @author Radu Sebastian LAZIN
 */
public enum KeyStoreType {

	/**
	 * PKCS12 key store type recommended for most use cases.
	 */
	PKCS12("PKCS12"),

	/**
	 * JKS key store type.
	 */
	JKS("JKS");

	/**
	 * The name map for easy from string implementation.
	 */
	private static final Map<String, KeyStoreType> NAME_MAP = Enums.buildNameMap(values(), ToStringFunction.toUpperCase());

	/**
	 * Key store type as string.
	 */
	private final String value;

	/**
	 * Constructor.
	 *
	 * @param value key store type as string
	 */
	KeyStoreType(final String value) {
		this.value = value;
	}

	/**
	 * Returns a {@link KeyStoreType} enum from a {@link String}.
	 *
	 * @param keyStoreType key store type as string
	 * @return a key store type enum
	 */
	public static KeyStoreType fromString(final String keyStoreType) {
		return Enums.fromString(Objects.requireNonNull(keyStoreType, "keyStoreType cannot be null").toUpperCase(), NAME_MAP, values());
	}

	/**
	 * Returns the key store type as string.
	 *
	 * @return key store type as string
	 */
	public String value() {
		return this.value;
	}

	/**
	 * Returns the string representation of the key store type.
	 *
	 * @return string representation of the key store type
	 */
	@Override
	public String toString() {
		return value();
	}
}

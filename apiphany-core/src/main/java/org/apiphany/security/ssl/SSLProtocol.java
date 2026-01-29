package org.apiphany.security.ssl;

import java.util.Map;
import java.util.Objects;

import org.morphix.lang.Enums;

/**
 * Enum representing SSL/TLS protocol versions. Includes both standard protocols and some legacy/deprecated ones.
 *
 * @author Radu Sebastian LAZIN
 */
public enum SSLProtocol {

	/**
	 * TLS 1.3 protocol (RFC 8446).
	 * <p>
	 * Introduced in Java 11, this is the most recent and secure protocol version. Features include:
	 * <ul>
	 * <li>Improved security and performance</li>
	 * <li>1-RTT handshakes (0-RTT with caveats)</li>
	 * <li>Removed obsolete cryptographic algorithms</li>
	 * </ul>
	 * This should be the preferred protocol when available.
	 */
	TLS_1_3("TLSv1.3", (byte) 0x03, (byte) 0x04),

	/**
	 * TLS 1.2 protocol (RFC 5246).
	 * <p>
	 * Currently the most widely supported secure protocol version. When properly configured with modern cipher suites,
	 * provides strong security. Supported by virtually all modern systems.
	 */
	TLS_1_2("TLSv1.2", (byte) 0x03, (byte) 0x03),

	/**
	 * TLS 1.1 protocol (RFC 4346).
	 * <p>
	 * Considered obsolete and potentially vulnerable to attacks:
	 * <ul>
	 * <li>Lacks modern cipher suites</li>
	 * <li>Vulnerable to BEAST attack (CVE-2011-3389)</li>
	 * </ul>
	 * Disabled by default in Java 8u31+ and should not be used.
	 */
	TLS_1_1("TLSv1.1", (byte) 0x03, (byte) 0x02),

	/**
	 * TLS 1.0 protocol (RFC 2246).
	 * <p>
	 * Originally published as SSL 3.1, this is the first TLS version. Considered insecure due to multiple vulnerabilities:
	 * <ul>
	 * <li>POODLE (CVE-2014-3566)</li>
	 * <li>BEAST (CVE-2011-3389)</li>
	 * <li>Lacks modern cryptographic algorithms</li>
	 * </ul>
	 * Disabled by default in Java 8u31+ and should not be used.
	 */
	TLS_1_0("TLSv1", (byte) 0x03, (byte) 0x01),

	/**
	 * SSL 3.0 protocol.
	 * <p>
	 * Completely insecure and deprecated:
	 * <ul>
	 * <li>Vulnerable to POODLE (CVE-2014-3566)</li>
	 * <li>Lacks modern cryptographic features</li>
	 * <li>Disabled by default in Java since 8u31</li>
	 * <li>Removed entirely in Java 11</li>
	 * </ul>
	 * Should never be used under any circumstances.
	 */
	@Deprecated
	SSL_3_0("SSLv3", (byte) 0x03, (byte) 0x00),

	/**
	 * SSL 2.0 protocol (represented by SSLv2Hello for backward compatibility).
	 * <p>
	 * Extremely insecure and deprecated:
	 * <ul>
	 * <li>Vulnerable to DROWN (CVE-2016-0800)</li>
	 * <li>Contains fundamental design flaws</li>
	 * <li>Disabled by default in Java since 7</li>
	 * <li>Removed entirely in Java 8</li>
	 * </ul>
	 * Note: This is actually the SSLv2Hello pseudo-protocol used for version negotiation, not the actual SSLv2 protocol.
	 */
	@Deprecated
	SSL_2_0("SSLv2Hello", (byte) 0x02, (byte) 0x00);

	/**
	 * The master secret length in bytes for TLS 1.2 (value: 48).
	 */
	public static final int TLS_1_2_MASTER_SECRET_LENGTH = 48;

	/**
	 * The name map for easy from string implementation.
	 */
	private static final Map<String, SSLProtocol> NAME_MAP = Enums.buildNameMap(values());

	/**
	 * The version map for easy from string implementation.
	 */
	private static final Map<Short, SSLProtocol> VERSION_MAP = Enums.buildNameMap(values(), SSLProtocol::handshakeVersion);

	/**
	 * The {@link String} value.
	 */
	private final String value;

	/**
	 * The SSL handshake version as {@code short}.
	 */
	private final short handshakeVersion;

	/**
	 * The major version byte.
	 */
	private final byte majorVersion;

	/**
	 * The minor version byte.
	 */
	private final byte minorVersion;

	/**
	 * Constructs an enumeration.
	 *
	 * @param value string value
	 */
	SSLProtocol(final String value, final byte majorVersion, final byte minorVersion) {
		this.value = value;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.handshakeVersion = (short) (((short) (majorVersion << 8)) + (minorVersion & 0xFF));
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return value();
	}

	/**
	 * Returns the string value.
	 *
	 * @return the string value
	 */
	public String value() {
		return value;
	}

	/**
	 * Returns the SSL handshake version for this protocol.
	 *
	 * @return the SSL handshake version for this protocol
	 */
	public short handshakeVersion() {
		return handshakeVersion;
	}

	/**
	 * Returns the major version byte.
	 *
	 * @return the major version byte
	 */
	public byte majorVersion() {
		return majorVersion;
	}

	/**
	 * Returns the minor version byte.
	 *
	 * @return the minor version byte
	 */
	public byte minorVersion() {
		return minorVersion;
	}

	/**
	 * Returns a {@link SSLProtocol} enum from a {@link String}.
	 *
	 * @param value the SSL protocol as string
	 * @return a SSL protocol enum
	 */
	public static SSLProtocol fromString(final String value) {
		return Enums.fromString(Objects.requireNonNull(value), NAME_MAP, values());
	}

	/**
	 * Returns a {@link SSLProtocol} enum from a {@code short}.
	 *
	 * @param version the SSL protocol as short
	 * @return a SSL protocol enum
	 */
	public static SSLProtocol fromVersion(final short version) {
		return Enums.from(version, VERSION_MAP, values());
	}
}

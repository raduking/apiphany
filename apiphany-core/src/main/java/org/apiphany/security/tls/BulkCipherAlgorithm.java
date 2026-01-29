package org.apiphany.security.tls;

/**
 * Enumeration of supported bulk cipher algorithms.
 *
 * @author Radu Sebastian LAZIN
 */
public enum BulkCipherAlgorithm {

	/**
	 * Advanced Encryption Standard (AES) algorithm.
	 */
	AES("AES"),

	/**
	 * ChaCha20-Poly1305 algorithm.
	 */
	CHACHA20_POLY1305("ChaCha20-Poly1305"),

	/**
	 * Triple Data Encryption Standard (3DES) algorithm.
	 */
	DES_EDE("DESede"),

	/**
	 * RC4 stream cipher algorithm.
	 */
	RC4("RC4"),

	/**
	 * GOST 28147-89 algorithm.
	 */
	GOST_28147("GOST28147"),

	/**
	 * Kuznyechik algorithm.
	 */
	KUZNYECHIK("Kuznyechik"),

	/**
	 * No encryption.
	 */
	NONE("NONE");

	/**
	 * The JCA name of the algorithm.
	 */
	private final String jcaName;

	/**
	 * Constructs a {@link BulkCipherAlgorithm} with the specified JCA name.
	 *
	 * @param jcaName the JCA name of the algorithm
	 */
	BulkCipherAlgorithm(final String jcaName) {
		this.jcaName = jcaName;
	}

	/**
	 * Returns the JCA name of the algorithm.
	 *
	 * @return the JCA name
	 */
	public String jcaName() {
		return jcaName;
	}

	/**
	 * Returns the string representation of the algorithm.
	 *
	 * @return the JCA name as string
	 */
	@Override
	public String toString() {
		return jcaName();
	}
}

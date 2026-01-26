package org.apiphany.security.keys;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.KeySpec;

/**
 * Marker interface for key-related classes.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Keys {

	/**
	 * Initializes and returns a {@link KeyFactory} for the specified algorithm.
	 *
	 * @param algorithm the algorithm name
	 * @return the initialized {@link KeyFactory}
	 * @throws SecurityException if the algorithm is not supported
	 */
	static KeyFactory getKeyFactory(final String algorithm) {
		try {
			return KeyFactory.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new SecurityException("Error initializing " + algorithm + " KeyFactory", e);
		}
	}

	/**
	 * Generates a new key pair for the specified algorithm.
	 *
	 * @param algorithm the algorithm name
	 * @return the generated {@link KeyPair}
	 * @throws SecurityException if key pair generation fails
	 */
	static KeyPair generateKeyPair(final String algorithm) {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
			return keyPairGenerator.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new SecurityException("Error generating key pair", e);
		}
	}

	/**
	 * Generates a public key from the given {@link KeySpec} using the provided {@link KeyFactory}.
	 *
	 * @param keyFactory the {@link KeyFactory} to use
	 * @param keySpec the {@link KeySpec} defining the public key
	 * @return the generated {@link PublicKey}
	 * @throws SecurityException if public key generation fails
	 */
	static PublicKey generatePublicKey(final KeyFactory keyFactory, final KeySpec keySpec) {
		try {
			return keyFactory.generatePublic(keySpec);
		} catch (Exception e) {
			throw new SecurityException("Error generating public key", e);
		}
	}
}

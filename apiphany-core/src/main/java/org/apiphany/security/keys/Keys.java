package org.apiphany.security.keys;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;

import javax.crypto.KeyAgreement;

/**
 * Marker interface for key-related classes and provides utility methods for key management.
 * <p>
 * Methods in this class catches checked exceptions and re-throws them as {@link SecurityException}.
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
		} catch (Exception e) {
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
		} catch (Exception e) {
			throw new SecurityException("Error generating key pair", e);
		}
	}

	/**
	 * Generates a shared secret using the specified algorithm and key pair.
	 *
	 * @param algorithm the algorithm name
	 * @param keyPair the {@link KeyPair}
	 * @return the generated shared secret as a byte array
	 * @throws SecurityException if shared secret generation fails
	 */
	static byte[] generateSecret(final String algorithm, final KeyPair keyPair) {
		return generateSecret(algorithm, keyPair.getPublic(), keyPair.getPrivate());
	}

	/**
	 * Generates a shared secret using the specified algorithm, public key, and private key.
	 *
	 * @param algorithm the algorithm name
	 * @param publicKey the {@link PublicKey}
	 * @param privateKey the {@link PrivateKey}
	 * @return the generated shared secret as a byte array
	 * @throws SecurityException if shared secret generation fails
	 */
	static byte[] generateSecret(final String algorithm, final PublicKey publicKey, final PrivateKey privateKey) {
		try {
			KeyAgreement ka = KeyAgreement.getInstance(algorithm);
			ka.init(privateKey);
			ka.doPhase(publicKey, true);
			return ka.generateSecret();
		} catch (Exception e) {
			throw new SecurityException("Error generating shared secret", e);
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

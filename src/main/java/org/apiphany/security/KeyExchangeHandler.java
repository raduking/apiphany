package org.apiphany.security;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.apiphany.io.BytesOrder;

/**
 * Handles cryptographic key operations for key exchange protocols, including key generation, conversion between byte
 * representations, shared secret computation, and key verification.
 *
 * @author Radu Sebastian LAZIN
 */
public interface KeyExchangeHandler {

	/**
	 * Computes a shared secret using the provided private and public keys. This is typically used for key agreement
	 * protocols like ECDH.
	 *
	 * @param privateKey the private key from one party
	 * @param publicKey the public key from the other party
	 * @return the computed shared secret as a byte array
	 */
	byte[] getSharedSecret(final PrivateKey privateKey, final PublicKey publicKey);

	/**
	 * Generates a new key pair suitable for the key exchange algorithm.
	 *
	 * @return a newly generated KeyPair containing both public and private keys
	 */
	KeyPair generateKeyPair();

	/**
	 * Reconstructs a PublicKey object from its byte array representation.
	 *
	 * @param publicKeyBytes the byte array representation of the public key
	 * @param bytesOrder the byte order of the input bytes
	 * @return the reconstructed PublicKey object
	 */
	PublicKey publicKeyFrom(final byte[] publicKeyBytes, BytesOrder bytesOrder);

	/**
	 * Converts a PublicKey to its byte array representation.
	 *
	 * @param publicKey the PublicKey to convert
	 * @param bytesOrder the desired byte order for the output
	 * @return the byte array representation of the public key
	 */
	byte[] toByteArray(final PublicKey publicKey, BytesOrder bytesOrder);

	/**
	 * Converts a PrivateKey to its byte array representation.
	 *
	 * @param privateKey the PrivateKey to convert
	 * @param bytesOrder the desired byte order for the output
	 * @return the byte array representation of the private key
	 */
	byte[] toByteArray(final PrivateKey privateKey, BytesOrder bytesOrder);

	/**
	 * Verifies that a byte array representation matches the given PublicKey.
	 *
	 * @param publicKeyBytes the byte array to verify
	 * @param bytesOrder the byte order of the input bytes
	 * @param publicKey the PublicKey to compare against
	 * @return true if the byte array represents the same public key, false otherwise
	 */
	boolean verifyKeyMatch(final byte[] publicKeyBytes, BytesOrder bytesOrder, final PublicKey publicKey);

}

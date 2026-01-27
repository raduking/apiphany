package org.apiphany.security.keys;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.XECPrivateKey;
import java.security.interfaces.XECPublicKey;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPublicKeySpec;
import java.util.Arrays;

import org.apiphany.io.BytesOrder;
import org.apiphany.lang.Bytes;

/**
 * Handler for X25519 key exchange operations.
 *
 * @author Radu Sebastian LAZIN
 */
public class X25519Keys implements KeyExchangeHandler {

	/**
	 * Canonical instance of X25519Keys.
	 */
	public static final X25519Keys INSTANCE = new X25519Keys();

	/**
	 * The standard name for X25519 algorithm in Java Cryptography Architecture.
	 */
	public static final String ALGORITHM = "XDH";

	/**
	 * The NamedParameterSpec for X25519 curve.
	 */
	public static final NamedParameterSpec CURVE = NamedParameterSpec.X25519;

	/**
	 * The size of X25519 public keys in bytes.
	 */
	public static final int PUBLIC_KEY_SIZE = 32;

	/**
	 * The KeyFactory instance for X25519 keys.
	 */
	private final KeyFactory keyFactory;

	/**
	 * Constructs a new X25519Keys handler.
	 */
	public X25519Keys() {
		this.keyFactory = Keys.getKeyFactory(ALGORITHM);
	}

	/**
	 * Returns the KeyFactory instance.
	 *
	 * @return the {@link KeyFactory}
	 */
	protected KeyFactory getKeyFactory() {
		return keyFactory;
	}

	/**
	 * Generates a new X25519 key pair.
	 *
	 * @return the generated {@link KeyPair}
	 * @throws SecurityException if key pair generation fails
	 * @see KeyExchangeHandler#generateKeyPair()
	 */
	@Override
	public KeyPair generateKeyPair() {
		return Keys.generateKeyPair(ALGORITHM);
	}

	/**
	 * Converts a byte array to a {@link PublicKey} object.
	 *
	 * @param publicKeyBytes the byte array representing the public key
	 * @param bytesOrder the byte order of the input byte array
	 * @return the corresponding {@link PublicKey} object
	 * @see KeyExchangeHandler#publicKeyFrom(byte[], BytesOrder)
	 */
	@Override
	public PublicKey publicKeyFrom(final byte[] publicKeyBytes, final BytesOrder bytesOrder) {
		return switch (bytesOrder) {
			case LITTLE_ENDIAN -> fromLittleEndian(publicKeyBytes);
			case BIG_ENDIAN -> fromBigEndian(publicKeyBytes);
		};
	}

	/**
	 * Converts a little-endian byte array to a {@link PublicKey} object.
	 *
	 * @param publicKeyBytes the little-endian byte array representing the public key
	 * @return the corresponding {@link PublicKey} object
	 */
	public PublicKey fromLittleEndian(final byte[] publicKeyBytes) {
		byte[] littleEndianBytes = Arrays.copyOf(publicKeyBytes, publicKeyBytes.length);
		return fromBigEndian(Bytes.reverse(littleEndianBytes));
	}

	/**
	 * Converts a big-endian byte array to a {@link PublicKey} object.
	 *
	 * @param publicKeyBytes the big-endian byte array representing the public key
	 * @return the corresponding {@link PublicKey} object
	 */
	public PublicKey fromBigEndian(final byte[] publicKeyBytes) {
		BigInteger u = new BigInteger(1, publicKeyBytes);
		XECPublicKeySpec pubKeySpec = new XECPublicKeySpec(CURVE, u);
		return Keys.generatePublicKey(keyFactory, pubKeySpec);
	}

	/**
	 * Generates a shared secret using the provided private and public keys.
	 *
	 * @param privateKey the private key
	 * @param publicKey the public key
	 * @return the generated shared secret as a byte array
	 * @throws SecurityException if the shared secret generation fails
	 * @see KeyExchangeHandler#getSharedSecret(PrivateKey, PublicKey)
	 */
	@Override
	public byte[] getSharedSecret(final PrivateKey privateKey, final PublicKey publicKey) {
		// X25519 is only used with ECDHE
		return Keys.generateSecret(ALGORITHM, publicKey, privateKey);
	}

	/**
	 * Converts a {@link PublicKey} object to a byte array.
	 *
	 * @param publicKey the {@link PublicKey} object
	 * @param bytesOrder the desired byte order of the output byte array
	 * @return the corresponding byte array representing the public key
	 * @see KeyExchangeHandler#toByteArray(PublicKey, BytesOrder)
	 */
	@Override
	public byte[] toByteArray(final PublicKey publicKey, final BytesOrder bytesOrder) {
		return switch (bytesOrder) {
			case LITTLE_ENDIAN -> toByteArrayLittleEndian(publicKey);
			case BIG_ENDIAN -> toByteArrayBigEndian(publicKey);
		};
	}

	/**
	 * Converts a {@link PublicKey} object to a big-endian byte array.
	 *
	 * @param publicKey the {@link PublicKey} object
	 * @return the corresponding big-endian byte array representing the public key
	 */
	public byte[] toByteArrayBigEndian(final PublicKey publicKey) {
		BigInteger u = ((XECPublicKey) publicKey).getU();
		byte[] bigEndian = u.toByteArray();

		byte[] beNormalized = new byte[PUBLIC_KEY_SIZE];
		if (bigEndian.length > PUBLIC_KEY_SIZE) {
			// strip sign byte if present
			if (bigEndian.length == PUBLIC_KEY_SIZE + 1 && bigEndian[0] == 0x00) {
				System.arraycopy(bigEndian, 1, beNormalized, 0, PUBLIC_KEY_SIZE);
			} else {
				throw new SecurityException("Error converting public key to big-endian byte array,"
						+ " bigInteger too large: " + bigEndian.length);
			}
		} else {
			System.arraycopy(bigEndian, 0, beNormalized, PUBLIC_KEY_SIZE - bigEndian.length, bigEndian.length);
		}
		return beNormalized;
	}

	/**
	 * Converts a {@link PublicKey} object to a little-endian byte array.
	 *
	 * @param publicKey the {@link PublicKey} object
	 * @return the corresponding little-endian byte array representing the public key
	 */
	public byte[] toByteArrayLittleEndian(final PublicKey publicKey) {
		return Bytes.reverse(toByteArrayBigEndian(publicKey));
	}

	/**
	 * Converts a {@link PrivateKey} object to a byte array.
	 *
	 * @param privateKey the {@link PrivateKey} object
	 * @param bytesOrder the desired byte order of the output byte array
	 * @return the corresponding byte array representing the private key
	 * @see KeyExchangeHandler#toByteArray(PrivateKey, BytesOrder)
	 */
	@Override
	public byte[] toByteArray(final PrivateKey privateKey, final BytesOrder bytesOrder) {
		return switch (bytesOrder) {
			case LITTLE_ENDIAN -> toByteArrayLittleEndian(privateKey);
			case BIG_ENDIAN -> toByteArrayBigEndian(privateKey);
		};
	}

	/**
	 * Converts a {@link PrivateKey} object to a little-endian byte array.
	 *
	 * @param privateKey the {@link PrivateKey} object
	 * @return the corresponding little-endian byte array representing the private key
	 */
	public byte[] toByteArrayLittleEndian(final PrivateKey privateKey) {
		return ((XECPrivateKey) privateKey).getScalar().orElseThrow();
	}

	/**
	 * Converts a {@link PrivateKey} object to a big-endian byte array.
	 *
	 * @param privateKey the {@link PrivateKey} object
	 * @return the corresponding big-endian byte array representing the private key
	 */
	public byte[] toByteArrayBigEndian(final PrivateKey privateKey) {
		return Bytes.reverse(toByteArrayLittleEndian(privateKey));
	}

	/**
	 * Verifies if the given byte array matches the provided {@link PublicKey}.
	 *
	 * @param key the byte array representing the public key
	 * @param bytesOrder the byte order of the input byte array
	 * @param publicKey the {@link PublicKey} object to compare against
	 * @return true if the byte array matches the public key, false otherwise
	 * @see KeyExchangeHandler#verifyKeyMatch(byte[], BytesOrder, PublicKey)
	 */
	@Override
	public boolean verifyKeyMatch(final byte[] key, final BytesOrder bytesOrder, final PublicKey publicKey) {
		byte[] publicKeyBytes = toByteArray(publicKey, bytesOrder);
		return Arrays.equals(key, publicKeyBytes);
	}
}

package org.apiphany.utils.security;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.XECPrivateKey;
import java.security.interfaces.XECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPublicKeySpec;
import java.util.Arrays;

import javax.crypto.KeyAgreement;

import org.apiphany.io.BytesOrder;
import org.apiphany.lang.Bytes;
import org.apiphany.security.KeyExchangeHandler;

public class X25519Keys implements KeyExchangeHandler {

	public static final String ALGORITHM = "XDH";

	private static final int PUBLIC_KEY_SIZE = 32;

	private final String algorithm;
	private final KeyFactory keyFactory;

	public X25519Keys(final String algorithm) {
		this.algorithm = algorithm;
		try {
			this.keyFactory = KeyFactory.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new SecurityException(e);
		}
	}

	public X25519Keys() {
		this(ALGORITHM);
	}

	@Override
	public KeyPair generateKeyPair() {
		try {
			return generateKeyPair(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new SecurityException(e);
		}
	}

	public static KeyPair generateKeyPair(final String algorithm) throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
		return keyPairGenerator.generateKeyPair();
	}

	@Override
	public PublicKey publicKeyFrom(final byte[] publicKeyBytes, final BytesOrder bytesOrder) {
		try {
			return switch (bytesOrder) {
				case LITTLE_ENDIAN -> fromLittleEndian(publicKeyBytes);
				case BIG_ENDIAN -> fromBigEndian(publicKeyBytes);
			};
		} catch (InvalidKeySpecException e) {
			throw new SecurityException(e);
		}
	}

	public PublicKey fromLittleEndian(final byte[] publicKeyBytes) throws InvalidKeySpecException {
		byte[] littleEndianBytes = Arrays.copyOf(publicKeyBytes, publicKeyBytes.length);
		return fromBigEndian(Bytes.reverse(littleEndianBytes));
	}

	public PublicKey fromBigEndian(final byte[] publicKeyBytes) throws InvalidKeySpecException {
		BigInteger u = new BigInteger(1, publicKeyBytes);
		XECPublicKeySpec pubKeySpec = new XECPublicKeySpec(NamedParameterSpec.X25519, u);
		return keyFactory.generatePublic(pubKeySpec);
	}

	@Override
	public byte[] getSharedSecret(final PrivateKey privateKey, final PublicKey publicKey) {
		// X25519 is only used with ECDHE
		try {
			KeyAgreement ka = KeyAgreement.getInstance(algorithm);
			ka.init(privateKey);
			ka.doPhase(publicKey, true);
			return ka.generateSecret();
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			throw new SecurityException(e);
		}
	}

	@Override
	public byte[] toByteArray(final PublicKey publicKey, final BytesOrder bytesOrder) {
		return switch (bytesOrder) {
			case LITTLE_ENDIAN -> toByteArrayLittleEndian(publicKey);
			case BIG_ENDIAN -> toByteArrayBigEndian(publicKey);
		};
	}

	public byte[] toByteArrayBigEndian(final PublicKey publicKey) {
		BigInteger u = ((XECPublicKey) publicKey).getU();
		byte[] bigEndian = u.toByteArray();

		byte[] beNormalized = new byte[PUBLIC_KEY_SIZE];
		if (bigEndian.length > PUBLIC_KEY_SIZE) {
			// strip sign byte if present
			if (bigEndian.length == PUBLIC_KEY_SIZE + 1 && bigEndian[0] == 0x00) {
				System.arraycopy(bigEndian, 1, beNormalized, 0, PUBLIC_KEY_SIZE);
			} else {
				throw new IllegalArgumentException("BigInteger too large: " + bigEndian.length);
			}
		} else {
			System.arraycopy(bigEndian, 0, beNormalized, PUBLIC_KEY_SIZE - bigEndian.length, bigEndian.length);
		}
		return beNormalized;
	}

	public byte[] toByteArrayLittleEndian(final PublicKey publicKey) {
		return Bytes.reverse(toByteArrayBigEndian(publicKey));
	}

	@Override
	public byte[] toByteArray(final PrivateKey privateKey, final BytesOrder bytesOrder) {
		return switch (bytesOrder) {
			case LITTLE_ENDIAN -> toByteArrayLittleEndian(privateKey);
			case BIG_ENDIAN -> toByteArrayBigEndian(privateKey);
		};
	}

	public byte[] toByteArrayLittleEndian(final PrivateKey privateKey) {
		return ((XECPrivateKey) privateKey).getScalar().orElseThrow();
	}

	public byte[] toByteArrayBigEndian(final PrivateKey privateKey) {
		return Bytes.reverse(toByteArrayLittleEndian(privateKey));
	}

	@Override
	public boolean verifyKeyMatch(final byte[] key, final BytesOrder bytesOrder, final PublicKey publicKey) {
		byte[] publicKeyBytes = toByteArray(publicKey, bytesOrder);
		return Arrays.equals(key, publicKeyBytes);
	}
}

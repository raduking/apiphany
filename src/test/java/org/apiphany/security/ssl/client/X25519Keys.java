package org.apiphany.security.ssl.client;

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

public class X25519Keys {

	private static final String ALGORITHM = "XDH";
	private static final int BYTES = 32;

	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
		return keyPairGenerator.generateKeyPair();
	}

	public static PublicKey getPublicKeyBE(final byte[] publicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
		BigInteger u = new BigInteger(1, publicKeyBytes); // '1' for positive number
		XECPublicKeySpec pubKeySpec = new XECPublicKeySpec(NamedParameterSpec.X25519, u);
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		return keyFactory.generatePublic(pubKeySpec);
	}

	public static PublicKey getPublicKeyLE(final byte[] lePublicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] beBytes = Arrays.copyOf(lePublicKeyBytes, lePublicKeyBytes.length);
		Bytes.reverse(beBytes);

		BigInteger u = new BigInteger(1, beBytes);
		XECPublicKeySpec pubKeySpec = new XECPublicKeySpec(NamedParameterSpec.X25519, u);
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		return keyFactory.generatePublic(pubKeySpec);
	}

	public static byte[] getECDHESharedSecret(final PrivateKey privateKey, final PublicKey publicKey)
			throws NoSuchAlgorithmException, InvalidKeyException {
		KeyAgreement ka = KeyAgreement.getInstance(ALGORITHM);
		ka.init(privateKey);
		ka.doPhase(publicKey, true);
		return ka.generateSecret();
	}

	public static byte[] toRawByteArray(final PublicKey publicKey) {
		BigInteger u = ((XECPublicKey) publicKey).getU();
		byte[] bigEndian = u.toByteArray();

		byte[] beNormalized = new byte[BYTES];
		if (bigEndian.length > BYTES) {
			// Strip sign byte if present
			if (bigEndian.length == BYTES + 1 && bigEndian[0] == 0x00) {
				System.arraycopy(bigEndian, 1, beNormalized, 0, BYTES);
			} else {
				throw new IllegalArgumentException("BigInteger too large: " + bigEndian.length);
			}
		} else {
			System.arraycopy(bigEndian, 0, beNormalized, BYTES - bigEndian.length, bigEndian.length);
		}
		Bytes.reverse(beNormalized);
		return beNormalized;
	}

	public static byte[] toRawByteArray(final PrivateKey privateKey) {
		return ((XECPrivateKey) privateKey).getScalar().orElseThrow();
	}

	public static boolean verifyKeyMatch(final byte[] littleEndianKey, final PublicKey publicKey) {
		byte[] beNormalized = toRawByteArray(publicKey);
		return Arrays.equals(littleEndianKey, beNormalized);
	}
}

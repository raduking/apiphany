package org.apiphany.security.ssl.client;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.XECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPublicKeySpec;

import javax.crypto.KeyAgreement;

public class X25519KeyGeneration {

	private static final String ALGORITHM = "XDH";

	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
		return keyPairGenerator.generateKeyPair();
	}

	public static PublicKey getPublicKey(byte[] publicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
		BigInteger u = new BigInteger(1, publicKeyBytes); // '1' for positive number
		XECPublicKeySpec pubKeySpec = new XECPublicKeySpec(NamedParameterSpec.X25519, u);
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		return keyFactory.generatePublic(pubKeySpec);
	}

	public static byte[] getSharedSecret(PrivateKey privateKey, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException {
		KeyAgreement ka = KeyAgreement.getInstance(ALGORITHM);
		ka.init(privateKey);
		ka.doPhase(publicKey, true);
		return ka.generateSecret();
	}

	public static byte[] getBytes(PublicKey publicKey) {
		BigInteger uCoord = ((XECPublicKey) publicKey).getU();
		byte[] unsigned = uCoord.toByteArray();
		byte[] publicKeyBytes = new byte[32];

		int offset = Math.max(0, unsigned.length - 32);
		int length = Math.min(unsigned.length, 32);
		System.arraycopy(unsigned, offset, publicKeyBytes, 32 - length, length);

		return publicKeyBytes;
	}
}

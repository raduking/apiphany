package org.apiphany.security.ssl.client;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

import javax.crypto.KeyAgreement;

public class X25519Keys {

	private static final String ALGORITHM = "XDH";
	private static final int BYTES = 32;

	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
		return keyPairGenerator.generateKeyPair();
	}

	public static PublicKey getPublicKey(final byte[] publicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
		BigInteger u = new BigInteger(1, publicKeyBytes); // '1' for positive number
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

	public static byte[] toRawByteArrayV1(final PublicKey publicKey) {
		BigInteger uCoord = ((XECPublicKey) publicKey).getU();
		byte[] unsigned = uCoord.toByteArray();
		byte[] publicKeyBytes = new byte[BYTES];

		int offset = Math.max(0, unsigned.length - BYTES);
		int length = Math.min(unsigned.length, BYTES);
		System.arraycopy(unsigned, offset, publicKeyBytes, BYTES - length, length);

		// X25519 often uses little endian and the bytes will be reversed
		return publicKeyBytes;
	}

	public static byte[] toRawByteArrayV2(final PublicKey publicKey) {
		BigInteger uCoord = ((XECPublicKey) publicKey).getU();
		byte[] unsigned = uCoord.toByteArray();
		byte[] result = new byte[32];

		int offset = unsigned.length > BYTES ? 1 : 0;
		int length = Math.min(unsigned.length - offset, BYTES);

		// to reverse the order
		ByteBuffer.wrap(unsigned, offset, length)
				.order(ByteOrder.LITTLE_ENDIAN)
				.get(result, 0, length);

		return result;
	}

	public static byte[] toRawByteArray(final PrivateKey privateKey) {
		return ((XECPrivateKey) privateKey).getScalar().orElseThrow();
	}
}

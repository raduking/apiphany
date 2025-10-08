package org.apiphany.security;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PSSParameterSpec;

import org.morphix.reflection.Constructors;

/**
 * Utility for signing and verifying data using {@link JwsAlgorithm}.
 * <p>
 * Supports RSA (PKCS#1), RSA-PSS, ECDSA, and EdDSA algorithms as defined in JWS/JCA.
 *
 * @author Radu Sebastian LAZIN
 */
public final class Signer {

	/**
	 * Hide constructor.
	 */
	private Signer() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Signs the given data using the specified private key and JWS algorithm.
	 *
	 * @param privateKey the private key to sign with
	 * @param jwsAlgorithm the JWS algorithm to use
	 * @param data the data to sign
	 * @return the signature bytes
	 * @throws SecurityException if signing fails
	 */
	public static byte[] sign(final PrivateKey privateKey, final JwsAlgorithm jwsAlgorithm, final byte[] data) {
		try {
			Signature signature = getSignature(jwsAlgorithm);
			signature.initSign(privateKey);
			signature.update(data);
			return signature.sign();
		} catch (GeneralSecurityException e) {
			throw new SecurityException("Signing failed with " + jwsAlgorithm, e);
		}
	}

	/**
	 * Verifies a signature over the given data using the specified public key and JWS algorithm.
	 *
	 * @param publicKey the public key to verify with
	 * @param jwsAlgorithm the JWS algorithm to use
	 * @param data the original data
	 * @param signatureBytes the signature bytes to verify
	 * @return {@code true} if the signature is valid, {@code false} otherwise
	 * @throws SecurityException if verification fails
	 */
	public static boolean verify(final PublicKey publicKey, final JwsAlgorithm jwsAlgorithm, final byte[] data, final byte[] signatureBytes) {
		try {
			Signature signature = getSignature(jwsAlgorithm);
			signature.initVerify(publicKey);
			signature.update(data);
			return signature.verify(signatureBytes);
		} catch (GeneralSecurityException e) {
			throw new SecurityException("Signature verification failed with " + jwsAlgorithm, e);
		}
	}

	/**
	 * Creates a configured {@link Signature} instance for the given JWS algorithm.
	 * <p>
	 * For RSA-PSS algorithms, the appropriate {@link PSSParameterSpec} is applied.
	 * </p>
	 *
	 * @param jwsAlgorithm the JWS algorithm
	 * @return a configured {@link Signature} instance
	 * @throws NoSuchAlgorithmException if the algorithm is not supported
	 * @throws InvalidAlgorithmParameterException if the parameters cannot be set
	 */
	private static Signature getSignature(final JwsAlgorithm jwsAlgorithm) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		JcaSignatureAlgorithm jcaAlgorithm = jwsAlgorithm.jcaAlgorithm();
		Signature signature = Signature.getInstance(jcaAlgorithm.value());
		PSSParameterSpec parameterSpec = jwsAlgorithm.pssParameterSpec();
		if (null != parameterSpec) {
			signature.setParameter(parameterSpec);
		}
		return signature;
	}

}
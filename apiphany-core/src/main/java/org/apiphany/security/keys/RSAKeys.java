package org.apiphany.security.keys;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.apiphany.lang.Strings;
import org.morphix.reflection.Constructors;

/**
 * RSA key management utility methods.
 * <p>
 * This class provides methods to load RSA public and private keys from PEM-formatted files. No fancy dependencies are
 * used for PEM parsing; the implementation relies on standard Java libraries. Also no fancy validation is performed on
 * the keys, assuming the files are well-formed PEM files.
 * <p>
 * If the files cannot be read or the keys cannot be parsed, a {@link SecurityException} is thrown with details about
 * the error.
 * <p>
 * TODO: implement RSA key generation, saving, and loading methods.
 *
 * @author Radu Sebastian LAZIN
 */
public class RSAKeys {

	/**
	 * The RSA algorithm identifier.
	 */
	public static final String ALGORITHM = "RSA";

	/**
	 * The key envelope used in PEM formatting.
	 */
	public static final String KEY_ENVELOPE = String.valueOf('-').repeat(5);

	/**
	 * PEM delimiter for public key start.
	 */
	public static final String BEGIN_PUBLIC_KEY = Strings.envelope(KEY_ENVELOPE, "BEGIN PUBLIC KEY");

	/**
	 * PEM delimiter for public key end.
	 */
	public static final String END_PUBLIC_KEY = Strings.envelope(KEY_ENVELOPE, "END PUBLIC KEY");

	/**
	 * PEM delimiter for private key start.
	 */
	public static final String BEGIN_PRIVATE_KEY = Strings.envelope(KEY_ENVELOPE, "BEGIN PRIVATE KEY");

	/**
	 * PEM delimiter for private key end.
	 */
	public static final String END_PRIVATE_KEY = Strings.envelope(KEY_ENVELOPE, "END PRIVATE KEY");

	/**
	 * Private constructor to prevent instantiation.
	 */
	private RSAKeys() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Loads an RSA public key from a PEM-formatted file.
	 *
	 * @param filePath the path to the PEM file containing the RSA public key
	 * @return the loaded RSAPublicKey
	 * @throws SecurityException if an error occurs during parsing the key
	 */
	public static RSAPublicKey loadPEMPublicKey(final String filePath) {
		String key = Strings.fromFile(filePath, e -> {
			throw new SecurityException("Cannot read RSA public key from PEM file: " + filePath, e);
		});

		String publicKeyPEM = key
				.replace(BEGIN_PUBLIC_KEY, "")
				.replaceAll(Strings.EOL, "")
				.replace(END_PUBLIC_KEY, "");

		byte[] decoded = Base64.getMimeDecoder().decode(publicKeyPEM.getBytes());
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
			return (RSAPublicKey) keyFactory.generatePublic(keySpec);
		} catch (Exception e) {
			throw new SecurityException("Cannot load RSA public key from PEM file: " + filePath, e);
		}
	}

	/**
	 * Loads an RSA private key from a PEM-formatted file.
	 *
	 * @param filePath the path to the PEM file containing the RSA private key
	 * @return the loaded RSAPrivateKey
	 * @throws SecurityException if an error occurs during parsing the key
	 */
	public static RSAPrivateKey loadPEMPrivateKey(final String filePath) {
		String key = Strings.fromFile(filePath, e -> {
			throw new SecurityException("Cannot read RSA private key from PEM file: " + filePath, e);
		});

		String privateKeyPEM = key
				.replace(BEGIN_PRIVATE_KEY, "")
				.replaceAll(Strings.EOL, "")
				.replace(END_PRIVATE_KEY, "");

		byte[] decoded = Base64.getMimeDecoder().decode(privateKeyPEM.getBytes());
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (Exception e) {
			throw new SecurityException("Cannot load RSA private key from PEM file: " + filePath, e);
		}
	}
}

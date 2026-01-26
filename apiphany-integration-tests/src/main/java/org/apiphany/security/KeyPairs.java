package org.apiphany.security;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.apiphany.io.BytesOrder;
import org.apiphany.lang.Hex;
import org.apiphany.lang.Strings;
import org.apiphany.security.keys.RSAKeys;
import org.apiphany.security.keys.X25519Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run this to generate a set of new set of XDH private/public keys for testing.
 * <p>
 * Generated keys will be put in the {@code src/test/resources/security/ssl} and
 * {@code src/test/resources/security/oauth2} folders.
 * <p>
 * The files will be:
 * <ul>
 * <li>{@code xdh_private.key} - XDH private key in raw KEY format</li>
 * <li>{@code xdh_public.key} - XDH public key in raw KEY format</li>
 * <li>{@code rsa_private.pem} - RSA private key in PEM format</li>
 * <li>{@code rsa_public.pem} - RSA public key in PEM format</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public class KeyPairs {

	private static final Logger LOGGER = LoggerFactory.getLogger(KeyPairs.class);

	public static class FileName {

		public static final String XDH_PRIVATE_KEY = "xdh_private.key";
		public static final String XDH_PUBLIC_KEY = "xdh_public.key";

		public static final String RSA_PRIVATE_PEM = "rsa_private.pem";
		public static final String RSA_PUBLIC_PEM = "rsa_public.pem";

		private FileName() {
			// hide constructor
		}
	}

	public static void saveAsKey(final KeyPair keyPair, final String resourceDir) throws Exception {
		Path dirPath = Paths.get(resourceDir);
		if (!Files.exists(dirPath)) {
			Files.createDirectories(dirPath);
		}

		byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
		Path privateKeyPath = dirPath.resolve(FileName.XDH_PRIVATE_KEY);
		Files.write(privateKeyPath, privateKeyBytes);
		LOGGER.info("Saved private key (KEY): {}", privateKeyPath);

		byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
		Path publicKeyPath = dirPath.resolve(FileName.XDH_PUBLIC_KEY);
		Files.write(publicKeyPath, publicKeyBytes);
		LOGGER.info("Saved public key (KEY): {}", publicKeyPath);
	}

	public static void saveAsPem(final KeyPair keyPair, final String resourceDir) throws Exception {
		Path dirPath = Paths.get(resourceDir);
		if (!Files.exists(dirPath)) {
			Files.createDirectories(dirPath);
		}

		String privateBase64 = Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(keyPair.getPrivate().getEncoded());
		Path privateKeyPath = dirPath.resolve(FileName.RSA_PRIVATE_PEM);
		String privatePem = RSAKeys.BEGIN_PRIVATE_KEY + Strings.EOL + privateBase64 + Strings.EOL + RSAKeys.END_PRIVATE_KEY + Strings.EOL;
		Files.writeString(privateKeyPath, privatePem, StandardCharsets.US_ASCII);
		LOGGER.info("Saved private key (PEM): {}", privateKeyPath);

		String publicBase64 = Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(keyPair.getPublic().getEncoded());
		Path publicKeyPath = dirPath.resolve(FileName.RSA_PUBLIC_PEM);
		String publicPem = RSAKeys.BEGIN_PUBLIC_KEY + Strings.EOL + publicBase64 + Strings.EOL + RSAKeys.END_PUBLIC_KEY + Strings.EOL;
		Files.writeString(publicKeyPath, publicPem, StandardCharsets.US_ASCII);
		LOGGER.info("Saved public key (PEM): {}", publicKeyPath);
	}

	public static KeyPair loadKeyPairFromResources() {
		PrivateKey privateKey;
		try (InputStream is = KeyPairs.class.getResourceAsStream("/security/ssl/" + FileName.XDH_PRIVATE_KEY)) {
			if (null == is) {
				throw new FileNotFoundException("Resource not found: /security/ssl/" + FileName.XDH_PRIVATE_KEY);
			}
			byte[] privateKeyBytes = is.readAllBytes();
			LOGGER.info("Loaded private key bytes:\n{}", Hex.dump(privateKeyBytes));
			KeyFactory keyFactory = KeyFactory.getInstance(X25519Keys.ALGORITHM);
			privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
		} catch (Exception e) {
			throw new IllegalStateException("Cannot load " + FileName.XDH_PRIVATE_KEY, e);
		}
		LOGGER.info("Loaded private key:\n{}", Hex.dump(X25519Keys.INSTANCE.toByteArray(privateKey, BytesOrder.LITTLE_ENDIAN)));

		PublicKey publicKey;
		try (InputStream is = KeyPairs.class.getResourceAsStream("/security/ssl/" + FileName.XDH_PUBLIC_KEY)) {
			if (null == is) {
				throw new FileNotFoundException("Resource not found: /security/ssl/" + FileName.XDH_PUBLIC_KEY);
			}
			byte[] publicKeyBytes = is.readAllBytes();
			LOGGER.info("Loaded public key bytes:\n{}", Hex.dump(publicKeyBytes));
			KeyFactory keyFactory = KeyFactory.getInstance(X25519Keys.ALGORITHM);
			publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
		} catch (Exception e) {
			throw new IllegalStateException("Cannot load " + FileName.XDH_PUBLIC_KEY, e);
		}
		LOGGER.info("Loaded public key:\n{}", Hex.dump(X25519Keys.INSTANCE.toByteArray(publicKey, BytesOrder.LITTLE_ENDIAN)));

		return new KeyPair(publicKey, privateKey);
	}

	public static void main(final String[] args) throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(X25519Keys.ALGORITHM);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		saveAsKey(keyPair, "src/test/resources/security/ssl");

		keyPairGenerator = KeyPairGenerator.getInstance(RSAKeys.ALGORITHM);
		keyPair = keyPairGenerator.generateKeyPair();

		saveAsPem(keyPair, "src/test/resources/security/oauth2");
	}
}

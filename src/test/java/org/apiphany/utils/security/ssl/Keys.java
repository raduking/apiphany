package org.apiphany.utils.security.ssl;

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
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.apiphany.io.BytesOrder;
import org.apiphany.lang.Hex;
import org.apiphany.lang.Strings;
import org.apiphany.utils.security.X25519Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run this to generate a set of new set of XDH private/public keys for testing.
 * <p>
 * Generated keys will be put in the {@code src/test/resources/security/ssl} folder.
 *
 * @author Radu Sebastian LAZIN
 */
public class Keys {

	private static final Logger LOGGER = LoggerFactory.getLogger(Keys.class);

	public static class FileName {

		public static final String XDH_PRIVATE_KEY = "xdh_private.key";
		public static final String XDH_PUBLIC_KEY = "xdh_public.key";

		public static final String RSA_PRIVATE_PEM = "rsa_private.pem";
		public static final String RSA_PUBLIC_PEM = "rsa_public.pem";

		private FileName() {
			// hide constructor
		}
	}

	public static final String KEY_ENVELOPE = String.valueOf('-').repeat(5);

	public static final String BEGIN_PUBLIC_KEY = Strings.envelope(KEY_ENVELOPE, "BEGIN PUBLIC KEY");
	public static final String END_PUBLIC_KEY = Strings.envelope(KEY_ENVELOPE, "END PUBLIC KEY");

	public static final String BEGIN_PRIVATE_KEY = Strings.envelope(KEY_ENVELOPE, "BEGIN PRIVATE KEY");
	public static final String END_PRIVATE_KEY = Strings.envelope(KEY_ENVELOPE, "END PRIVATE KEY");

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
		String privatePem = BEGIN_PRIVATE_KEY + Strings.EOL + privateBase64 + Strings.EOL + END_PRIVATE_KEY + Strings.EOL;
		Files.writeString(privateKeyPath, privatePem, StandardCharsets.US_ASCII);
		LOGGER.info("Saved private key (PEM): {}", privateKeyPath);

		String publicBase64 = Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(keyPair.getPublic().getEncoded());
		Path publicKeyPath = dirPath.resolve(FileName.RSA_PUBLIC_PEM);
		String publicPem = BEGIN_PUBLIC_KEY + Strings.EOL + publicBase64 + Strings.EOL + END_PUBLIC_KEY + Strings.EOL;
		Files.writeString(publicKeyPath, publicPem, StandardCharsets.US_ASCII);
		LOGGER.info("Saved public key (PEM): {}", publicKeyPath);
	}

	public static KeyPair loadKeyPairFromResources() {
		X25519Keys keys = new X25519Keys();

		PrivateKey privateKey;
		try (InputStream is = Keys.class.getResourceAsStream("/security/ssl/" + FileName.XDH_PRIVATE_KEY)) {
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
		LOGGER.info("Loaded private key:\n{}", Hex.dump(keys.toByteArray(privateKey, BytesOrder.LITTLE_ENDIAN)));

		PublicKey publicKey;
		try (InputStream is = Keys.class.getResourceAsStream("/security/ssl/" + FileName.XDH_PUBLIC_KEY)) {
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
		LOGGER.info("Loaded public key:\n{}", Hex.dump(keys.toByteArray(publicKey, BytesOrder.LITTLE_ENDIAN)));

		return new KeyPair(publicKey, privateKey);
	}

	public static RSAPrivateKey loadRSAPrivateKey(final String filePath) throws Exception {
		String key = Strings.fromFile(filePath);

		String privateKeyPEM = key
				.replace(BEGIN_PRIVATE_KEY, "")
				.replaceAll(Strings.EOL, "")
				.replace(END_PRIVATE_KEY, "");

		byte[] decoded = Base64.getMimeDecoder().decode(privateKeyPEM.getBytes());

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);

		return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
	}

	public static RSAPublicKey loadRSAPublicKey(final String filePath) throws Exception {
		String key = Strings.fromFile(filePath);

		String publicKeyPEM = key
				.replace(BEGIN_PUBLIC_KEY, "")
				.replaceAll(Strings.EOL, "")
				.replace(END_PUBLIC_KEY, "");

		byte[] decoded = Base64.getMimeDecoder().decode(publicKeyPEM.getBytes());

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);

		return (RSAPublicKey) keyFactory.generatePublic(keySpec);
	}

	public static void main(final String[] args) throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(X25519Keys.ALGORITHM);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		saveAsKey(keyPair, "src/test/resources/security/ssl");

		keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPair = keyPairGenerator.generateKeyPair();

		saveAsPem(keyPair, "src/test/resources/security/oauth2");
	}
}

package org.apiphany.utils.security.ssl;

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

	public static final String XDH_PRIVATE_KEY_FILE_NAME = "xdh_private.key";
	public static final String XDH_PUBLIC_KEY_FILE_NAME = "xdh_public.key";

	public static final String XDH_PRIVATE_PEM_FILE_NAME = "xdh_private.pem";
	public static final String XDH_PUBLIC_PEM_FILE_NAME = "xdh_public.pem";

	public static void saveAsKey(final KeyPair keyPair, final String resourceDir) throws Exception {
		Path dirPath = Paths.get(resourceDir);
		if (!Files.exists(dirPath)) {
			Files.createDirectories(dirPath);
		}

		byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
		Path privateKeyPath = dirPath.resolve(XDH_PRIVATE_KEY_FILE_NAME);
		Files.write(privateKeyPath, privateKeyBytes);
		LOGGER.info("Saved private key (KEY): {}", privateKeyPath);

		byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
		Path publicKeyPath = dirPath.resolve(XDH_PUBLIC_KEY_FILE_NAME);
		Files.write(publicKeyPath, publicKeyBytes);
		LOGGER.info("Saved public key (KEY): {}", publicKeyPath);
	}

	public static void saveAsPem(final KeyPair keyPair, final String resourceDir) throws Exception {
		Path dirPath = Paths.get(resourceDir);
		if (!Files.exists(dirPath)) {
			Files.createDirectories(dirPath);
		}

		String privateBase64 = Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(keyPair.getPrivate().getEncoded());
		Path privateKeyPath = dirPath.resolve(XDH_PRIVATE_PEM_FILE_NAME);
		String privatePem = "-----BEGIN PRIVATE KEY-----\n" + privateBase64 + "\n-----END PRIVATE KEY-----\n";
		Files.writeString(privateKeyPath, privatePem, StandardCharsets.US_ASCII);
		LOGGER.info("Saved private key (PEM): {}", privateKeyPath);

		String publicBase64 = Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(keyPair.getPublic().getEncoded());
		Path publicKeyPath = dirPath.resolve(XDH_PUBLIC_PEM_FILE_NAME);
		String publicPem = "-----BEGIN PUBLIC KEY-----\n" + publicBase64 + "\n-----END PUBLIC KEY-----\n";
		Files.writeString(publicKeyPath, publicPem, StandardCharsets.US_ASCII);
		LOGGER.info("Saved public key: {}", publicKeyPath);
	}

	public static KeyPair loadKeyPairFromResources() {
		X25519Keys keys = new X25519Keys();

		PrivateKey privateKey;
		try (InputStream is = Keys.class.getResourceAsStream("/security/ssl/" + XDH_PRIVATE_KEY_FILE_NAME)) {
			byte[] privateKeyBytes = is.readAllBytes();
			LOGGER.info("Loaded private key bytes:\n{}", Hex.dump(privateKeyBytes));
			KeyFactory keyFactory = KeyFactory.getInstance("XDH");
			privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
		} catch (Exception e) {
			throw new IllegalStateException("Cannot load " + XDH_PRIVATE_KEY_FILE_NAME, e);
		}
		LOGGER.info("Loaded private key:\n{}", Hex.dump(keys.toByteArray(privateKey, BytesOrder.LITTLE_ENDIAN)));

		PublicKey publicKey;
		try (InputStream is = Keys.class.getResourceAsStream("/security/ssl/" + XDH_PUBLIC_KEY_FILE_NAME)) {
			byte[] publicKeyBytes = is.readAllBytes();
			LOGGER.info("Loaded public key bytes:\n{}", Hex.dump(publicKeyBytes));
			KeyFactory keyFactory = KeyFactory.getInstance("XDH");
			publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
		} catch (Exception e) {
			throw new IllegalStateException("Cannot load " + XDH_PUBLIC_KEY_FILE_NAME, e);
		}
		LOGGER.info("Loaded public key:\n{}", Hex.dump(keys.toByteArray(publicKey, BytesOrder.LITTLE_ENDIAN)));

		return new KeyPair(publicKey, privateKey);
	}

	public static void main(final String[] args) throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("XDH");
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		saveAsKey(keyPair, "src/test/resources/security/ssl");
		saveAsPem(keyPair, "src/test/resources/security/oauth2");
	}
}

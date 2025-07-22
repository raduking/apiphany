package org.apiphany.security.ssl;

import java.io.InputStream;
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

import org.apiphany.lang.Hex;
import org.apiphany.security.ssl.client.X25519Keys;
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

	public static void save(final KeyPair keyPair, final String resourceDir) throws Exception {
		Path dirPath = Paths.get(resourceDir);
		if (!Files.exists(dirPath)) {
			Files.createDirectories(dirPath);
		}

		byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
		Path privateKeyPath = dirPath.resolve("xdh_private.key");
		Files.write(privateKeyPath, privateKeyBytes);
		LOGGER.info("Saved private key: {}", privateKeyPath);

		byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
		Path publicKeyPath = dirPath.resolve("xdh_public.key");
		Files.write(publicKeyPath, publicKeyBytes);
		LOGGER.info("Saved public key: {}", publicKeyPath);
	}

	public static KeyPair loadKeyPairFromResources() {
		X25519Keys x25519Keys = new X25519Keys();

		PrivateKey privateKey;
		try (InputStream is = Keys.class.getResourceAsStream("/security/ssl/" + XDH_PRIVATE_KEY_FILE_NAME)) {
			byte[] privateKeyBytes = is.readAllBytes();
			LOGGER.info("Loaded private key bytes:\n{}", Hex.dump(privateKeyBytes));
			KeyFactory keyFactory = KeyFactory.getInstance("XDH");
			privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
		} catch (Exception e) {
			throw new IllegalStateException("Cannot load " + XDH_PRIVATE_KEY_FILE_NAME, e);
		}
		LOGGER.info("Loaded private key:\n{}", Hex.dump(x25519Keys.toRawByteArray(privateKey)));

		PublicKey publicKey;
		try (InputStream is = Keys.class.getResourceAsStream("/security/ssl/" + XDH_PUBLIC_KEY_FILE_NAME)) {
			byte[] publicKeyBytes = is.readAllBytes();
			LOGGER.info("Loaded public key bytes:\n{}", Hex.dump(publicKeyBytes));
			KeyFactory keyFactory = KeyFactory.getInstance("XDH");
			publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
		} catch (Exception e) {
			throw new IllegalStateException("Cannot load " + XDH_PUBLIC_KEY_FILE_NAME, e);
		}
		LOGGER.info("Loaded public key:\n{}", Hex.dump(x25519Keys.toRawByteArray(publicKey)));

		return new KeyPair(publicKey, privateKey);
	}

	public static void main(final String[] args) throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("XDH");
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		save(keyPair, "src/test/resources/security/ssl");
	}
}

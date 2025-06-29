package org.apiphany.security.ssl;

import static java.util.Objects.requireNonNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apiphany.lang.collections.Arrays;

/**
 * Utility methods for working with certificates, key stores, trust stores.
 *
 * @author Radu Sebastian LAZIN
 */
public final class Certificates {

	/**
	 * Initializes a {@link SSLContext}. See {@link SSLContext#init(KeyManager[], TrustManager[], SecureRandom)}.
	 *
	 * @param sslContext the SSL context to be initialized
	 * @param keyManagers the key managers, cannot be null
	 * @param trustManagers the trust managers, cannot be null
	 * @param random secure random
	 */
	public static void initSSLContext(final SSLContext sslContext, final KeyManager[] keyManagers, final TrustManager[] trustManagers,
			final SecureRandom random) throws GeneralSecurityException {
		requireNonNull(keyManagers, "keyManagers array cannot be null");
		requireNonNull(keyManagers, "trustManagers array cannot be null");
		KeyManager[] actualKeyManagers = keyManagers.length == 0 ? null : keyManagers;
		TrustManager[] actualTrustManagers = trustManagers.length == 0 ? null : trustManagers;
		sslContext.init(actualKeyManagers, actualTrustManagers, random);
	}

	/**
	 * Key Managers.
	 *
	 * @param keyStore the key store
	 * @param password password for the key store
	 * @return key managers
	 * @throws GeneralSecurityException when it can't create a factory instance
	 */
	public static KeyManager[] getKeyManagers(final KeyStore keyStore, final char[] password) throws GeneralSecurityException {
		KeyManager[] result = null;
		if (null != keyStore) {
			String algorithm = KeyManagerFactory.getDefaultAlgorithm();
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(algorithm);
			keyManagerFactory.init(keyStore, password);
			result = keyManagerFactory.getKeyManagers();
		}
		return Arrays.safe(result, KeyManager.class);
	}

	/**
	 * Trust Managers.
	 *
	 * @return trust managers
	 * @throws GeneralSecurityException when it can't create a factory instance
	 */
	public static TrustManager[] getTrustManagers(final KeyStore trustStore) throws GeneralSecurityException {
		TrustManager[] result = null;
		if (null != trustStore) {
			String algorithm = KeyManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
			trustManagerFactory.init(trustStore);
			result = trustManagerFactory.getTrustManagers();
		}
		return Arrays.safe(result, TrustManager.class);
	}

	/**
	 * Returns a Key Store with the given parameters.
	 *
	 * @param keyStoreLocation key store file location
	 * @param keyStoreType key store type
	 * @param password key store password
	 * @param isExternal flag to indicate whether the certificate should be loaded from the jar or from the file system
	 * @return key store
	 */
	public static KeyStore keyStore(final String keyStoreLocation, final String keyStoreType, final char[] password, final boolean isExternal) {
		if (null == keyStoreLocation || keyStoreLocation.isEmpty()) {
			return null;
		}
		char[] pass = password;
		if (null != pass && pass.length == 0) {
			pass = null;
		}
		return loadKeystore(keyStoreType, keyStoreLocation, pass, isExternal);
	}

	/**
	 * Returns a Key Store with the given parameters.
	 *
	 * @param storeProperties store properties
	 * @return key store
	 */
	public static KeyStore keyStore(final CertificateStoreInfo storeProperties) {
		return keyStore(storeProperties.getLocation(), storeProperties.getType(), storeProperties.getPassword(), storeProperties.isExternal());
	}

	/**
	 * Returns a Key Store with the given parameters.
	 *
	 * @param keyStoreType key store type
	 * @param location key store file location
	 * @param pass key store password
	 * @param isExternal flag to indicate whether the certificate should be loaded from the jar or from the file system
	 * @return key store
	 */
	private static KeyStore loadKeystore(final String keyStoreType, final String location, final char[] pass, final boolean isExternal) {
		try (InputStream keyStoreInput = isExternal
				? new FileInputStream(location)
				: Thread.currentThread().getContextClassLoader().getResourceAsStream(location)) {
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(keyStoreInput, pass);
			return keyStore;
		} catch (GeneralSecurityException | IOException e) {
			throw new SecurityException("Error loading key store: " + location, e);
		}
	}

	/**
	 * Private constructor.
	 */
	private Certificates() {
		// empty
	}
}

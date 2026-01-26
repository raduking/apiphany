package org.apiphany.security.ssl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Objects;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.JavaArrays;
import org.morphix.lang.Nullables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for working with Java SSL like key stores, trust stores, SSL context, etc.
 * <p>
 * TODO: add a builder
 *
 * @author Radu Sebastian LAZIN
 */
public final class SSLContexts {

	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SSLContexts.class);

	/**
	 * Creates a new SSL context based on the provided properties.
	 *
	 * @param sslProperties SSL properties
	 * @return a new SSL context
	 */
	public static SSLContext create(final SSLProperties sslProperties) {
		try {
			SSLContext sslContext = SSLContext.getInstance(sslProperties.getProtocol().value());
			SSLContexts.initialize(sslContext, sslProperties.getKeystore(), sslProperties.getTruststore());
			return sslContext;
		} catch (Exception e) {
			throw new SecurityException("Error initializing SSL context", e);
		}
	}

	/**
	 * Initializes a {@link SSLContext}.
	 *
	 * @param sslContext the SSL context to be initialized
	 * @param keyManagers the key managers; cannot be null
	 * @param trustManagers the trust managers; cannot be null
	 * @param random secure random
	 * @throws GeneralSecurityException when the SSL context cannot be initialized
	 */
	public static void initialize(final SSLContext sslContext, final KeyManager[] keyManagers, final TrustManager[] trustManagers,
			final SecureRandom random) throws GeneralSecurityException {
		Objects.requireNonNull(keyManagers, "keyManagers array cannot be null");
		Objects.requireNonNull(keyManagers, "trustManagers array cannot be null");
		KeyManager[] actualKeyManagers = keyManagers.length == 0 ? null : keyManagers;
		TrustManager[] actualTrustManagers = trustManagers.length == 0 ? null : trustManagers;
		sslContext.init(actualKeyManagers, actualTrustManagers, random);
	}

	/**
	 * Initializes a {@link SSLContext}.
	 *
	 * @param sslContext the SSL context to be initialized
	 * @param keyManagers the key managers; cannot be null
	 * @param trustManagers the trust managers; cannot be null
	 * @throws GeneralSecurityException when the SSL context cannot be initialized
	 */
	public static void initialize(final SSLContext sslContext, final KeyManager[] keyManagers, final TrustManager[] trustManagers)
			throws GeneralSecurityException {
		initialize(sslContext, keyManagers, trustManagers, new SecureRandom());
	}

	/**
	 * Initializes a {@link SSLContext}.
	 *
	 * @param sslContext the SSL context to be initialized
	 * @param keyStore key store information
	 * @param trustStore trust store information
	 * @throws GeneralSecurityException when the SSL context cannot be initialized
	 */
	public static void initialize(final SSLContext sslContext, final StoreInfo keyStore, final StoreInfo trustStore)
			throws GeneralSecurityException {
		initialize(sslContext, getKeyManagers(keyStore), getTrustManagers(trustStore));
	}

	/**
	 * Returns the key managers.
	 *
	 * @param keyStoreInfo the key store information
	 * @return key managers
	 * @throws GeneralSecurityException when it can't create a factory instance
	 */
	public static KeyManager[] getKeyManagers(final StoreInfo keyStoreInfo) throws GeneralSecurityException {
		return getKeyManagers(keyStore(keyStoreInfo), keyStoreInfo.getPassword(), keyStoreInfo.getAlgorithm());
	}

	/**
	 * Returns the key managers.
	 *
	 * @param keyStore the key store
	 * @param password password for the key store
	 * @param algorithm the algorithm for the key manager factory
	 * @return key managers
	 * @throws GeneralSecurityException when it can't create a factory instance
	 */
	public static KeyManager[] getKeyManagers(final KeyStore keyStore, final char[] password, final String algorithm)
			throws GeneralSecurityException {
		KeyManager[] result = null;
		if (null != keyStore) {
			String alg = Nullables.nonNullOrDefault(algorithm, KeyManagerFactory::getDefaultAlgorithm);
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(alg);
			keyManagerFactory.init(keyStore, password);
			result = keyManagerFactory.getKeyManagers();
		}
		return JavaArrays.safe(result, KeyManager.class);
	}

	/**
	 * Returns the trust managers.
	 *
	 * @param trustStoreInfo the trust store information
	 * @return trust managers
	 * @throws GeneralSecurityException when it can't create a factory instance
	 */
	public static TrustManager[] getTrustManagers(final StoreInfo trustStoreInfo) throws GeneralSecurityException {
		return getTrustManagers(keyStore(trustStoreInfo), trustStoreInfo.getAlgorithm());
	}

	/**
	 * Returns the trust managers.
	 *
	 * @param trustStore the trust store
	 * @param algorithm the algorithm for the key manager factory
	 * @return trust managers
	 * @throws GeneralSecurityException when it can't create a factory instance
	 */
	public static TrustManager[] getTrustManagers(final KeyStore trustStore, final String algorithm) throws GeneralSecurityException {
		TrustManager[] result = null;
		if (null != trustStore) {
			String alg = Nullables.nonNullOrDefault(algorithm, KeyManagerFactory::getDefaultAlgorithm);
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(alg);
			trustManagerFactory.init(trustStore);
			result = trustManagerFactory.getTrustManagers();
		}
		return JavaArrays.safe(result, TrustManager.class);
	}

	/**
	 * Returns a Key Store with the given parameters.
	 *
	 * @param storeProperties store properties
	 * @return key store
	 */
	public static KeyStore keyStore(final StoreInfo storeProperties) {
		return keyStore(storeProperties.getLocation(), storeProperties.getType(), storeProperties.getPassword(), storeProperties.isExternal());
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
		if (Strings.isEmpty(keyStoreType)) {
			LOGGER.warn("Location is empty. Key store type: {}, external: {}", keyStoreType, isExternal);
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
			if (null == keyStoreInput) {
				throw new IOException("File not found: " + location);
			}
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
	private SSLContexts() {
		// empty
	}
}

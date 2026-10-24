package org.apiphany.security.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.function.Function;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apiphany.io.ResourceLocation;
import org.apiphany.lang.Strings;
import org.apiphany.logging.LoggerAdapters;
import org.morphix.lang.JavaArrays;
import org.morphix.lang.Nullables;
import org.morphix.lang.function.Functions;
import org.morphix.lang.function.LoggerAdapter;
import org.morphix.reflection.Constructors;

/**
 * Utility methods for working with Java SSL like key stores, trust stores, SSL context, etc.
 *
 * @author Radu Sebastian LAZIN
 */
public final class SSLContexts {

	/**
	 * Logger for this class.
	 */
	private static final LoggerAdapter LOGGER = LoggerAdapters.of(SSLContexts.class);

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
		Objects.requireNonNull(trustManagers, "trustManagers array cannot be null");
		sslContext.init(JavaArrays.nullIfEmpty(keyManagers), JavaArrays.nullIfEmpty(trustManagers), random);
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
			String alg = Nullables.nonNullOrDefault(algorithm, TrustManagerFactory::getDefaultAlgorithm);
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(alg);
			trustManagerFactory.init(trustStore);
			result = trustManagerFactory.getTrustManagers();
		}
		return JavaArrays.safe(result, TrustManager.class);
	}

	/**
	 * Returns the trust managers for the given truststore configuration, re-throwing any security failure as a
	 * {@link SecurityException}.
	 *
	 * @param trustStoreInfo truststore configuration
	 * @return the trust managers for the given configuration, may be null
	 * @throws SecurityException when the trust managers cannot be created from the given configuration
	 */
	public static TrustManager[] trustManagers(final StoreInfo trustStoreInfo) {
		try {
			return SSLContexts.getTrustManagers(trustStoreInfo);
		} catch (GeneralSecurityException e) {
			throw new SecurityException("Failed to create trust managers from the truststore configuration", e);
		}
	}

	/**
	 * Returns the first trust manager of the given type from the given trust managers, or the platform default trust
	 * manager of the given type when the array is null or contains none of the given type.
	 *
	 * @param <T> the trust manager type
	 *
	 * @param trustManagerClass the trust manager class to look for
	 * @param trustManagers trust managers to inspect, may be null
	 * @return a trust manager of the given type
	 * @throws SecurityException when no trust manager of the given type can be found
	 */
	public static <T extends TrustManager> T trustManager(final Class<T> trustManagerClass, final TrustManager[] trustManagers) {
		return trustManager(trustManagerClass, trustManagers, SSLContexts::defaultTrustManager);
	}

	/**
	 * Returns the first trust manager of the given type from the given trust managers, or the platform default trust
	 * manager of the given type when the array is null or contains none of the given type.
	 *
	 * @param <T> the trust manager type
	 *
	 * @param trustManagerClass the trust manager class to look for
	 * @param trustManagers trust managers to inspect, may be null
	 * @param fallback function to determine the trust manager if one is not present in the given trust managers
	 * @return a trust manager of the given type
	 * @throws SecurityException when no trust manager of the given type can be found
	 */
	public static <T extends TrustManager> T trustManager(final Class<T> trustManagerClass, final TrustManager[] trustManagers,
			final Function<Class<T>, T> fallback) {
		if (null != trustManagers) {
			for (TrustManager trustManager : trustManagers) {
				if (trustManagerClass.isInstance(trustManager)) {
					return trustManagerClass.cast(trustManager);
				}
			}
		}
		return fallback.apply(trustManagerClass);
	}

	/**
	 * Returns the platform default trust manager of the given type, searching the trust managers provided by the default
	 * {@link TrustManagerFactory}.
	 *
	 * @param <T> the trust manager type
	 *
	 * @param trustManagerClass the trust manager class to look for
	 * @return the platform default trust manager of the given type
	 * @throws SecurityException when the default trust managers cannot be created or no trust manager of the given found
	 */
	public static <T extends TrustManager> T defaultTrustManager(final Class<T> trustManagerClass) {
		T trustManager;
		try {
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init((KeyStore) null);
			trustManager = trustManager(trustManagerClass, trustManagerFactory.getTrustManagers(), Functions.returnNull());
		} catch (GeneralSecurityException e) {
			throw new SecurityException("Error while looking for default trust manager", e);
		}
		if (null == trustManager) {
			throw new SecurityException("No " + trustManagerClass.getName() + " found in default trust managers");
		}
		return trustManager;
	}

	/**
	 * Returns a Key Store with the given parameters.
	 *
	 * @param storeProperties store properties
	 * @return key store
	 */
	public static KeyStore keyStore(final StoreInfo storeProperties) {
		return keyStore(storeProperties.getLocation(), storeProperties.getType(), storeProperties.getPassword(),
				storeProperties.isExternal() ? ResourceLocation.FILE_SYSTEM : ResourceLocation.CLASS_PATH);
	}

	/**
	 * Returns a Key Store with the given parameters.
	 *
	 * @param keyStoreLocation key store file location
	 * @param keyStoreType key store type
	 * @param password key store password
	 * @param resourceLocation the resource location indicating whether the certificate should be loaded
	 * @return key store
	 */
	public static KeyStore keyStore(final String keyStoreLocation, final String keyStoreType, final char[] password,
			final ResourceLocation resourceLocation) {
		if (Strings.isEmpty(keyStoreLocation)) {
			LOGGER.warn("Location is empty. Key store type: {}, file source: {}", keyStoreType, resourceLocation);
			return null;
		}
		if (Strings.isEmpty(keyStoreType)) {
			LOGGER.warn("Key store type is empty. Key store location: {}, file source: {}", keyStoreLocation, resourceLocation);
			return null;
		}
		char[] pass = password;
		if (null != pass && pass.length == 0) {
			pass = null;
		}
		return loadKeystore(keyStoreType, keyStoreLocation, pass, resourceLocation);
	}

	/**
	 * Returns a Key Store with the given parameters.
	 *
	 * @param keyStoreType key store type
	 * @param location key store file location
	 * @param pass key store password
	 * @param resourceLocation indicate whether the certificate should be loaded from the jar or from the file system
	 * @return key store
	 */
	private static KeyStore loadKeystore(final String keyStoreType, final String location, final char[] pass,
			final ResourceLocation resourceLocation) {
		try (InputStream keyStoreInput = resourceLocation.open(location)) {
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
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns a new {@link SSLContextBuilder} for fluently building {@link SSLContext} instances.
	 *
	 * @return a new {@link SSLContextBuilder}
	 */
	public static SSLContextBuilder builder() {
		return SSLContextBuilder.create();
	}
}

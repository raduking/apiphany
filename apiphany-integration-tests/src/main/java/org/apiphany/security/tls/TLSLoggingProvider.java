package org.apiphany.security.tls;

import java.io.Serial;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.apiphany.lang.Hex;
import org.morphix.reflection.Classes;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security provider that logs TLS key material generation.
 * <p>
 * Needs open access to {@code javax.crypto.JceSecurity} and may require
 *
 * <pre>
 * --add-opens java.base/javax.crypto=ALL-UNNAMED
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
public final class TLSLoggingProvider extends Provider {

	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TLSLoggingProvider.class);

	/**
	 * Serial version UID.
	 */
	@Serial
	private static final long serialVersionUID = -2570888245931771474L;

	/**
	 * Default constructor.
	 */
	public TLSLoggingProvider() {
		super("ApiphanyTLSLoggingProvider", "1.0", "TLS key material logger");
		// register our wrapper for TLS 1.2 key material
		put("KeyGenerator.SunTls12KeyMaterial", LoggingTlsKeyMaterialGenerator.class.getName());
		put("KeyGenerator.SunTls12MasterSecret", LoggingTlsMasterSecretGenerator.class.getName());
	}

	/**
	 * Install the TLS logging provider at priority 1 (before SunJCE).
	 */
	public static void install() {
		TLSLoggingProvider provider = new TLSLoggingProvider();
		safeMarkAsVerified(provider);
		// insert before SunJCE
		Security.insertProviderAt(provider, 1);
	}

	/**
	 * Safely mark the given provider as verified in the JCE security internals.
	 *
	 * @param provider provider to mark as verified
	 */
	public static void safeMarkAsVerified(final Provider provider) {
		try {
			markAsVerified(provider);
		} catch (Exception e) {
			LOGGER.error("Could not mark provider as verified, TLS secrets logging will not be available.", e);
		}
	}

	/**
	 * Mark the given provider as verified in the JCE security internals.
	 *
	 * @param provider provider to mark as verified
	 */
	private static void markAsVerified(final Provider provider) {
		Class<?> jceSecurityClass = Classes.getOne("javax.crypto.JceSecurity");
		Map<Object, Object> verificationResults = Fields.IgnoreAccess.getStatic(jceSecurityClass, "verificationResults");
		ReferenceQueue<Object> queue = Fields.IgnoreAccess.getStatic(jceSecurityClass, "queue");

		// find the WeakIdentityWrapper private static inner class
		Class<?>[] declared = jceSecurityClass.getDeclaredClasses();
		Class<?> weakIdentityWrapperClass = null;
		for (Class<?> cls : declared) {
			String className = cls.getSimpleName();
			if ("WeakIdentityWrapper".equals(className)) {
				weakIdentityWrapperClass = cls;
				break;
			}
		}
		if (weakIdentityWrapperClass == null) {
			throw new IllegalStateException("WeakIdentityWrapper class not found");
		}
		Constructor<?> constructor = Constructors.getDeclared(weakIdentityWrapperClass, Provider.class, ReferenceQueue.class);
		Object wrapperInstance = Constructors.IgnoreAccess.newInstance(constructor, provider, queue);
		if (verificationResults.containsKey(wrapperInstance)) {
			LOGGER.warn("Provider wrapper already present in verification results.");
		}
		Object providerVerified = Fields.IgnoreAccess.get(jceSecurityClass, "PROVIDER_VERIFIED");
		verificationResults.put(wrapperInstance, providerVerified);
	}

	/**
	 * Key generator wrapper that logs generated key material.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public abstract static class LoggingKeyGeneratorSpi extends KeyGeneratorSpi {

		/**
		 * Delegate key generator.
		 */
		private final KeyGenerator delegate;

		/**
		 * Constructor.
		 *
		 * @param algo algorithm name
		 * @throws NoSuchAlgorithmException if the algorithm is not found
		 * @throws NoSuchProviderException if the provider is not found
		 */
		protected LoggingKeyGeneratorSpi(final String algo) throws NoSuchAlgorithmException, NoSuchProviderException {
			this.delegate = KeyGenerator.getInstance(algo, "SunJCE");
		}

		/**
		 * @see KeyGeneratorSpi#engineInit(SecureRandom)
		 */
		@Override
		protected void engineInit(final SecureRandom random) {
			delegate.init(random);
		}

		/**
		 * @see KeyGeneratorSpi#engineInit(int, SecureRandom)
		 */
		@Override
		protected void engineInit(final int keySize, final SecureRandom random) {
			delegate.init(keySize, random);
		}

		/**
		 * @see KeyGeneratorSpi#engineInit(AlgorithmParameterSpec, SecureRandom)
		 */
		@Override
		protected void engineInit(final AlgorithmParameterSpec params, final SecureRandom random) throws InvalidAlgorithmParameterException {
			delegate.init(params, random);
		}

		/**
		 * @see KeyGeneratorSpi#engineGenerateKey()
		 */
		@Override
		protected SecretKey engineGenerateKey() {
			SecretKey key = delegate.generateKey();
			try {
				KeyGeneratorSpi spi = Fields.IgnoreAccess.get(delegate, "spi");
				Object spec = Fields.IgnoreAccess.get(spi, "spec");

				byte[] clientRandom = Fields.IgnoreAccess.get(spec, "clientRandom");
				LOGGER.debug("Client Random: {}", Hex.string(clientRandom));
				byte[] serverRandom = Fields.IgnoreAccess.get(spec, "serverRandom");
				LOGGER.debug("Server Random: {}", Hex.string(serverRandom));

				logKeyField(spec, "premasterSecret");
				logKeyField(spec, "masterSecret");
			} catch (Exception e) {
				LOGGER.warn("Could not log secrets", e);
			}
			try {
				if (null != key.getEncoded()) {
					logKey(key);
				} else {
					logKeyField(key, "clientMacKey");
					logKeyField(key, "serverMacKey");
					logKeyField(key, "clientCipherKey");
					logKeyField(key, "serverCipherKey");

					IvParameterSpec clientIV = Fields.IgnoreAccess.get(key, "clientIv");
					IvParameterSpec serverIV = Fields.IgnoreAccess.get(key, "serverIv");
					LOGGER.debug("clientIV: {}", null != clientIV ? Hex.string(clientIV.getIV()) : "null");
					LOGGER.debug("serverIV: {}", null != serverIV ? Hex.string(serverIV.getIV()) : "null");
				}
			} catch (Exception e) {
				LOGGER.warn("Could not log generated key", e);
			}
			return key;
		}

		/**
		 * Log the given key field from the given object.
		 *
		 * @param obj object containing the field
		 * @param fieldName field name
		 */
		private static void logKeyField(final Object obj, final String fieldName) {
			Field keyField = Fields.getOneDeclaredInHierarchy(obj, fieldName);
			if (null != keyField) {
				SecretKey key = Fields.IgnoreAccess.get(obj, keyField);
				logKey(key, fieldName);
			}
		}

		/**
		 * Log the given key.
		 *
		 * @param key secret key
		 */
		private static void logKey(final SecretKey key) {
			logKey(key, "<none>");
		}

		/**
		 * Log the given key with the given field name.
		 *
		 * @param key secret key
		 * @param fieldName field name
		 */
		private static void logKey(final SecretKey key, final String fieldName) {
			LOGGER.debug("Generated {} (field:{}): {}", key.getAlgorithm(), fieldName, Hex.string(key.getEncoded()));
		}
	}

	/**
	 * Logging TLS key material generator.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static final class LoggingTlsKeyMaterialGenerator extends LoggingKeyGeneratorSpi {

		/**
		 * Constructor.
		 *
		 * @throws Exception if an error occurs
		 */
		public LoggingTlsKeyMaterialGenerator() throws Exception {
			super("SunTls12KeyMaterial");
		}
	}

	/**
	 * Logging TLS master secret generator.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static final class LoggingTlsMasterSecretGenerator extends LoggingKeyGeneratorSpi {

		/**
		 * Constructor.
		 *
		 * @throws Exception if an error occurs
		 */
		public LoggingTlsMasterSecretGenerator() throws Exception {
			super("SunTls12MasterSecret");
		}
	}
}

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
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Fields;
import org.morphix.reflection.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security provider that logs TLS key material generation.
 * <p>
 * Needs open access to {@link javax.crypto.JceSecurity} and may require
 *
 * <pre>
 * --add-opens java.base/javax.crypto=ALL-UNNAMED
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
public final class TLSLoggingProvider extends Provider {

	private static final Logger LOGGER = LoggerFactory.getLogger(TLSLoggingProvider.class);

	@Serial
	private static final long serialVersionUID = -2570888245931771474L;

	public TLSLoggingProvider() {
		super("ApiphanyTLSLoggingProvider", "1.0", "TLS key material logger");
		// register our wrapper for TLS 1.2 key material
		put("KeyGenerator.SunTls12KeyMaterial", LoggingTlsKeyMaterialGenerator.class.getName());
		put("KeyGenerator.SunTls12MasterSecret", LoggingTlsMasterSecretGenerator.class.getName());
	}

	public static void install() {
		TLSLoggingProvider provider = new TLSLoggingProvider();
		safeMarkAsVerified(provider);
		// insert before SunJCE
		Security.insertProviderAt(provider, 1);
	}

	public static void safeMarkAsVerified(final Provider provider) {
		try {
			markAsVerified(provider);
		} catch (Exception e) {
			LOGGER.error("Could not mark provider as verified, TLS secrets logging will not be available.", e);
		}
	}

	private static void markAsVerified(final Provider provider) {
		Class<?> jceSecurityClass = Reflection.getClass("javax.crypto.JceSecurity");
		Map<Object, Object> verificationResults = Fields.IgnoreAccess.getStatic(jceSecurityClass, "verificationResults");
		ReferenceQueue<Object> queue = Fields.IgnoreAccess.getStatic(jceSecurityClass, "queue");

		// find the WeakIdentityWrapper private static inner class
		Class<?>[] declared = jceSecurityClass.getDeclaredClasses();
		Class<?> weakIdentityWrapperClass = null;
		for (Class<?> c : declared) {
			String className = c.getSimpleName();
			if ("WeakIdentityWrapper".equals(className)) {
				weakIdentityWrapperClass = c;
				break;
			}
		}
		if (weakIdentityWrapperClass == null) {
			throw new IllegalStateException("WeakIdentityWrapper class not found");
		}
		Constructor<?> constructor = Constructors.getDeclaredConstructor(weakIdentityWrapperClass, Provider.class, ReferenceQueue.class);
		Object wrapperInstance = Constructors.IgnoreAccess.newInstance(constructor, provider, queue);
		if (verificationResults.containsKey(wrapperInstance)) {
			LOGGER.warn("Provider wrapper already present in verification results.");
		}
		Object providerVerified = Fields.IgnoreAccess.get(jceSecurityClass, "PROVIDER_VERIFIED");
		verificationResults.put(wrapperInstance, providerVerified);
	}

	public abstract static class LoggingWrapper extends KeyGeneratorSpi {

		private final KeyGenerator delegate;

		protected LoggingWrapper(final String algo) throws NoSuchAlgorithmException, NoSuchProviderException {
			this.delegate = KeyGenerator.getInstance(algo, "SunJCE");
		}

		@Override
		protected void engineInit(final SecureRandom random) {
			delegate.init(random);
		}

		@Override
		protected void engineInit(final int keysize, final SecureRandom random) {
			delegate.init(keysize, random);
		}

		@Override
		protected void engineInit(final AlgorithmParameterSpec params, final SecureRandom random) throws InvalidAlgorithmParameterException {
			delegate.init(params, random);
		}

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

				logKey(spec, "premasterSecret");
				logKey(spec, "masterSecret");
			} catch (Exception e) {
				LOGGER.warn("Could not log secrets", e);
			}
			try {
				if (null != key.getEncoded()) {
					logKey(key);
				} else {
					logKey(key, "clientMacKey");
					logKey(key, "serverMacKey");
					logKey(key, "clientCipherKey");
					logKey(key, "serverCipherKey");

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
	}

	private static void logKey(final Object obj, final String fieldName) {
		Field keyField = Fields.getOneDeclaredInHierarchy(obj, fieldName);
		if (null != keyField) {
			SecretKey key = Fields.IgnoreAccess.get(obj, keyField);
			logKey(key);
		}
	}

	private static void logKey(final SecretKey key) {
		LOGGER.debug("Generated {}: {}", key.getAlgorithm(), Hex.string(key.getEncoded()));
	}

	public static final class LoggingTlsKeyMaterialGenerator extends LoggingWrapper {

		public LoggingTlsKeyMaterialGenerator() throws Exception {
			super("SunTls12KeyMaterial");
		}
	}

	public static final class LoggingTlsMasterSecretGenerator extends LoggingWrapper {

		public LoggingTlsMasterSecretGenerator() throws Exception {
			super("SunTls12MasterSecret");
		}
	}
}

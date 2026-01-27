package org.apiphany.security.tls;

import org.apiphany.lang.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logging wrapper for {@link ExchangeKeys} to trace key derivation process.
 *
 * @author Radu Sebastian LAZIN
 */
public class LoggingExchangeKeys {

	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingExchangeKeys.class);

	/**
	 * Hide constructor.
	 */
	private LoggingExchangeKeys() {
		// empty
	}

	/**
	 * Parses the given TLS key block according to the given cipher suite, logging all derived keys.
	 *
	 * @param keyBlock TLS key block
	 * @param suite cipher suite
	 */
	public static ExchangeKeys from(final byte[] keyBlock, final CipherSuite suite) {
		LOGGER.debug("keyBlock: {}", Hex.stringSupplier(keyBlock));
		LOGGER.debug("keyBlock length: {}", keyBlock.length);

		ExchangeKeys exchangeKeys = ExchangeKeys.from(keyBlock, suite);

		LOGGER.debug("clientMacKey: {}", Hex.stringSupplier(exchangeKeys.getClientMacKey()));
		LOGGER.debug("serverMacKey: {}", Hex.stringSupplier(exchangeKeys.getServerMacKey()));
		LOGGER.debug("clientWriteKey: {}", Hex.stringSupplier(exchangeKeys.getClientWriteKey()));
		LOGGER.debug("serverWriteKey: {}", Hex.stringSupplier(exchangeKeys.getServerWriteKey()));
		LOGGER.debug("clientIV: {}", Hex.stringSupplier(exchangeKeys.getClientIV()));
		LOGGER.debug("serverIV: {}", Hex.stringSupplier(exchangeKeys.getServerIV()));

		return exchangeKeys;
	}
}

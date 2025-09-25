package org.apiphany.utils;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.security.ssl.server.LegacyHttpsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForkedLegacyHttpsServerRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(ForkedLegacyHttpsServerRunner.class);

	public static final int ERROR_USAGE = 1;
	public static final int ERROR_EXECUTION = 666;

	public static void main(final String[] args) {
		if (args.length != 2) {
			LOGGER.error("Usage: ForkedServerRunner <port> <sslPropertiesJsonPath>");
			System.exit(ERROR_USAGE);
		}

		int port = Integer.parseInt(args[0]);
		String sslPropertiesJsonPath = args[1];

		String json = Strings.fromFile(sslPropertiesJsonPath, e -> LOGGER.error("Error reading file", e));
		SSLProperties sslProperties = JsonBuilder.fromJson(json, SSLProperties.class);

		try (LegacyHttpsServer server = new LegacyHttpsServer(port, sslProperties)) {
			// block current thread
			Thread.currentThread().join();
		} catch (Throwable t) {
			LOGGER.error("Failed to start server", t);
			System.exit(ERROR_EXECUTION);
		}
	}
}

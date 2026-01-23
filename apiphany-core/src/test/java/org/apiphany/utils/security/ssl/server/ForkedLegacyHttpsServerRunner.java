package org.apiphany.utils.security.ssl.server;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Pair;
import org.apiphany.lang.Strings;
import org.apiphany.lang.retry.Retry;
import org.apiphany.lang.retry.WaitTimeout;
import org.apiphany.net.Sockets;
import org.apiphany.security.ssl.SSLProperties;
import org.morphix.lang.function.ThrowingBiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to run a {@link LegacyHttpsServer} in a separate process so that it has a separated SSL context.
 *
 * @author Radu Sebastian LAZIN
 */
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

		try (var server = new LegacyHttpsServer(port, sslProperties)) {
			// block current thread
			Thread.currentThread().join();
		} catch (Throwable t) {
			LOGGER.error("Failed to start server", t);
			System.exit(ERROR_EXECUTION);
		}
	}

	public static <T> T on(final String sslPropertiesJsonFile, final String host, final Duration socketTimeout, final boolean sslDebugInfo,
			final ThrowingBiFunction<String, Integer, T> statements) throws Exception {
		int port = Sockets.findAvailableTcpPort();

		Pair<Process, Thread> serverInfo = start(sslPropertiesJsonFile, host, port, socketTimeout, sslDebugInfo);
		try {
			return statements.apply(host, port);
		} catch (Throwable t) {
			throw new IllegalStateException("Error executing statements: ", t);
		} finally {
			stop(serverInfo);
		}
	}

	public static Pair<Process, Thread> start(final String sslPropertiesJsonFile, final String host, final int port, final Duration socketTimeout,
			final boolean sslDebugInfo) throws Exception {
		List<String> cmd = new ArrayList<>();

		cmd.addAll(List.of(
				"java",
				"--add-opens", "java.base/sun.security.ssl=ALL-UNNAMED",
				"--add-opens", "java.base/javax.net.ssl=ALL-UNNAMED",
				"--add-opens", "jdk.httpserver/sun.net.httpserver=ALL-UNNAMED"));
		if (sslDebugInfo) {
			cmd.add("-Djavax.net.debug=ssl:handshake:verbose:plaintext:sslctx:packet");
		}
		cmd.addAll(List.of(
				"-cp", System.getProperty("java.class.path"),
				ForkedLegacyHttpsServerRunner.class.getName(),
				String.valueOf(port),
				sslPropertiesJsonFile));

		Process serverProcess = new ProcessBuilder(cmd)
				.redirectErrorStream(true)
				.start();

		Thread loggingThread = Thread.ofVirtual().start(() -> {
			try (InputStream is = serverProcess.getInputStream()) {
				is.transferTo(System.out);
			} catch (Exception e) {
				LOGGER.error("Error logging", e);
			}
		});

		Retry retry = Retry.of(WaitTimeout.of(socketTimeout, Duration.ofMillis(200)));
		boolean canConnect = retry.until(() -> Sockets.canConnectTo(host, port), Boolean::booleanValue);

		if (!canConnect) {
			stop(serverProcess, loggingThread);
			throw new IllegalStateException("Cannot connect to server: " + host + ":" + port);
		}

		return Pair.of(serverProcess, loggingThread);
	}

	public static void stop(final Process serverProcess, final Thread loggingThread) throws Exception {
		if (null != serverProcess) {
			serverProcess.destroy();
		}
		if (null != loggingThread) {
			loggingThread.join();
		}
	}

	public static void stop(final Pair<Process, Thread> serverInfo) throws Exception {
		stop(serverInfo.left(), serverInfo.right());
	}
}

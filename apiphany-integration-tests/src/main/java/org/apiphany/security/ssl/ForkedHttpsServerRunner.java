package org.apiphany.security.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Pair;
import org.apiphany.lang.Strings;
import org.apiphany.lang.retry.Retry;
import org.apiphany.lang.retry.WaitTimeout;
import org.apiphany.net.Sockets;
import org.apiphany.test.fork.ForkedJvmRunner;
import org.morphix.lang.function.ThrowingBiFunction;
import org.morphix.reflection.Classes;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.ReflectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to run a HTTPS server in a separate process so that it has a separated SSL context.
 * <p>
 * The server class must have a constructor with the following signature:
 *
 * <pre>
 *    MyHttpsServer(int port, SSLProperties sslProperties)
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
public class ForkedHttpsServerRunner {

	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ForkedHttpsServerRunner.class);

	/**
	 * Interval between connection retries.
	 */
	private static final Duration CONNECT_RETRY_INTERVAL = Duration.ofMillis(200);

	/**
	 * JVM argument to add module opens.
	 */
	private static final String JVM_ADD_OPENS = "--add-opens";

	/**
	 * Hide default constructor, only the main method is needed.
	 */
	private ForkedHttpsServerRunner() {
		// empty
	}

	/**
	 * Main method to start the server.
	 *
	 * @param args command line arguments: {@code <port> <sslPropertiesJsonPath>}
	 */
	public static void main(final String[] args) {
		if (args.length != 3) {
			LOGGER.error("Usage: ForkedServerRunner <serverClass> <port> <sslPropertiesJsonPath>");
			System.exit(ForkedJvmRunner.Error.USAGE);
		}

		String serverClassName = args[0];
		int port = Integer.parseInt(args[1]);
		String sslPropertiesJsonPath = args[2];

		String json = Strings.fromFile(sslPropertiesJsonPath, e -> LOGGER.error("Error reading file", e));
		SSLProperties sslProperties = JsonBuilder.fromJson(json, SSLProperties.class);

		LOGGER.info("Loading server class: {}", serverClassName);
		Class<? extends AutoCloseable> serverClass = null;
		try {
			serverClass = Classes.getOne(serverClassName);
		} catch (ReflectionException e) {
			LOGGER.error("Server class not found: {}", serverClassName, e);
			System.exit(ForkedJvmRunner.Error.TEST);
		}

		try (var server = createServerInstance(serverClass, port, sslProperties)) {
			// block current thread
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			LOGGER.info("Server interrupted, shutting down");
			Thread.currentThread().interrupt();
			System.exit(ForkedJvmRunner.Error.TEST);
		} catch (Exception e) {
			LOGGER.error("Failed to start server", e);
			System.exit(ForkedJvmRunner.Error.TEST);
		}
	}

	/**
	 * Creates an instance of the server class.
	 *
	 * @param <T> the type of the server
	 *
	 * @param serverClass the server class
	 * @param port the port
	 * @param sslProperties the SSL properties
	 * @return the server instance
	 */
	private static <T extends AutoCloseable> T createServerInstance(final Class<T> serverClass, final int port, final SSLProperties sslProperties) {
		Constructor<T> constructor = Constructors.getDeclared(serverClass, int.class, SSLProperties.class);
		return Constructors.IgnoreAccess.newInstance(constructor, port, sslProperties);
	}

	/**
	 * Run statements on a forked legacy HTTPS server.
	 *
	 * @param <T> the type of the result
	 *
	 * @param serverClass the server class
	 * @param sslPropertiesJsonFile the SSL properties JSON file
	 * @param host the host
	 * @param socketTimeout the socket timeout
	 * @param sslDebugInfo whether to enable SSL debug info
	 * @param statements the statements to execute
	 * @return the result of the statements
	 * @throws IOException if an I/O error occurs
	 * @throws InterruptedException if interrupted while waiting for the server to start
	 */
	public static <T> T on(
			final Class<? extends AutoCloseable> serverClass,
			final String sslPropertiesJsonFile,
			final String host,
			final Duration socketTimeout,
			final boolean sslDebugInfo,
			final ThrowingBiFunction<String, Integer, T> statements) throws IOException, InterruptedException {
		int port = Sockets.findAvailableTcpPort();

		Pair<Process, Thread> serverInfo = start(serverClass, sslPropertiesJsonFile, host, port, socketTimeout, sslDebugInfo);
		try {
			return statements.apply(host, port);
		} catch (Throwable t) {
			throw new IllegalStateException("Error executing statements: ", t);
		} finally {
			stop(serverInfo);
		}
	}

	/**
	 * Starts a forked legacy HTTPS server, runs the main method of {@link ForkedHttpsServerRunner} in a separate process.
	 *
	 * @param serverClass the server class
	 * @param sslPropertiesJsonFile the SSL properties JSON file
	 * @param host the host
	 * @param port the port
	 * @param socketTimeout the socket timeout
	 * @param sslDebugInfo whether to enable SSL debug info
	 * @return a pair containing the server process and the logging thread
	 * @throws IOException if an I/O error occurs
	 * @throws InterruptedException if interrupted while waiting for the server to start
	 */
	public static Pair<Process, Thread> start(
			final Class<? extends AutoCloseable> serverClass,
			final String sslPropertiesJsonFile,
			final String host,
			final int port,
			final Duration socketTimeout,
			final boolean sslDebugInfo) throws IOException, InterruptedException {
		List<String> cmd = new ArrayList<>();

		cmd.addAll(List.of(
				"java",
				JVM_ADD_OPENS, "java.base/sun.security.ssl=ALL-UNNAMED",
				JVM_ADD_OPENS, "java.base/javax.net.ssl=ALL-UNNAMED",
				JVM_ADD_OPENS, "jdk.httpserver/sun.net.httpserver=ALL-UNNAMED"));
		if (sslDebugInfo) {
			cmd.add("-Djavax.net.debug=ssl:handshake:verbose:plaintext:sslctx:packet");
		}
		cmd.addAll(List.of(
				"-cp", System.getProperty("java.class.path"),
				ForkedHttpsServerRunner.class.getName(),
				serverClass.getName(),
				String.valueOf(port),
				sslPropertiesJsonFile));

		Process serverProcess = new ProcessBuilder(cmd)
				.redirectErrorStream(true)
				.start();

		Thread loggingThread = Thread.ofVirtual().start(() -> {
			try (InputStream is = serverProcess.getInputStream()) {
				is.transferTo(System.out); // NOSONAR this is fine for a test utility
			} catch (Exception e) {
				LOGGER.error("Error logging", e);
			}
		});

		Retry retry = Retry.of(WaitTimeout.of(socketTimeout, CONNECT_RETRY_INTERVAL));
		boolean canConnect = retry.until(() -> Sockets.canConnectTo(host, port), Boolean::booleanValue);

		if (!canConnect) {
			stop(serverProcess, loggingThread);
			throw new IllegalStateException("Cannot connect to server: " + host + ":" + port);
		}

		return Pair.of(serverProcess, loggingThread);
	}

	/**
	 * Stops the forked legacy HTTPS server.
	 *
	 * @param serverProcess the server process
	 * @param loggingThread the logging thread
	 * @throws InterruptedException if interrupted while waiting for the logging thread to finish
	 */
	public static void stop(final Process serverProcess, final Thread loggingThread) throws InterruptedException {
		if (null != serverProcess) {
			serverProcess.destroy();
		}
		if (null != loggingThread) {
			loggingThread.join();
		}
	}

	/**
	 * Stops the forked legacy HTTPS server.
	 *
	 * @param serverInfo a pair containing the server process and the logging thread
	 * @throws InterruptedException if interrupted while waiting for the logging thread to finish
	 */
	public static void stop(final Pair<Process, Thread> serverInfo) throws InterruptedException {
		stop(serverInfo.left(), serverInfo.right());
	}
}

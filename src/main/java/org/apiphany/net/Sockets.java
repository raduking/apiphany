package org.apiphany.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.Random;

import org.apiphany.lang.Assert;
import org.morphix.reflection.Constructors;

/**
 * Utility methods for sockets.
 *
 * @author Radu Sebastian LAZIN
 */
public final class Sockets {

	/**
	 * The minimum port number.
	 */
	public static final int MIN_PORT = 1024;

	/**
	 * The maximum port number.
	 */
	public static final int MAX_PORT = 65535;

	/**
	 * Default timeout used when methods are called without a timeout. We enforce a default timeout because these are
	 * utility methods, for infinite timeout use 0 (zero) as a parameter or a zero duration.
	 */
	public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(2);

	/**
	 * Used for generating a random port number, no need for SecureRandom.
	 */
	private static final Random RANDOM = new Random(System.currentTimeMillis()); // NOSONAR see Javadoc

	/**
	 * Private constructor.
	 */
	private Sockets() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Finds an available TCP port between {@link #MIN_PORT} and {@link #MAX_PORT}.
	 *
	 * @return the available port
	 */
	public static int findAvailableTcpPort() {
		return findAvailableTcpPort(DEFAULT_TIMEOUT);
	}

	/**
	 * Finds an available TCP port between {@link #MIN_PORT} and {@link #MAX_PORT}.
	 *
	 * @param timeout the operation timeout
	 * @return the available port
	 */
	public static int findAvailableTcpPort(final Duration timeout) {
		return findAvailableTcpPort(Math.toIntExact(timeout.toMillis()));
	}

	/**
	 * Finds an available TCP port between {@link #MIN_PORT} and {@link #MAX_PORT}.
	 *
	 * @param timeout the operation timeout
	 * @return the available port
	 */
	public static int findAvailableTcpPort(final int timeout) {
		return findAvailableTcpPort(MIN_PORT, MAX_PORT, timeout);
	}

	/**
	 * Finds a random available TCP port between the given minimum port and maximum port.
	 *
	 * @param minPortRange minimum port
	 * @param maxPortRange maximum port
	 * @param timeout the operation timeout
	 * @return the available port
	 */
	public static int findAvailableTcpPort(final int minPortRange, final int maxPortRange, final int timeout) {
		Assert.thatArgument(minPortRange >= MIN_PORT, "Port minimum value must be greater than %d", MIN_PORT);
		Assert.thatArgument(maxPortRange <= MAX_PORT, "Port maximum value must be less than %d", MAX_PORT);
		Assert.thatArgument(maxPortRange >= minPortRange, "Max port range must be greater than minimum port range");

		int attempts = maxPortRange - minPortRange + 1;
		for (int i = 0; i < attempts; ++i) {
			int currentPort = nextPort(minPortRange, maxPortRange);
			if (isTcpPortAvailable(currentPort, timeout)) {
				return currentPort;
			}
		}
		throw new IllegalStateException("No available port in range " + minPortRange + "-" + maxPortRange);
	}

	/**
	 * Finds a random available TCP port between the given minimum port and maximum port.
	 *
	 * @param minPortRange minimum port
	 * @param maxPortRange maximum port
	 * @param timeout the operation timeout
	 * @return the available port
	 */
	public static int findAvailableTcpPort(final int minPortRange, final int maxPortRange, final Duration timeout) {
		return findAvailableTcpPort(minPortRange, maxPortRange, Math.toIntExact(timeout.toMillis()));
	}

	/**
	 * Finds a random available TCP port between the given minimum port and maximum port.
	 *
	 * @param minPortRange minimum port
	 * @param maxPortRange maximum port
	 * @return the available port
	 */
	public static int findAvailableTcpPort(final int minPortRange, final int maxPortRange) {
		return findAvailableTcpPort(minPortRange, maxPortRange, DEFAULT_TIMEOUT);
	}

	/**
	 * Checks if a TCP connection can be established to the given host and port within the specified timeout.
	 *
	 * @param host the host name or IP address to connect to
	 * @param port the TCP port to connect to
	 * @return true if connection is successful, false otherwise
	 */
	public static boolean canConnectTo(final String host, final int port) {
		return canConnectTo(host, port, DEFAULT_TIMEOUT);
	}

	/**
	 * Checks if a TCP connection can be established to the given host and port within the specified timeout.
	 *
	 * @param host the host name or IP address to connect to
	 * @param port the TCP port to connect to
	 * @param timeout the timeout duration
	 * @return true if connection is successful, false otherwise
	 */
	public static boolean canConnectTo(final String host, final int port, final Duration timeout) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), Math.toIntExact(timeout.toMillis()));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Check if the given TCP port is available.
	 *
	 * @param currentPort port to check
	 * @return true if the port is available
	 */
	public static boolean isTcpPortAvailable(final int currentPort) {
		return isTcpPortAvailable(currentPort, Math.toIntExact(DEFAULT_TIMEOUT.toMillis()));
	}

	/**
	 * Check if the given TCP port is available.
	 *
	 * @param currentPort port to check
	 * @param timeout the operation timeout
	 * @return true if the port is available
	 */
	public static boolean isTcpPortAvailable(final int currentPort, final int timeout) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), currentPort), timeout);
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	/**
	 * Returns a random port in the given range.
	 *
	 * @param minPortRange minimum port range
	 * @param maxPortRange maximum port range
	 * @return a random port in the given range
	 */
	private static int nextPort(final int minPortRange, final int maxPortRange) {
		int bound = maxPortRange - minPortRange + 1;
		return RANDOM.nextInt(bound) + minPortRange;
	}
}

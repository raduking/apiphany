package org.apiphany.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.Random;

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
     * @param timeout the operation timeout
     * @return the available port
     */
    public static int findAvailableTcpPort(final Duration timeout) {
        return findAvailableTcpPort((int) timeout.toMillis());
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
    	checkArgument(minPortRange >= MIN_PORT, "Port minimum value must be greater than %d", MIN_PORT);
    	checkArgument(maxPortRange <= MAX_PORT, "Port maximum value must be less than %d", MAX_PORT);
    	checkArgument(maxPortRange >= minPortRange, "Max port range must be greater than minimum port range");

        int currentPort = nextPort(minPortRange, maxPortRange);
        while (!isTcpPortAvailable(currentPort, timeout)) {
            currentPort = nextPort(minPortRange, maxPortRange);
        }
        return currentPort;
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
        int seed = maxPortRange - minPortRange;
        return RANDOM.nextInt(seed) + minPortRange;
    }

    /**
     * Checks the condition is true and throws {@link IllegalArgumentException} if the condition is false.
     * TODO: maybe put this method in a Preconditions class
     *
     * @param condition condition to check
     * @param errorMessageTemplate error message template
     * @param messageArguments error message arguments
     */
	private static void checkArgument(final boolean condition, final String errorMessageTemplate, final Object... messageArguments) {
		if (!condition) {
			throw new IllegalArgumentException(String.format(errorMessageTemplate, messageArguments));
		}
	}

}

package org.apiphany.net;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import org.apiphany.utils.Tests;
import org.junit.jupiter.api.Test;
import org.morphix.lang.function.ThrowingRunnable;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link Sockets}.
 *
 * @author Radu Sebastian LAZIN
 */
class SocketsTest {

	private static final int PORT = 6666;

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(Sockets.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldReturnFalseWhenPortInUse() throws Exception {
		int port = Sockets.findAvailableTcpPort();

		boolean result = onOccupiedPort(port, () -> Sockets.isTcpPortAvailable(port));

		assertFalse(result);
	}

	@Test
	void shouldReturnTrueIfPortIsAvailableWhenRangeIsOnePort() {
		int port = Sockets.findAvailableTcpPort(PORT, PORT);

		assertThat(port, equalTo(PORT));
	}

	@Test
	void shouldThrowExceptionIfPortIsNotAvailableWhenRangeIsOnePort() {
		IllegalStateException e =
				assertThrows(IllegalStateException.class, () -> onOccupiedPort(PORT, () -> Sockets.findAvailableTcpPort(PORT, PORT)));

		assertThat(e.getMessage(), equalTo("No available port in range " + PORT + "-" + PORT));
	}

	@Test
	void shouldReturnNextAvailablePort() throws Exception {
		int port = onOccupiedPort(PORT, () -> Sockets.findAvailableTcpPort(PORT, PORT + 666));

		assertThat(port, not(equalTo(PORT)));
	}

	@Test
	void shouldReturnTrueWhenCanConnectToPort() throws Exception {
		int port = Sockets.findAvailableTcpPort();
		boolean canConnect = onOccupiedPort(port, () -> Sockets.canConnectTo("localhost", port));

		assertThat(canConnect, equalTo(true));
	}

	@Test
	void shouldThrowExceptionIfMinPortRangeIsLowerThanMinPort() {
		IllegalArgumentException e =
				assertThrows(IllegalArgumentException.class, () -> Sockets.findAvailableTcpPort(Sockets.MIN_PORT - 1, Sockets.MAX_PORT));

		assertThat(e.getMessage(), equalTo("Port minimum value must be greater than " + Sockets.MIN_PORT));
	}

	@Test
	void shouldThrowExceptionIfMaxPortRangeIsBiggerThanMaxPort() {
		IllegalArgumentException e =
				assertThrows(IllegalArgumentException.class, () -> Sockets.findAvailableTcpPort(Sockets.MIN_PORT, Sockets.MAX_PORT + 1));

		assertThat(e.getMessage(), equalTo("Port maximum value must be less than " + Sockets.MAX_PORT));
	}

	@Test
	void shouldThrowExceptionIfMaxPortRangeIsLessThanMinPort() {
		IllegalArgumentException e =
				assertThrows(IllegalArgumentException.class, () -> Sockets.findAvailableTcpPort(Sockets.MAX_PORT, Sockets.MIN_PORT));

		assertThat(e.getMessage(), equalTo("Max port range must be greater than minimum port range"));
	}

	private static <T> T onOccupiedPort(final int port, final Supplier<T> resultSupplier) throws Exception {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			CountDownLatch serverReady = new CountDownLatch(1);

			Thread.ofVirtual().start(ThrowingRunnable.unchecked(() -> {
				serverReady.countDown();
				try (Socket clientSocket = serverSocket.accept()) {
					// empty
				} catch (Exception e) {
					// expected when test finishes
				}
			}));
			serverReady.await();

			return resultSupplier.get();
		}
	}
}

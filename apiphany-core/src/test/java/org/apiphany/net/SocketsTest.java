package org.apiphany.net;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.morphix.lang.function.ThrowingRunnable;
import org.morphix.lang.thread.Threads;
import org.morphix.reflection.Constructors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for {@link Sockets}.
 *
 * @author Radu Sebastian LAZIN
 */
class SocketsTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(SocketsTest.class);

	private static final int PORT = 6666;

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(Sockets.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldThrowExceptionOnCallingDefaultClassConstructor() {
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(Sockets.Default.class);
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
				assertThrows(IllegalArgumentException.class,
						() -> Sockets.findAvailableTcpPort(Sockets.Default.MIN_PORT - 1, Sockets.Default.MAX_PORT));

		assertThat(e.getMessage(), equalTo("Port minimum value must be greater than " + Sockets.Default.MIN_PORT));
	}

	@Test
	void shouldThrowExceptionIfMaxPortRangeIsBiggerThanMaxPort() {
		IllegalArgumentException e =
				assertThrows(IllegalArgumentException.class,
						() -> Sockets.findAvailableTcpPort(Sockets.Default.MIN_PORT, Sockets.Default.MAX_PORT + 1));

		assertThat(e.getMessage(), equalTo("Port maximum value must be less than " + Sockets.Default.MAX_PORT));
	}

	@Test
	void shouldThrowExceptionIfMaxPortRangeIsLessThanMinPort() {
		IllegalArgumentException e =
				assertThrows(IllegalArgumentException.class,
						() -> Sockets.findAvailableTcpPort(Sockets.Default.MAX_PORT, Sockets.Default.MIN_PORT));

		assertThat(e.getMessage(), equalTo("Max port range must be greater than minimum port range"));
	}

	@Test
	void shouldReturnFalseIfHostIsNullInCanConnectTo() {
		boolean result = Sockets.canConnectTo(null, Sockets.Default.MIN_PORT);

		assertFalse(result);
	}

	private static <T> T onOccupiedPort(final int port, final Supplier<T> resultSupplier) throws Exception {
		LOGGER.debug("[ON-OCCUPIED-PORT] BEGIN");
		Thread thread = null;
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			CountDownLatch serverReady = new CountDownLatch(1);

			thread = Thread.ofVirtual().start(ThrowingRunnable.unchecked(() -> {
				serverReady.countDown();
				try (Socket clientSocket = serverSocket.accept()) {
					// empty
				} catch (Exception e) {
					// expected when test finishes
					LOGGER.debug("Socket accept interrupted.", e);
				}
			}));
			serverReady.await();

			return resultSupplier.get();
		} finally {
			if (null != thread) {
				thread.interrupt();
				// wait for virtual thread to terminate
				Threads.safeJoin(thread);
			}
			LOGGER.debug("[ON-OCCUPIED-PORT] END");
		}
	}
}

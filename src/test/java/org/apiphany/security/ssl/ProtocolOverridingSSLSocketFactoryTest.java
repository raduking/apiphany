package org.apiphany.security.ssl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link ProtocolOverridingSSLSocketFactory}.
 *
 * @author Radu Sebastian LAZIN
 */
@SuppressWarnings("resource")
@ExtendWith(MockitoExtension.class)
class ProtocolOverridingSSLSocketFactoryTest {

	private static final int HTTP_PORT = 8080;
	private static final int SSL_PORT = 443;
	private static final String HOST = "example.com";
	private static final String CYPHER_1 = "TLS_RSA_WITH_AES_256_CBC_SHA";
	private static final String CYPHER_2 = "TLS_RSA_WITH_AES_128_CBC_SHA";

	private static final String[] ENABLED_PROTOCOLS = {
			"TLSv1.2",
			"TLSv1.3"
	};

	@Mock
	private SSLSocketFactory mockDelegateFactory;

	@Mock
	private SSLSocket mockSSLSocket;

	@Mock
	private Socket mockPlainSocket;

	@Mock
	private InputStream mockInputStream;

	private ProtocolOverridingSSLSocketFactory factory;

	@BeforeEach
	void setUp() {
		factory = new ProtocolOverridingSSLSocketFactory(mockDelegateFactory, ENABLED_PROTOCOLS);
	}

	@Test
	void shouldThrowNullPointerExceptionWhenDelegateFactoryIsNull() {
		assertThrows(NullPointerException.class,
				() -> new ProtocolOverridingSSLSocketFactory(null, ENABLED_PROTOCOLS));
	}

	@Test
	void shouldThrowNullPointerExceptionWhenEnabledProtocolsIsNull() {
		assertThrows(NullPointerException.class,
				() -> new ProtocolOverridingSSLSocketFactory(mockDelegateFactory, (String[]) null));
	}

	@Test
	void shouldReturnDelegateDefaultCipherSuites() {
		String[] expectedSuites = {
				CYPHER_2
		};
		when(mockDelegateFactory.getDefaultCipherSuites()).thenReturn(expectedSuites);

		String[] result = factory.getDefaultCipherSuites();

		assertThat(result, is(expectedSuites));
		verify(mockDelegateFactory).getDefaultCipherSuites();
	}

	@Test
	void shouldReturnDelegateSupportedCipherSuites() {
		String[] expectedSuites = {
				CYPHER_2,
				CYPHER_1
		};
		when(mockDelegateFactory.getSupportedCipherSuites()).thenReturn(expectedSuites);

		String[] result = factory.getSupportedCipherSuites();

		assertThat(result, is(expectedSuites));
		verify(mockDelegateFactory).getSupportedCipherSuites();
	}

	@Test
	void shouldSetEnabledProtocolsWhenCreatingSocket() throws IOException {
		when(mockDelegateFactory.createSocket()).thenReturn(mockSSLSocket);

		Socket result = factory.createSocket();

		verify(mockSSLSocket).setEnabledProtocols(ENABLED_PROTOCOLS);
		assertThat(result, is(mockSSLSocket));
	}

	@Test
	void shouldNotSetEnabledProtocolsWhenCreatingPlainSocket() throws IOException {
		when(mockDelegateFactory.createSocket()).thenReturn(mockPlainSocket);

		Socket result = factory.createSocket();

		assertThat(result, is(mockPlainSocket));
	}

	@Test
	void shouldSetEnabledProtocolsWhenCreatingSocketWithHostAndPort() throws IOException {
		when(mockDelegateFactory.createSocket(HOST, SSL_PORT)).thenReturn(mockSSLSocket);

		Socket result = factory.createSocket(HOST, SSL_PORT);

		verify(mockSSLSocket).setEnabledProtocols(ENABLED_PROTOCOLS);
		assertThat(result, is(mockSSLSocket));
	}

	@Test
	void shouldSetEnabledProtocolsWhenCreatingSocketWithInetAddress() throws IOException {
		InetAddress address = InetAddress.getLoopbackAddress();
		when(mockDelegateFactory.createSocket(address, SSL_PORT)).thenReturn(mockSSLSocket);

		Socket result = factory.createSocket(address, SSL_PORT);

		verify(mockSSLSocket).setEnabledProtocols(ENABLED_PROTOCOLS);
		assertThat(result, is(mockSSLSocket));
	}

	@Test
	void shouldSetEnabledProtocolsWhenCreatingSocketWithSocketHostPortAndAutoClose() throws IOException {
		when(mockDelegateFactory.createSocket(mockPlainSocket, HOST, SSL_PORT, true))
				.thenReturn(mockSSLSocket);

		Socket result = factory.createSocket(mockPlainSocket, HOST, SSL_PORT, true);

		verify(mockSSLSocket).setEnabledProtocols(ENABLED_PROTOCOLS);
		assertThat(result, is(mockSSLSocket));
	}

	@Test
	void shouldSetEnabledProtocolsWhenCreatingSocketWithHostPortLocalAddressAndLocalPort() throws IOException {
		InetAddress address = InetAddress.getLoopbackAddress();
		when(mockDelegateFactory.createSocket(HOST, SSL_PORT, address, HTTP_PORT))
				.thenReturn(mockSSLSocket);

		Socket result = factory.createSocket(HOST, SSL_PORT, address, HTTP_PORT);

		verify(mockSSLSocket).setEnabledProtocols(ENABLED_PROTOCOLS);
		assertThat(result, is(mockSSLSocket));
	}

	@Test
	void shouldSetEnabledProtocolsWhenCreatingSocketWithInetAddressPortLocalAddressAndLocalPort() throws IOException {
		InetAddress address = InetAddress.getLoopbackAddress();
		when(mockDelegateFactory.createSocket(address, SSL_PORT, address, HTTP_PORT))
				.thenReturn(mockSSLSocket);

		Socket result = factory.createSocket(address, SSL_PORT, address, HTTP_PORT);

		verify(mockSSLSocket).setEnabledProtocols(ENABLED_PROTOCOLS);
		assertThat(result, is(mockSSLSocket));
	}

	@Test
	void shouldSetEnabledProtocolsWhenCreatingSocketWithSocketInputStreamAndAutoClose() throws IOException {
		when(mockDelegateFactory.createSocket(mockPlainSocket, mockInputStream, true))
				.thenReturn(mockSSLSocket);

		Socket result = factory.createSocket(mockPlainSocket, mockInputStream, true);

		verify(mockSSLSocket).setEnabledProtocols(ENABLED_PROTOCOLS);
		assertThat(result, is(mockSSLSocket));
	}

	@Test
	void shouldReturnEnabledProtocols() {
		assertThat(factory.getEnabledProtocols(), is(ENABLED_PROTOCOLS));
	}

	@Test
	void shouldReturnDelegateFactory() {
		assertThat(factory.getSSLSocketFactory(), is(mockDelegateFactory));
	}

	@Test
	void shouldHandleEmptyProtocolArrayWithoutError() throws IOException {
		factory = new ProtocolOverridingSSLSocketFactory(mockDelegateFactory);
		when(mockDelegateFactory.createSocket()).thenReturn(mockSSLSocket);

		Socket result = factory.createSocket();

		verify(mockSSLSocket).setEnabledProtocols(new String[0]);
		assertThat(result, is(mockSSLSocket));
	}

	@Test
	void shouldHandleNullSocketGracefully() {
		assertNull(factory.setEnabledProtocols(null));
	}

	@Test
	void shouldPropagateIOExceptionWhenSocketCreationFails() throws IOException {
		when(mockDelegateFactory.createSocket()).thenThrow(new IOException("Connection failed"));

		assertThrows(IOException.class, () -> factory.createSocket());
	}

	@Test
	void shouldNotFailWhenSettingInvalidProtocols() throws IOException {
		when(mockDelegateFactory.createSocket()).thenReturn(mockSSLSocket);
		doThrow(new IllegalArgumentException("Unsupported protocol"))
				.when(mockSSLSocket).setEnabledProtocols(ENABLED_PROTOCOLS);

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> factory.createSocket());

		assertThat(e.getMessage(), equalTo("Unsupported protocol"));
	}

	@Test
	void shouldHandleConcurrentSocketCreationSafely() throws Exception {
		when(mockDelegateFactory.createSocket()).thenReturn(mockSSLSocket);

		int threadCount = 10;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		List<Future<Socket>> futures = new ArrayList<>();

		for (int i = 0; i < threadCount; i++) {
			futures.add(executor.submit(() -> factory.createSocket()));
		}

		for (Future<Socket> future : futures) {
			assertThat(future.get(), is(mockSSLSocket));
		}

		verify(mockSSLSocket, times(threadCount)).setEnabledProtocols(ENABLED_PROTOCOLS);
		executor.shutdown();
	}

	@Test
	void shouldNotReuseOrCacheSockets() throws IOException {
		SSLSocket anotherSSLSocket = mock(SSLSocket.class);
		when(mockDelegateFactory.createSocket())
				.thenReturn(mockSSLSocket)
				.thenReturn(anotherSSLSocket);

		Socket firstSocket = factory.createSocket();
		Socket secondSocket = factory.createSocket();

		assertThat(firstSocket, not(sameInstance(secondSocket)));
		verify(mockSSLSocket).setEnabledProtocols(ENABLED_PROTOCOLS);
		verify(anotherSSLSocket).setEnabledProtocols(ENABLED_PROTOCOLS);
	}

	@Test
	void shouldWorkWithCustomSSLSocketImplementations() throws IOException {
		CustomSSLSocket customSocket = mock(CustomSSLSocket.class);

		when(mockDelegateFactory.createSocket()).thenReturn(customSocket);

		Socket result = factory.createSocket();

		verify(customSocket).setEnabledProtocols(ENABLED_PROTOCOLS);
		assertThat(result, is(customSocket));
	}

	@Test
	void shouldHandleNullHostWhenCreatingSocket() throws IOException {
		when(mockDelegateFactory.createSocket(ArgumentMatchers.<String>isNull(), anyInt())).thenReturn(mockSSLSocket);

		Socket result = factory.createSocket((String) null, SSL_PORT);

		verify(mockSSLSocket).setEnabledProtocols(ENABLED_PROTOCOLS);
		assertThat(result, is(mockSSLSocket));
	}

	@Test
	void shouldHandleInvalidPortNumbers() throws IOException {
		when(mockDelegateFactory.createSocket(anyString(), eq(-1))).thenThrow(new IOException());

		assertThrows(IOException.class, () -> factory.createSocket(HOST, -1));
	}

	@Test
	void shouldNotCloseSocketOnProtocolSetting() throws IOException {
		when(mockDelegateFactory.createSocket()).thenReturn(mockSSLSocket);

		Socket result = factory.createSocket();

		// verify socket is not closed (caller's responsibility)
		verify(mockSSLSocket, never()).close();
		assertThat(result, is(mockSSLSocket));
	}

	static class CustomSSLSocket extends SSLSocket {

		@Override
		public String[] getSupportedCipherSuites() {
			return null;
		}

		@Override
		public String[] getEnabledCipherSuites() {
			return null;
		}

		@Override
		public void setEnabledCipherSuites(final String[] suites) {
			// empty
		}

		@Override
		public String[] getSupportedProtocols() {
			return null;
		}

		@Override
		public String[] getEnabledProtocols() {
			return null;
		}

		@Override
		public void setEnabledProtocols(final String[] protocols) {
			// empty
		}

		@Override
		public SSLSession getSession() {
			return null;
		}

		@Override
		public void addHandshakeCompletedListener(final HandshakeCompletedListener listener) {
			// empty
		}

		@Override
		public void removeHandshakeCompletedListener(final HandshakeCompletedListener listener) {
			// empty
		}

		@Override
		public void startHandshake() throws IOException {
			// empty
		}

		@Override
		public void setUseClientMode(final boolean mode) {
			// empty
		}

		@Override
		public boolean getUseClientMode() {
			return false;
		}

		@Override
		public void setNeedClientAuth(final boolean need) {
			// empty
		}

		@Override
		public boolean getNeedClientAuth() {
			return false;
		}

		@Override
		public void setWantClientAuth(final boolean want) {
			// empty
		}

		@Override
		public boolean getWantClientAuth() {
			return false;
		}

		@Override
		public void setEnableSessionCreation(final boolean flag) {
			// empty
		}

		@Override
		public boolean getEnableSessionCreation() {
			return false;
		}
	}
}

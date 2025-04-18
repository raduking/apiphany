package org.apiphany.security.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * SSLSocketFactory doesn't seem to properly set the protocols on the SSL sockets that it creates which causes an SSLv2
 * client hello message during handshake, even when only TLSv1 is enabled. This only appears to be an issue on the
 * client sockets, not the server sockets.
 * <p>
 * This class wraps the SSLSocketFactory ensuring that the SSLSocket is properly configured.
 * <p>
 * Every {@code createSocket} method returns a {@link Socket} and the caller is always responsible for closing it.
 *
 * @author Radu Sebastian LAZIN
 */
public class ProtocolOverridingSSLSocketFactory extends SSLSocketFactory {

	/**
	 * Delegate SSL socket factory.
	 */
	private final SSLSocketFactory sslSocketFactory;

	/**
	 * Protocols to enable on socket.
	 */
	private final String[] enabledProtocols;

	/**
	 * Constructor to set the delegate and the protocols needed to be enabled on the socket.
	 *
	 * @param sslSocketFactory SSLConnectionFactory delegate
	 * @param enabledProtocols protocols to enable
	 */
	public ProtocolOverridingSSLSocketFactory(final SSLSocketFactory sslSocketFactory, final String... enabledProtocols) {
		this.sslSocketFactory = Objects.requireNonNull(sslSocketFactory, "sslSocketFactory must not be null.");
		this.enabledProtocols = Objects.requireNonNull(enabledProtocols, "enabledProtocols must not be null.");
	}

	/**
	 * @see SSLSocketFactory#getDefaultCipherSuites()
	 */
	@Override
	public String[] getDefaultCipherSuites() {
		return getSSLSocketFactory().getDefaultCipherSuites();
	}

	/**
	 * @see SSLSocketFactory#getSupportedCipherSuites()
	 */
	@Override
	public String[] getSupportedCipherSuites() {
		return getSSLSocketFactory().getSupportedCipherSuites();
	}

	/**
	 * @see SSLSocketFactory#createSocket()
	 */
	@SuppressWarnings("resource")
	@Override
	public Socket createSocket() throws IOException {
		final Socket createdSocket = getSSLSocketFactory().createSocket();
		return setEnabledProtocols(createdSocket);
	}

	/**
	 * @see SSLSocketFactory#createSocket(Socket, String, int, boolean)
	 */
	@SuppressWarnings("resource")
	@Override
	public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose) throws IOException {
		final Socket createdSocket = getSSLSocketFactory().createSocket(socket, host, port, autoClose);
		return setEnabledProtocols(createdSocket);
	}

	/**
	 * @see SSLSocketFactory#createSocket(String, int)
	 */
	@SuppressWarnings("resource")
	@Override
	public Socket createSocket(final String host, final int port) throws IOException {
		final Socket createdSocket = getSSLSocketFactory().createSocket(host, port);
		return setEnabledProtocols(createdSocket);
	}

	/**
	 * @see SSLSocketFactory#createSocket(String, int, InetAddress, int)
	 */
	@SuppressWarnings("resource")
	@Override
	public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort) throws IOException {
		final Socket createdSocket = getSSLSocketFactory().createSocket(host, port, localAddress, localPort);
		return setEnabledProtocols(createdSocket);
	}

	/**
	 * @see SSLSocketFactory#createSocket(InetAddress, int)
	 */
	@SuppressWarnings("resource")
	@Override
	public Socket createSocket(final InetAddress host, final int port) throws IOException {
		final Socket createdSocket = getSSLSocketFactory().createSocket(host, port);
		return setEnabledProtocols(createdSocket);
	}

	/**
	 * @see SSLSocketFactory#createSocket(InetAddress, int, InetAddress, int)
	 */
	@SuppressWarnings("resource")
	@Override
	public Socket createSocket(final InetAddress host, final int port, final InetAddress localAddress, final int localPort) throws IOException {
		final Socket createdSocket = getSSLSocketFactory().createSocket(host, port, localAddress, localPort);
		return setEnabledProtocols(createdSocket);
	}

	/**
	 * @see SSLSocketFactory#createSocket(Socket, InputStream, boolean)
	 */
	@SuppressWarnings("resource")
	@Override
	public Socket createSocket(final Socket s, final InputStream consumed, final boolean autoClose) throws IOException {
		final Socket createdSocket = getSSLSocketFactory().createSocket(s, consumed, autoClose);
		return setEnabledProtocols(createdSocket);
	}

	/**
	 * Enabled protocols.
	 *
	 * @return enabled protocols
	 */
	public String[] getEnabledProtocols() {
		return enabledProtocols;
	}

	/**
	 * Returns the delegate
	 *
	 * @return delegate
	 */
	protected SSLSocketFactory getSSLSocketFactory() {
		return sslSocketFactory;
	}

	/**
	 * Set the {@link SSLSocket#getEnabledProtocols() enabled protocols} to {@link #enabledProtocols} if the
	 * <code>socket</code> is a {@link SSLSocket}
	 *
	 * @param socket the socket
	 * @return the socket
	 */
	protected Socket setEnabledProtocols(final Socket socket) {
		if (socket instanceof SSLSocket sslSocket) {
			sslSocket.setEnabledProtocols(getEnabledProtocols());
		}
		return socket;
	}

}

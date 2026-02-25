package org.apiphany.client.http;

import java.util.Objects;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.util.Timeout;
import org.apiphany.json.JsonBuilder;

/**
 * Apache HTTP Client 5 properties.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApacheHC5Properties {

	/**
	 * The root property prefix for Apache HTTP Client 5 configuration.
	 */
	public static final String ROOT = "http-client5";

	/**
	 * Specific Apache HTTP Client 5 connection properties.
	 */
	private Connection connection = new Connection();

	/**
	 * Specific Apache HTTP Client 5 request properties.
	 */
	private Request request = new Request();

	/**
	 * Specific Apache HTTP Client 5 connection request properties.
	 */
	private ConnectionRequest connectionRequest = new ConnectionRequest();

	/**
	 * Specific Apache HTTP Client 5 connect properties.
	 */
	private Connect connect = new Connect();

	/**
	 * Specific Apache HTTP Client 5 socket properties.
	 */
	private Socket socket = new Socket();

	/**
	 * Default constructor.
	 */
	public ApacheHC5Properties() {
		// empty
	}

	/**
	 * @see #toString()
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof ApacheHC5Properties that) {
			return Objects.equals(this.connection, that.connection) &&
					Objects.equals(this.request, that.request) &&
					Objects.equals(this.connectionRequest, that.connectionRequest) &&
					Objects.equals(this.connect, that.connect) &&
					Objects.equals(this.socket, that.socket);
		}
		return false;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(connection, request, connectionRequest, connect, socket);
	}

	/**
	 * Returns the request properties.
	 *
	 * @return the request properties
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * Sets the request properties.
	 *
	 * @param request properties to set
	 */
	public void setRequest(final Request request) {
		this.request = request;
	}

	/**
	 * Returns the connection properties.
	 *
	 * @return the connection properties
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Sets the connection properties.
	 *
	 * @param connection properties to set
	 */
	public void setConnection(final Connection connection) {
		this.connection = connection;
	}

	/**
	 * Returns the connect properties.
	 *
	 * @return the connect properties
	 */
	public Connect getConnect() {
		return connect;
	}

	/**
	 * Set the connect properties.
	 *
	 * @param connect connect properties to set.
	 */
	public void setConnect(final Connect connect) {
		this.connect = connect;
	}

	/**
	 * Returns the connection request properties.
	 *
	 * @return the connection request properties
	 */
	public ConnectionRequest getConnectionRequest() {
		return connectionRequest;
	}

	/**
	 * Set the connection request properties.
	 *
	 * @param connectionRequest connection request properties to set.
	 */
	public void setConnectionRequest(final ConnectionRequest connectionRequest) {
		this.connectionRequest = connectionRequest;
	}

	/**
	 * Returns the socket properties.
	 *
	 * @return the socket properties
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * Set the socket properties.
	 *
	 * @param socket socket properties to set.
	 */
	public void setSocket(final Socket socket) {
		this.socket = socket;
	}

	/**
	 * Client connection and pooling properties.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Connection {

		/**
		 * Default time to live: 30 seconds.
		 */
		public static final Timeout DEFAULT_TIME_TO_LIVE = Timeout.ofSeconds(30);

		/**
		 * Default maximum total connections per pool: 100.
		 */
		public static final int DEFAULT_MAX_TOTAL = 100;

		/**
		 * Default maximum per route connections: 100.
		 */
		public static final int DEFAULT_MAX_PER_ROUTE = 100;

		/**
		 * Default flag indicating whether redirects should be followed: false.
		 */
		public static final boolean DEFAULT_FOLLOW_REDIRECTS = false;

		/**
		 * Time to live.
		 */
		private Timeout timeToLive = DEFAULT_TIME_TO_LIVE;

		/**
		 * Maximum total connections.
		 */
		private int maxTotal = DEFAULT_MAX_TOTAL;

		/**
		 * Maximum per route connections.
		 */
		private int maxPerRoute = DEFAULT_MAX_PER_ROUTE;

		/**
		 * Flag indicating whether redirects should be followed.
		 */
		private boolean followRedirects = DEFAULT_FOLLOW_REDIRECTS;

		/**
		 * Default constructor.
		 */
		protected Connection() {
			// empty
		}

		/**
		 * Returns a JSON representation of this {@link Connection} object.
		 *
		 * @return a JSON string representing this object.
		 */
		@Override
		public String toString() {
			return JsonBuilder.toJson(this);
		}

		/**
		 * @see Object#equals(Object)
		 */
		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof Connection that) {
				return Objects.equals(this.timeToLive, that.timeToLive) &&
						this.maxTotal == that.maxTotal &&
						this.maxPerRoute == that.maxPerRoute;
			}
			return false;
		}

		/**
		 * @see Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(timeToLive, maxTotal, maxPerRoute);
		}

		/**
		 * Returns the maximum per route connections.
		 *
		 * @return the maximum per route connections.
		 */
		public int getMaxPerRoute() {
			return maxPerRoute;
		}

		/**
		 * Sets the maximum per route connections.
		 *
		 * @param maxPerRoute the maximum per route connections to set.
		 */
		public void setMaxPerRoute(final int maxPerRoute) {
			this.maxPerRoute = maxPerRoute;
		}

		/**
		 * Returns the maximum total connections.
		 *
		 * @return the maximum total connections.
		 */
		public int getMaxTotal() {
			return maxTotal;
		}

		/**
		 * Sets the maximum total connections.
		 *
		 * @param maxTotal the maximum total connections to set.
		 */
		public void setMaxTotal(final int maxTotal) {
			this.maxTotal = maxTotal;
		}

		/**
		 * Returns the time to live timeout.
		 *
		 * @return the time to live timeout.
		 */
		public Timeout getTimeToLive() {
			return timeToLive;
		}

		/**
		 * Sets the time to live timeout.
		 *
		 * @param timeToLive time to live to set.
		 */
		public void setTimeToLive(final Timeout timeToLive) {
			this.timeToLive = timeToLive;
		}

		/**
		 * Returns whether redirects should be followed.
		 *
		 * @return true if redirects should be followed, false otherwise
		 */
		public boolean isFollowRedirects() {
			return followRedirects;
		}

		/**
		 * Sets whether redirects should be followed.
		 *
		 * @param followRedirects true to follow redirects, false to not follow
		 */
		public void setFollowRedirects(final boolean followRedirects) {
			this.followRedirects = followRedirects;
		}
	}

	/**
	 * Apache HTTP Client 5 Request properties, the defaults will be populated from {@link RequestConfig#DEFAULT}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Request {

		/**
		 * Flag indicating whether protocol upgrade is enabled. Defaults to the value from {@link RequestConfig#DEFAULT}.
		 */
		private boolean protocolUpgradeEnabled = RequestConfig.DEFAULT.isProtocolUpgradeEnabled();

		/**
		 * The HTTP protocol version to use. Defaults to the value from {@link HttpVersion#DEFAULT}.
		 */
		private String protocolVersion = HttpVersion.DEFAULT.format();

		/**
		 * Request timeout is {@link Timeout#INFINITE} by default.
		 */
		private Timeout requestTimeout = Timeout.INFINITE;

		/**
		 * Default constructor.
		 */
		protected Request() {
			// empty
		}

		/**
		 * Returns a JSON representation of this Request object.
		 *
		 * @return JSON string representation of this object
		 */
		@Override
		public String toString() {
			return JsonBuilder.toJson(this);
		}

		/**
		 * @see Object#equals(Object)
		 */
		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof Request that) {
				return this.protocolUpgradeEnabled == that.protocolUpgradeEnabled
						&& Objects.equals(this.protocolVersion, that.protocolVersion)
						&& Objects.equals(this.requestTimeout, that.requestTimeout);
			}
			return false;
		}

		/**
		 * @see Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(protocolUpgradeEnabled, protocolVersion, requestTimeout);
		}

		/**
		 * Gets whether protocol upgrade is enabled.
		 *
		 * @return true if protocol upgrade is enabled, false otherwise
		 */
		public boolean isProtocolUpgradeEnabled() {
			return protocolUpgradeEnabled;
		}

		/**
		 * Sets whether protocol upgrade should be enabled.
		 *
		 * @param protocolUpgradeEnabled true to enable protocol upgrade, false to disable
		 */
		public void setProtocolUpgradeEnabled(final boolean protocolUpgradeEnabled) {
			this.protocolUpgradeEnabled = protocolUpgradeEnabled;
		}

		/**
		 * Gets the protocol version as a string.
		 *
		 * @return the protocol version string
		 */
		public String getProtocolVersion() {
			return protocolVersion;
		}

		/**
		 * Gets the protocol version as a {@link ProtocolVersion} object. If parsing fails, returns the default HTTP version.
		 *
		 * @return the protocol version as ProtocolVersion object
		 */
		public ProtocolVersion getHttpProtocolVersion() {
			try {
				return ProtocolVersion.parse(protocolVersion);
			} catch (ParseException pe) {
				return HttpVersion.DEFAULT;
			}
		}

		/**
		 * Sets the protocol version.
		 *
		 * @param protocolVersion the protocol version string to set
		 */
		public void setProtocolVersion(final String protocolVersion) {
			this.protocolVersion = protocolVersion;
		}

		/**
		 * Returns the request timeout.
		 *
		 * @return the request timeout.
		 */
		public Timeout getRequestTimeout() {
			return requestTimeout;
		}

		/**
		 * Sets the request timeout.
		 *
		 * @param requestTimeout timeout to set
		 */
		public void setRequestTimeout(final Timeout requestTimeout) {
			this.requestTimeout = requestTimeout;
		}
	}

	/**
	 * Apache HTTP Client 5 connect properties.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Connect {

		/**
		 * Connect timeout is {@link Timeout#INFINITE} by default.
		 */
		private Timeout timeout = Timeout.INFINITE;

		/**
		 * Default constructor.
		 */
		protected Connect() {
			// empty
		}

		/**
		 * Returns a JSON representation of this Connect object.
		 *
		 * @return JSON string representation of this object
		 */
		@Override
		public String toString() {
			return JsonBuilder.toJson(this);
		}

		/**
		 * @see Object#equals(Object)
		 */
		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof Connect that) {
				return Objects.equals(this.timeout, that.timeout);
			}
			return false;
		}

		/**
		 * @see Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(timeout);
		}

		/**
		 * Returns the connect timeout.
		 *
		 * @return the connect timeout.
		 */
		public Timeout getTimeout() {
			return timeout;
		}

		/**
		 * Sets the connect timeout.
		 *
		 * @param timeout timeout to set
		 */
		public void setTimeout(final Timeout timeout) {
			this.timeout = timeout;
		}
	}

	/**
	 * Apache HTTP Client 5 connection request properties.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class ConnectionRequest {

		/**
		 * Connection request timeout is {@link Timeout#INFINITE} by default.
		 */
		private Timeout timeout = Timeout.INFINITE;

		/**
		 * Default constructor.
		 */
		protected ConnectionRequest() {
			// empty
		}

		/**
		 * Returns a JSON representation of this ConnectionRequest object.
		 *
		 * @return JSON string representation of this object
		 */
		@Override
		public String toString() {
			return JsonBuilder.toJson(this);
		}

		/**
		 * @see Object#equals(Object)
		 */
		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof ConnectionRequest that) {
				return Objects.equals(this.timeout, that.timeout);
			}
			return false;
		}

		/**
		 * @see Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(timeout);
		}

		/**
		 * Returns the connection request timeout.
		 *
		 * @return the connection request timeout.
		 */
		public Timeout getTimeout() {
			return timeout;
		}

		/**
		 * Sets the connection request timeout.
		 *
		 * @param timeout timeout to set
		 */
		public void setTimeout(final Timeout timeout) {
			this.timeout = timeout;
		}
	}

	/**
	 * Apache HTTP Client 5 socket properties.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Socket {

		/**
		 * Socket timeout is {@link Timeout#INFINITE} by default.
		 */
		private Timeout timeout = Timeout.INFINITE;

		/**
		 * Default constructor.
		 */
		protected Socket() {
			// empty
		}

		/**
		 * Returns a JSON representation of this Socket object.
		 *
		 * @return JSON string representation of this object
		 */
		@Override
		public String toString() {
			return JsonBuilder.toJson(this);
		}

		/**
		 * @see Object#equals(Object)
		 */
		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof Socket that) {
				return Objects.equals(this.timeout, that.timeout);
			}
			return false;
		}

		/**
		 * @see Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(timeout);
		}

		/**
		 * Returns the socket timeout.
		 *
		 * @return the socket timeout.
		 */
		public Timeout getTimeout() {
			return timeout;
		}

		/**
		 * Sets the socket timeout.
		 *
		 * @param timeout timeout to set
		 */
		public void setTimeout(final Timeout timeout) {
			this.timeout = timeout;
		}
	}
}

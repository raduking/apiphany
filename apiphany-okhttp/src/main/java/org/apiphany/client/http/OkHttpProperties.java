package org.apiphany.client.http;

import java.time.Duration;
import java.util.Objects;

import org.apiphany.http.OkHttpLibrary;
import org.apiphany.json.JsonBuilder;

/**
 * OkHttp properties.
 * <p>
 * Unlike Apache HttpClient 5, OkHttp has a single global {@code ConnectionPool} (not configurable per-route) and splits
 * read/write timeouts instead of exposing a single socket timeout. There is also no connection-pool checkout timeout
 * concept equivalent to Apache HttpClient 5's connection request timeout, so no such section is present here.
 *
 * @author Radu Sebastian LAZIN
 */
public class OkHttpProperties {

	/**
	 * The root property prefix for OkHttp configuration.
	 */
	public static final String ROOT = OkHttpLibrary.CLIENT_NAME;

	/**
	 * Specific OkHttp connection properties.
	 */
	private Connection connection = new Connection();

	/**
	 * Specific OkHttp request properties.
	 */
	private Request request = new Request();

	/**
	 * Specific OkHttp connect properties.
	 */
	private Connect connect = new Connect();

	/**
	 * Specific OkHttp read properties.
	 */
	private Read read = new Read();

	/**
	 * Specific OkHttp write properties.
	 */
	private Write write = new Write();

	/**
	 * Default constructor.
	 */
	public OkHttpProperties() {
		// empty
	}

	/**
	 * Returns a new instance of {@link OkHttpProperties} with default values.
	 *
	 * @return a new instance of OkHttpProperties with default values
	 */
	public static OkHttpProperties withDefaults() {
		return new OkHttpProperties();
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
		if (o instanceof OkHttpProperties that) {
			return Objects.equals(this.connection, that.connection) &&
					Objects.equals(this.request, that.request) &&
					Objects.equals(this.connect, that.connect) &&
					Objects.equals(this.read, that.read) &&
					Objects.equals(this.write, that.write);
		}
		return false;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(connection, request, connect, read, write);
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
	 * Returns the read properties.
	 *
	 * @return the read properties
	 */
	public Read getRead() {
		return read;
	}

	/**
	 * Set the read properties.
	 *
	 * @param read read properties to set.
	 */
	public void setRead(final Read read) {
		this.read = read;
	}

	/**
	 * Returns the write properties.
	 *
	 * @return the write properties
	 */
	public Write getWrite() {
		return write;
	}

	/**
	 * Set the write properties.
	 *
	 * @param write write properties to set.
	 */
	public void setWrite(final Write write) {
		this.write = write;
	}

	/**
	 * Client connection and pooling properties.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Connection {

		/**
		 * Constants for default connection property values.
		 *
		 * @author Radu Sebastian LAZIN
		 */
		public static class Default {

			/**
			 * Default keep alive duration: 5 minutes, OkHttp's own {@code ConnectionPool} default.
			 */
			public static final Duration KEEP_ALIVE_DURATION = Duration.ofMinutes(5);

			/**
			 * Default maximum idle connections: 5, OkHttp's own {@code ConnectionPool} default.
			 */
			public static final int MAX_IDLE_CONNECTIONS = 5;

			/**
			 * Default flag indicating whether redirects should be followed: false.
			 */
			public static final boolean FOLLOW_REDIRECTS = false;

			/**
			 * Hide constructor.
			 */
			private Default() {
				// empty
			}
		}

		/**
		 * Keep alive duration for idle connections in the pool.
		 */
		private Duration keepAliveDuration = Default.KEEP_ALIVE_DURATION;

		/**
		 * Maximum idle connections kept in the pool.
		 */
		private int maxIdleConnections = Default.MAX_IDLE_CONNECTIONS;

		/**
		 * Flag indicating whether redirects should be followed.
		 */
		private boolean followRedirects = Default.FOLLOW_REDIRECTS;

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
				return Objects.equals(this.keepAliveDuration, that.keepAliveDuration) &&
						this.maxIdleConnections == that.maxIdleConnections &&
						this.followRedirects == that.followRedirects;
			}
			return false;
		}

		/**
		 * @see Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(keepAliveDuration, maxIdleConnections, followRedirects);
		}

		/**
		 * Returns the maximum idle connections.
		 *
		 * @return the maximum idle connections.
		 */
		public int getMaxIdleConnections() {
			return maxIdleConnections;
		}

		/**
		 * Sets the maximum idle connections.
		 *
		 * @param maxIdleConnections the maximum idle connections to set.
		 */
		public void setMaxIdleConnections(final int maxIdleConnections) {
			this.maxIdleConnections = maxIdleConnections;
		}

		/**
		 * Returns the keep alive duration.
		 *
		 * @return the keep alive duration.
		 */
		public Duration getKeepAliveDuration() {
			return keepAliveDuration;
		}

		/**
		 * Sets the keep alive duration.
		 *
		 * @param keepAliveDuration keep alive duration to set.
		 */
		public void setKeepAliveDuration(final Duration keepAliveDuration) {
			this.keepAliveDuration = keepAliveDuration;
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
	 * OkHttp request properties.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Request {

		/**
		 * Call timeout is {@link Duration#ZERO} (no timeout) by default, OkHttp's own default.
		 */
		private Duration callTimeout = Duration.ZERO;

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
				return Objects.equals(this.callTimeout, that.callTimeout);
			}
			return false;
		}

		/**
		 * @see Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(callTimeout);
		}

		/**
		 * Returns the call timeout, i.e. the overall timeout for the whole call (connect + write + server processing + read).
		 *
		 * @return the call timeout.
		 */
		public Duration getCallTimeout() {
			return callTimeout;
		}

		/**
		 * Sets the call timeout.
		 *
		 * @param callTimeout timeout to set
		 */
		public void setCallTimeout(final Duration callTimeout) {
			this.callTimeout = callTimeout;
		}
	}

	/**
	 * OkHttp connect properties.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Connect {

		/**
		 * Connect timeout is 10 seconds by default, OkHttp's own default.
		 */
		private Duration timeout = Duration.ofSeconds(10);

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
		public Duration getTimeout() {
			return timeout;
		}

		/**
		 * Sets the connect timeout.
		 *
		 * @param timeout timeout to set
		 */
		public void setTimeout(final Duration timeout) {
			this.timeout = timeout;
		}
	}

	/**
	 * OkHttp read properties.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Read {

		/**
		 * Read timeout is 10 seconds by default, OkHttp's own default.
		 */
		private Duration timeout = Duration.ofSeconds(10);

		/**
		 * Default constructor.
		 */
		protected Read() {
			// empty
		}

		/**
		 * Returns a JSON representation of this Read object.
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
			if (o instanceof Read that) {
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
		 * Returns the read timeout.
		 *
		 * @return the read timeout.
		 */
		public Duration getTimeout() {
			return timeout;
		}

		/**
		 * Sets the read timeout.
		 *
		 * @param timeout timeout to set
		 */
		public void setTimeout(final Duration timeout) {
			this.timeout = timeout;
		}
	}

	/**
	 * OkHttp write properties.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Write {

		/**
		 * Write timeout is 10 seconds by default, OkHttp's own default.
		 */
		private Duration timeout = Duration.ofSeconds(10);

		/**
		 * Default constructor.
		 */
		protected Write() {
			// empty
		}

		/**
		 * Returns a JSON representation of this Write object.
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
			if (o instanceof Write that) {
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
		 * Returns the write timeout.
		 *
		 * @return the write timeout.
		 */
		public Duration getTimeout() {
			return timeout;
		}

		/**
		 * Sets the write timeout.
		 *
		 * @param timeout timeout to set
		 */
		public void setTimeout(final Duration timeout) {
			this.timeout = timeout;
		}
	}
}

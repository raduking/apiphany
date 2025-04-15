package org.apiphany.client;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Maps;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.thread.Threads;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Fields;
import org.morphix.reflection.Reflection;

/**
 * Base class for client properties, it can also be used as a stand-alone class to import common properties for clients.
 * It can also hold child client properties if needed.
 *
 * @author Radu Sebastian LAZIN
 */
public class ClientProperties {

	/**
	 * Indicates whether the client is enabled or disabled. Defaults to true.
	 */
	private boolean enabled = true;

	/**
	 * Configuration for client timeouts.
	 */
	private Timeout timeout = new Timeout();

	/**
	 * Configuration for client connections and pooling.
	 */
	private Connection connection = new Connection();

	/**
	 * Configuration for client compression.
	 */
	private Compression compression = new Compression();

	/**
	 * A map of client-specific properties.
	 */
	private Map<String, Object> client;

	/**
	 * A map of custom properties for additional configurations.
	 */
	private Map<String, Object> custom;

	/**
	 * Returns a JSON representation of this {@link ClientProperties} object.
	 *
	 * @return a JSON string representing this object.
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns whether the client is enabled.
	 *
	 * @return true if the client is enabled, false otherwise.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets whether the client is enabled.
	 *
	 * @param enabled true to enable the client, false to disable it.
	 */
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns whether the client is disabled.
	 *
	 * @return true if the client is disabled, false otherwise.
	 */
	public boolean isDisabled() {
		return !isEnabled();
	}

	/**
	 * Returns the timeout configuration for the client.
	 *
	 * @return the timeout configuration.
	 */
	public Timeout getTimeout() {
		return timeout;
	}

	/**
	 * Sets the timeout configuration for the client.
	 *
	 * @param timeout the timeout configuration to set.
	 */
	public void setTimeout(final Timeout timeout) {
		this.timeout = timeout;
	}

	/**
	 * Returns the connection configuration for the client.
	 *
	 * @return the connection configuration.
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Sets the connection configuration for the client.
	 *
	 * @param connections the connection configuration to set.
	 */
	public void setConnection(final Connection connections) {
		this.connection = connections;
	}

	/**
	 * Returns the compression configuration for the client.
	 *
	 * @return the compression configuration.
	 */
	public Compression getCompression() {
		return compression;
	}

	/**
	 * Sets the compression configuration for the client.
	 *
	 * @param compression the compression configuration to set.
	 */
	public void setCompression(final Compression compression) {
		this.compression = compression;
	}

	/**
	 * Retrieves client-specific properties for a given client prefix and name.
	 *
	 * @param <T> the type of the client properties.
	 * @param clientPrefix the prefix for the client properties.
	 * @param clientName the name of the client.
	 * @return the client properties as an instance of the specified type.
	 */
	public <T extends ClientProperties> T getClientProperties(final String clientPrefix, final String clientName) {
		String fullPath = clientPrefix + "." + clientName;
		Map<String, Object> clientProperties = getPropertiesMap(this::getClient, fullPath);
		return JavaObjects.cast(JsonBuilder.fromMap(clientProperties, getClass(), e -> {
			throw new IllegalStateException("Error reading properties from: " + fullPath);
		}));
	}

	/**
	 * Helper method to retrieve a nested properties map from a root supplier and a full path.
	 *
	 * @param rootSupplier the supplier for the root properties map.
	 * @param fullPath the full path to the nested properties.
	 * @return the nested properties map.
	 */
	private static Map<String, Object> getPropertiesMap(final Supplier<Map<String, Object>> rootSupplier, final String fullPath) {
		Map<String, Object> actualProps = rootSupplier.get();
		if (null == actualProps) {
			return Collections.emptyMap();
		}
		String[] paths = fullPath.split("\\.");
		for (String path : paths) {
			Object props = actualProps.get(path);
			if (props instanceof Map<?, ?> mapProps) {
				actualProps = JavaObjects.cast(mapProps);
			}
			if (null == props) {
				break;
			}
		}
		return Maps.convertKeys(actualProps, Strings::fromLowerCamelToKebabCase);
	}

	/**
	 * Returns the client-specific properties map.
	 *
	 * @return the client properties map.
	 */
	public Map<String, Object> getClient() {
		return client;
	}

	/**
	 * Sets the client-specific properties map.
	 *
	 * @param client the client properties map to set.
	 */
	public void setClient(final Map<String, Object> client) {
		this.client = client;
	}

	/**
	 * Returns the custom properties map.
	 *
	 * @return the custom properties map.
	 */
	public Map<String, Object> getCustom() {
		return custom;
	}

	/**
	 * Sets the custom properties map.
	 *
	 * @param custom the custom properties map to set.
	 */
	public void setCustom(final Map<String, Object> custom) {
		this.custom = custom;
	}

	/**
	 * Retrieves custom properties for a given prefix and maps them to the specified class.
	 *
	 * @param <T> the type of the custom properties.
	 * @param prefix the prefix for the custom properties.
	 * @param cls the class to map the properties to.
	 * @return the custom properties as an instance of the specified class.
	 */
	public <T> T getCustomProperties(final String prefix, final Class<T> cls) {
		if (null == custom) {
			return null;
		}
		Map<String, Object> properties = getPropertiesMap(this::getCustom, prefix);
		return JsonBuilder.fromMap(properties, cls, Threads.noConsumer());
	}

	/**
	 * Retrieves custom properties mapped to the specified class using a static ROOT field as the prefix.
	 *
	 * @param <T> the type of the custom properties.
	 * @param cls the class to map the properties to.
	 * @return the custom properties as an instance of the specified class.
	 */
	public <T> T getCustomProperties(final Class<T> cls) {
		if (null == custom) {
			return null;
		}
		String prefix = Fields.IgnoreAccess.getStatic(cls, "ROOT");
		Map<String, Object> properties = getPropertiesMap(this::getCustom, prefix);
		return JsonBuilder.fromMap(properties, cls, Threads.noConsumer());
	}

	/**
	 * Client connection and pooling properties.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Connection {

		/**
		 * Default maximum total connections per pool.
		 */
		public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 100;

		/**
		 * Default maximum per route connections.
		 */
		public static final int DEFAULT_MAX_PER_ROUTE_CONNECTIONS = 100;

		/**
		 * Maximum total connections.
		 */
		private int maxTotal = DEFAULT_MAX_TOTAL_CONNECTIONS;

		/**
		 * Maximum per route connections.
		 */
		private int maxPerRoute = DEFAULT_MAX_PER_ROUTE_CONNECTIONS;

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
	}

	/**
	 * Client timeout properties.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Timeout {

		/**
		 * Constant representing a disabled timeout.
		 */
		public static final int DISABLED = 0;

		/**
		 * Connection timeout in milliseconds.
		 */
		private int connectTimeout = DISABLED;

		/**
		 * Connection request timeout in milliseconds.
		 */
		private int connectionRequestTimeout = DISABLED;

		/**
		 * Socket timeout in milliseconds.
		 */
		private int socketTimeout = DISABLED;

		/**
		 * Default constructor.
		 */
		protected Timeout() {
			// empty
		}

		/**
		 * Constructor for building a {@link Timeout} instance using a builder.
		 *
		 * @param builder the builder containing timeout configurations.
		 */
		protected Timeout(final Builder builder) {
			this.connectTimeout = builder.connectTimeout;
			this.connectionRequestTimeout = builder.connectionRequestTimeout;
			this.socketTimeout = builder.socketTimeout;
		}

		/**
		 * Returns a JSON representation of this {@link Timeout} object.
		 *
		 * @return a JSON string representing this object.
		 */
		@Override
		public String toString() {
			return JsonBuilder.toJson(this);
		}

		/**
		 * Returns the connection timeout in milliseconds.
		 *
		 * @return the connection timeout.
		 */
		public int getConnectTimeout() {
			return connectTimeout;
		}

		/**
		 * Sets the connection timeout in milliseconds.
		 *
		 * @param connectTimeout the connection timeout to set.
		 */
		public void setConnectTimeout(final int connectTimeout) {
			this.connectTimeout = connectTimeout;
		}

		/**
		 * Returns the connection request timeout in milliseconds.
		 *
		 * @return the connection request timeout.
		 */
		public int getConnectionRequestTimeout() {
			return connectionRequestTimeout;
		}

		/**
		 * Sets the connection request timeout in milliseconds.
		 *
		 * @param connectionRequestTimeout the connection request timeout to set.
		 */
		public void setConnectionRequestTimeout(final int connectionRequestTimeout) {
			this.connectionRequestTimeout = connectionRequestTimeout;
		}

		/**
		 * Returns the socket timeout in milliseconds.
		 *
		 * @return the socket timeout.
		 */
		public int getSocketTimeout() {
			return socketTimeout;
		}

		/**
		 * Sets the socket timeout in milliseconds.
		 *
		 * @param readTimeout the socket timeout to set.
		 */
		public void setSocketTimeout(final int readTimeout) {
			this.socketTimeout = readTimeout;
		}

		/**
		 * Builder for {@link Timeout}.
		 *
		 * @author Radu Sebastian LAZIN
		 */
		public static class Builder {

			/**
			 * Connection timeout set to {@link Timeout#DISABLED} by default.
			 */
			private int connectTimeout = DISABLED;

			/**
			 * Connection request timeout set to {@link Timeout#DISABLED} by default.
			 */
			private int connectionRequestTimeout = DISABLED;

			/**
			 * Socket timeout set to {@link Timeout#DISABLED} by default.
			 */
			private int socketTimeout = DISABLED;

			/**
			 * Default constructor.
			 */
			public Builder() {
				// empty
			}

			/**
			 * Creates a new custom builder instance.
			 *
			 * @return a new {@link Builder} instance.
			 */
			public static Builder custom() {
				return new Builder();
			}

			/**
			 * Builds a {@link Timeout} instance of the specified class.
			 *
			 * @param <T> the type of the timeout class.
			 * @param cls the class of the timeout to build.
			 * @return an instance of the specified timeout class.
			 */
			public <T extends Timeout> T build(final Class<T> cls) {
				Constructor<T> ctr = Constructors.getDeclaredConstructor(cls, Builder.class);
				return Constructors.IgnoreAccess.newInstance(ctr, this);
			}

			/**
			 * Sets the connection timeout in milliseconds.
			 *
			 * @param connectTimeout the connection timeout to set.
			 * @return this builder instance.
			 */
			public Builder connectTimeout(final int connectTimeout) {
				this.connectTimeout = connectTimeout;
				return this;
			}

			/**
			 * Sets the connection request timeout in milliseconds.
			 *
			 * @param connectionRequestTimeout the connection request timeout to set.
			 * @return this builder instance.
			 */
			public Builder connectionRequestTimeout(final int connectionRequestTimeout) {
				this.connectionRequestTimeout = connectionRequestTimeout;
				return this;
			}

			/**
			 * Sets the socket timeout in milliseconds.
			 *
			 * @param socketTimeout the socket timeout to set.
			 * @return this builder instance.
			 */
			public Builder socketTimeout(final int socketTimeout) {
				this.socketTimeout = socketTimeout;
				return this;
			}
		}
	}

	/**
	 * Compression properties.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Compression {

		/**
		 * Indicates whether GZIP compression is enabled.
		 */
		private Boolean gzip;

		/**
		 * Returns whether GZIP compression is enabled.
		 *
		 * @return true if GZIP compression is enabled, false otherwise.
		 */
		public boolean isGzip() {
			return Boolean.TRUE.equals(gzip);
		}

		/**
		 * Sets whether GZIP compression is enabled.
		 *
		 * @param gzip true to enable GZIP compression, false to disable it.
		 */
		public void setGzip(final Boolean gzip) {
			this.gzip = gzip;
		}

		/**
		 * Returns a JSON representation of this {@link Compression} object.
		 *
		 * @return a JSON string representing this object.
		 */
		@Override
		public String toString() {
			return JsonBuilder.toJson(this);
		}
	}

	/**
	 * Custom configuration for underlying clients.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Custom {

		/**
		 * The class name for the custom configuration.
		 */
		private String configClass;

		/**
		 * A map of custom configuration properties.
		 */
		private Map<String, Object> properties;

		/**
		 * Returns the class name for the custom configuration.
		 *
		 * @return the configuration class name.
		 */
		public String getConfigClass() {
			return configClass;
		}

		/**
		 * Sets the class name for the custom configuration.
		 *
		 * @param configClass the configuration class name to set.
		 */
		public void setConfigClass(final String configClass) {
			this.configClass = configClass;
		}

		/**
		 * Returns the custom configuration properties map.
		 *
		 * @return the custom properties map.
		 */
		public Map<String, Object> getProperties() {
			return properties;
		}

		/**
		 * Sets the custom configuration properties map.
		 *
		 * @param properties the custom properties map to set.
		 */
		public void setProperties(final Map<String, Object> properties) {
			this.properties = properties;
		}

		/**
		 * Retrieves the custom configuration as an instance of the specified class.
		 *
		 * @param <T> the type of the custom configuration.
		 * @param expectedConfigClass the expected configuration class.
		 * @return an instance of the specified class, or null if the configuration is invalid.
		 */
		public <T> T get(final Class<T> expectedConfigClass) {
			if (Strings.isEmpty(configClass)) {
				return null;
			}
			if (null == properties) {
				return null;
			}
			Class<T> cls = Reflection.getClass(configClass);
			if (null == cls) {
				return null;
			}
			if (!expectedConfigClass.isAssignableFrom(cls)) {
				return null;
			}
			return JsonBuilder.fromMap(properties, cls, Threads.consumeNothing());
		}
	}
}

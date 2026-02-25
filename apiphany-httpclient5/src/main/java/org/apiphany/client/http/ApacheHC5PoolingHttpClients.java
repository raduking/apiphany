package org.apiphany.client.http;

import java.util.function.Consumer;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.apiphany.client.ClientProperties;
import org.morphix.lang.function.Consumers;

/**
 * Interface containing utility methods for creating/configuring pooling HTTP clients.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ApacheHC5PoolingHttpClients {

	/**
	 * Returns a configured HTTP client based on the given client properties. The caller is responsible for closing the
	 * returned HTTP client.
	 *
	 * @param clientProperties HTTP client properties
	 * @return HTTP client
	 */
	static CloseableHttpClient createClient(final ClientProperties clientProperties) {
		return createClient(clientProperties, noCustomizer());
	}

	/**
	 * Returns a configured HTTP client based on the given client properties. The caller is responsible for closing the
	 * returned HTTP client.
	 *
	 * @param clientProperties HTTP client properties
	 * @param connectionManagerBuilderCustomizer builder customizer
	 * @return HTTP client
	 */
	static CloseableHttpClient createClient(
			final ClientProperties clientProperties,
			final Consumer<PoolingHttpClientConnectionManagerBuilder> connectionManagerBuilderCustomizer) {
		return createClient(clientProperties, connectionManagerBuilderCustomizer, noCustomizer(), noCustomizer());
	}

	/**
	 * Returns a configured HTTP client based on the given client properties. The caller is responsible for closing the
	 * returned HTTP client.
	 *
	 * @param clientProperties HTTP client properties
	 * @param connectionManagerBuilderCustomizer builder customizer
	 * @param connectionManagerCustomizer connection manager customizer
	 * @param httpClientBuilderCustomizer HTTP client builder customizer
	 * @return HTTP client
	 */
	@SuppressWarnings("resource")
	static CloseableHttpClient createClient(
			final ClientProperties clientProperties,
			final Consumer<PoolingHttpClientConnectionManagerBuilder> connectionManagerBuilderCustomizer,
			final Consumer<PoolingHttpClientConnectionManager> connectionManagerCustomizer,
			final Consumer<HttpClientBuilder> httpClientBuilderCustomizer) {
		ApacheHC5Properties apacheHC5Properties = clientProperties.getCustomProperties(ApacheHC5Properties.class);

		PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = null == apacheHC5Properties
				? createConnectionManagerBuilder(clientProperties)
				: createConnectionManagerBuilder(apacheHC5Properties);
		connectionManagerBuilderCustomizer.accept(connectionManagerBuilder);

		PoolingHttpClientConnectionManager connectionManager = connectionManagerBuilder.build();
		connectionManagerCustomizer.accept(connectionManager);

		RequestConfig requestConfig = null == apacheHC5Properties
				? createRequestConfig(clientProperties)
				: createRequestConfig(apacheHC5Properties);

		HttpClientBuilder httpClientBuilder = HttpClients.custom()
				.setConnectionManager(connectionManager)
				.setDefaultRequestConfig(requestConfig)
				.disableContentCompression();
		httpClientBuilderCustomizer.accept(httpClientBuilder);

		return httpClientBuilder.build();
	}

	/**
	 * Returns a pulling HTTP client connection manager builder configured with the given client properties.
	 *
	 * @param clientProperties HTTP client properties
	 * @return a pulling HTTP client connection manager
	 */
	static PoolingHttpClientConnectionManagerBuilder createConnectionManagerBuilder(final ClientProperties clientProperties) {
		ClientProperties.Timeout timeout = clientProperties.getTimeout();
		ClientProperties.Connection connections = clientProperties.getConnection();
		return PoolingHttpClientConnectionManagerBuilder.create()
				.setDefaultSocketConfig(SocketConfig.custom()
						.setSoTimeout(Timeout.of(timeout.getSocket()))
						.build())
				.setDefaultConnectionConfig(ConnectionConfig.custom()
						.setConnectTimeout(Timeout.of(timeout.getConnect()))
						.setSocketTimeout(Timeout.of(timeout.getSocket()))
						.build())
				.setMaxConnPerRoute(connections.getMaxPerRoute())
				.setMaxConnTotal(connections.getMaxTotal());
	}

	/**
	 * Returns a pulling HTTP client connection manager builder configured with the given Apache HTTP Client 5 properties.
	 *
	 * @param apacheHC5Properties Apache HTTP Client 5 properties
	 * @return a pulling HTTP client connection manager
	 */
	static PoolingHttpClientConnectionManagerBuilder createConnectionManagerBuilder(final ApacheHC5Properties apacheHC5Properties) {
		return PoolingHttpClientConnectionManagerBuilder.create()
				.setDefaultSocketConfig(SocketConfig.custom()
						.setSoTimeout(apacheHC5Properties.getSocket().getTimeout())
						.build())
				.setDefaultConnectionConfig(ConnectionConfig.custom()
						.setConnectTimeout(apacheHC5Properties.getConnect().getTimeout())
						.setSocketTimeout(apacheHC5Properties.getSocket().getTimeout())
						.build())
				.setMaxConnPerRoute(apacheHC5Properties.getConnection().getMaxPerRoute())
				.setMaxConnTotal(apacheHC5Properties.getConnection().getMaxTotal());
	}

	/**
	 * Creates the {@link RequestConfig} based on the client properties and the specified Apache HTTP Client 5 properties.
	 *
	 * @param clientProperties generic client properties
	 * @return returns the request configuration object
	 */
	static RequestConfig createRequestConfig(final ClientProperties clientProperties) {
		ClientProperties.Timeout timeout = clientProperties.getTimeout();
		return RequestConfig.custom()
				.setConnectionRequestTimeout(Timeout.of(timeout.getConnectionRequest()))
				.setProtocolUpgradeEnabled(false)
				.setResponseTimeout(Timeout.of(timeout.getRequest()))
				.build();
	}

	/**
	 * Creates the {@link RequestConfig} based on the client properties and the specified Apache HTTP Client 5 properties.
	 *
	 * @param apacheHC5Properties Apache HTTP Client 5 properties
	 * @return returns the request configuration object
	 */
	static RequestConfig createRequestConfig(final ApacheHC5Properties apacheHC5Properties) {
		return RequestConfig.custom()
				.setConnectionRequestTimeout(apacheHC5Properties.getConnectionRequest().getTimeout())
				.setProtocolUpgradeEnabled(apacheHC5Properties.getRequest().isProtocolUpgradeEnabled())
				.setResponseTimeout(apacheHC5Properties.getRequest().getRequestTimeout())
				.build();
	}

	/**
	 * To be used in conjunction with {@link #createClient(ClientProperties, Consumer, Consumer, Consumer)} when no
	 * customization is necessary for one of the parameters.
	 *
	 * @param <T> customizer type
	 *
	 * @return empty customizer
	 */
	static <T> Consumer<T> noCustomizer() {
		return Consumers.noConsumer();
	}
}

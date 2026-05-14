package org.apiphany.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ClientProperties.Timeout;
import org.apiphany.client.http.ApacheHC5PoolingHttpClients;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.client.http.JavaNetHttpProperties;
import org.apiphany.client.http.RestTemplateProperties;
import org.apiphany.lang.Strings;
import org.morphix.lang.collections.Lists;
import org.morphix.lang.resource.ScopedResource;
import org.springframework.boot.http.client.JdkHttpClientBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * A {@link ClientHttpRequestFactory} implementation that delegates to another instance and also allows to close some
 * resources when the factory is closed.
 *
 * @author Radu Sebastian LAZIN
 */
public class CloseableHttpRequestFactory implements ClientHttpRequestFactory, AutoCloseable {

	/**
	 * The delegate client HTTP request factory to which the {@link #createRequest(URI, HttpMethod)} method is delegated.
	 */
	private final ClientHttpRequestFactory delegate;

	/**
	 * The list of closeable resources to be closed when the factory is closed.
	 */
	private final List<ScopedResource<AutoCloseable>> closeables = new ArrayList<>();

	/**
	 * Constructor with delegate client HTTP request factory and list of closeable resources.
	 *
	 * @param delegate the delegate client HTTP request factory
	 * @param closeables the list of closeable resources to be closed when the factory is closed
	 */
	@SuppressWarnings("resource")
	protected CloseableHttpRequestFactory(final ClientHttpRequestFactory delegate, final List<AutoCloseable> closeables) {
		this.delegate = Objects.requireNonNull(delegate, "Delegate client HTTP request factory must not be null");
		for (AutoCloseable closeable : Lists.safe(closeables)) {
			if (null != closeable) {
				this.closeables.add(ScopedResource.managed(closeable));
			}
		}
	}

	/**
	 * Constructor with delegate client HTTP request factory and varargs of closeable resources.
	 *
	 * @param delegate the delegate client HTTP request factory
	 * @param closeables the varargs of closeable resources to be closed when the factory is closed
	 */
	protected CloseableHttpRequestFactory(final ClientHttpRequestFactory delegate, final AutoCloseable... closeables) {
		this(delegate, Arrays.asList(closeables));
	}

	/**
	 * Static factory method for creating a new instance of {@link CloseableHttpRequestFactory} with the given delegate
	 * client HTTP request factory and varargs of closeable resources.
	 *
	 * @param delegate the delegate client HTTP request factory
	 * @param closeables the varargs of closeable resources to be closed when the factory is closed
	 * @return a new instance of {@link CloseableHttpRequestFactory}
	 */
	public static CloseableHttpRequestFactory of(final ClientHttpRequestFactory delegate, final AutoCloseable... closeables) {
		return new CloseableHttpRequestFactory(delegate, closeables);
	}

	/**
	 * @see AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		for (AutoCloseable closeable : closeables) {
			closeable.close();
		}
	}

	/**
	 * @see ClientHttpRequestFactory#createRequest(URI, HttpMethod)
	 */
	@Override
	public ClientHttpRequest createRequest(final URI uri, final HttpMethod httpMethod) throws IOException {
		return delegate.createRequest(uri, httpMethod);
	}

	/**
	 * Detects the usable {@link CloseableHttpRequestFactory} implementation based on the presence of specific classes in
	 * the classpath and the given client properties.
	 *
	 * @param clientProperties client properties
	 * @return a {@link CloseableHttpRequestFactory} that delegates to an appropriate implementation
	 * @throws IllegalArgumentException if the client library specified in the client properties is not blank but does not
	 *     match any known libraries
	 */
	public static CloseableHttpRequestFactory detect(final ClientProperties clientProperties) {
		RestTemplateProperties restTemplateProperties = clientProperties.getCustomProperties(RestTemplateProperties.class);
		if (null != restTemplateProperties) {
			CloseableHttpRequestFactory factory = detect(clientProperties, restTemplateProperties.getClientLibrary());
			if (null != factory) {
				return factory;
			}
		}
		if (ApacheHC5Library.isPresent()) {
			return HttpComponents.create(clientProperties);
		}
		return JavaNet.create(clientProperties);
	}

	/**
	 * Detects the usable {@link CloseableHttpRequestFactory} implementation based on the given client library name and
	 * client properties.
	 *
	 * @param clientProperties client properties
	 * @param clientLibrary the client library name to use for detection (e.g., "http-client5" or "simple")
	 * @return a {@link CloseableHttpRequestFactory} that delegates to an appropriate implementation based on the given
	 * client library name, or {@code null} if the client library name is blank or does not match any known libraries
	 * @throws IllegalArgumentException if the client library name is not blank but does not match any known libraries
	 */
	public static CloseableHttpRequestFactory detect(final ClientProperties clientProperties, final String clientLibrary) {
		if (Strings.isBlank(clientLibrary)) {
			return null;
		}
		return switch (clientLibrary.toLowerCase()) {
			case ApacheHC5Library.CLIENT_NAME -> HttpComponents.create(clientProperties);
			case JavaNetHttpProperties.ROOT -> JavaNet.create(clientProperties);
			default -> throw new IllegalArgumentException("Unsupported client library: " + clientLibrary);
		};
	}

	/**
	 * Static inner class containing utility methods for creating {@link CloseableHttpRequestFactory} instances that
	 * delegate to an {@link HttpComponentsClientHttpRequestFactory}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class HttpComponents {

		/**
		 * Creates a new {@link CloseableHttpRequestFactory} that delegates to an {@link HttpComponentsClientHttpRequestFactory}
		 * based on the given client properties. The caller is responsible for closing the returned factory.
		 *
		 * @param clientProperties client properties
		 * @return a new {@link CloseableHttpRequestFactory} that delegates to an {@link HttpComponentsClientHttpRequestFactory}
		 * based on the given client properties
		 */
		@SuppressWarnings("resource")
		public static CloseableHttpRequestFactory create(final ClientProperties clientProperties) {
			CloseableHttpClient httpClient = ApacheHC5PoolingHttpClients.createClient(clientProperties,
					ApacheHC5PoolingHttpClients.noCustomizer());
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			return CloseableHttpRequestFactory.of(requestFactory, httpClient);
		}

		/**
		 * Private constructor.
		 */
		private HttpComponents() {
			// empty
		}
	}

	/**
	 * Static inner class containing utility methods for creating {@link CloseableHttpRequestFactory} instances that
	 * delegate to a {@link SimpleClientHttpRequestFactory}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class JavaNet {

		/**
		 * Creates a new {@link CloseableHttpRequestFactory} that delegates to a {@link SimpleClientHttpRequestFactory} based on
		 * the given client properties. The caller is responsible for closing the returned factory.
		 *
		 * @param clientProperties client properties
		 * @return a new {@link CloseableHttpRequestFactory} that delegates to a {@link SimpleClientHttpRequestFactory} based on
		 * the given client properties
		 */
		@SuppressWarnings("resource")
		public static CloseableHttpRequestFactory create(final ClientProperties clientProperties) { // NOSONAR
			HttpClient httpClient = new JdkHttpClientBuilder()
					.withCustomizer(httpClientBuilder -> JavaNetHttpExchangeClient.customize(httpClientBuilder, clientProperties, null))
					.build(null);
			JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
			requestFactory.setReadTimeout(JavaNetHttpExchangeClient.getUsableTimeout(clientProperties.getTimeout(), Timeout::getRequest));
			return CloseableHttpRequestFactory.of(requestFactory, httpClient);
		}

		/**
		 * Private constructor.
		 */
		private JavaNet() {
			// empty
		}
	}
}

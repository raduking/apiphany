package org.apiphany.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ClientProperties.Timeout;
import org.apiphany.client.http.RestTemplateProperties;
import org.apiphany.lang.Strings;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;
import org.morphix.lang.collections.Lists;
import org.morphix.lang.resource.ScopedResource;
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
	 * @param args optional arguments that can be used for detection (e.g., client library name)
	 * @return a {@link CloseableHttpRequestFactory} that delegates to an appropriate implementation
	 * @throws IllegalArgumentException if the client library specified in the client properties is not blank but does not
	 *     match any known libraries
	 */
	public static CloseableHttpRequestFactory detect(final ClientProperties clientProperties, final Object... args) {
		if (JavaObjects.isEmpty(args)) {
			return detect(clientProperties, Map.of());
		}
		Map<Class<?>, Object> argsMap = new HashMap<>();
		for (Object arg : args) {
			if (null != arg) {
				argsMap.put(arg.getClass(), arg);
			}
		}
		return detect(clientProperties, argsMap);
	}

	/**
	 * Detects the usable {@link CloseableHttpRequestFactory} implementation based on the presence of specific classes in
	 * the classpath and the given client properties.
	 *
	 * @param clientProperties client properties
	 * @param args optional arguments that can be used for detection (e.g., client library name)
	 * @return a {@link CloseableHttpRequestFactory} that delegates to an appropriate implementation
	 * @throws IllegalArgumentException if the client library specified in the client properties is not blank but does not
	 *     match any known libraries
	 */
	public static CloseableHttpRequestFactory detect(final ClientProperties clientProperties, final Map<Class<?>, Object> args) {
		RestTemplateProperties restTemplateProperties = clientProperties.getCustomProperties(RestTemplateProperties.class);
		if (null != restTemplateProperties) {
			CloseableHttpRequestFactory factory = detect(clientProperties, restTemplateProperties.getClientLibrary(), args);
			if (null != factory) {
				return factory;
			}
		}
		if (ApacheHC5Library.isPresent()) {
			return HttpComponents.create(clientProperties, args);
		}
		return JavaNetHttp.create(clientProperties, args);
	}

	/**
	 * Detects the usable {@link CloseableHttpRequestFactory} implementation based on the given client library name and
	 * client properties.
	 *
	 * @param clientProperties client properties
	 * @param clientLibrary the client library name to use for detection (e.g., "http-client5" or "simple")
	 * @param args optional arguments that can be used for detection (e.g., client library name)
	 * @return a {@link CloseableHttpRequestFactory} that delegates to an appropriate implementation based on the given
	 * client library name, or {@code null} if the client library name is blank or does not match any known libraries
	 * @throws IllegalArgumentException if the client library name is not blank but does not match any known libraries
	 */
	public static CloseableHttpRequestFactory detect(final ClientProperties clientProperties, final String clientLibrary,
			final Map<Class<?>, Object> args) {
		if (Strings.isBlank(clientLibrary)) {
			return null;
		}
		return switch (clientLibrary.toLowerCase()) {
			case ApacheHC5Library.CLIENT_NAME -> HttpComponents.create(clientProperties, args);
			case JavaNetHttpLibrary.CLIENT_NAME -> JavaNetHttp.create(clientProperties, args);
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
		 * @param args optional arguments that can be used for creating the HTTP client (e.g., SSL context)
		 * @return a new {@link CloseableHttpRequestFactory} that delegates to an {@link HttpComponentsClientHttpRequestFactory}
		 * based on the given client properties
		 */
		@SuppressWarnings("resource")
		public static CloseableHttpRequestFactory create(final ClientProperties clientProperties, final Map<Class<?>, Object> args) { // NOSONAR
			CloseableHttpClient httpClient = ApacheHC5Clients.createClient(clientProperties,
					connectionManagerBuilder -> {
						SSLContext sslContext = JavaObjects.cast(args.get(SSLContext.class));
						Nullables.whenNotNull(sslContext)
								.then(ssl -> ApacheHC5Clients.configureTls(connectionManagerBuilder, ssl));
					});
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
	public static class JavaNetHttp {

		/**
		 * Creates a new {@link CloseableHttpRequestFactory} that delegates to a {@link SimpleClientHttpRequestFactory} based on
		 * the given client properties. The caller is responsible for closing the returned factory.
		 *
		 * @param clientProperties client properties
		 * @param args optional arguments that can be used for creating the HTTP client (e.g., SSL context)
		 * @return a new {@link CloseableHttpRequestFactory} that delegates to a {@link SimpleClientHttpRequestFactory} based on
		 * the given client properties
		 */
		@SuppressWarnings("resource")
		public static CloseableHttpRequestFactory create(final ClientProperties clientProperties, final Map<Class<?>, Object> args) { // NOSONAR
			HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
			SSLContext sslContext = JavaObjects.cast(args.get(SSLContext.class));
			JavaNetHttpClients.customize(httpClientBuilder, clientProperties, sslContext);
			// build the HTTP client and create the request factory with the appropriate timeout settings based on the client
			// properties
			HttpClient httpClient = httpClientBuilder.build();
			JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
			requestFactory.setReadTimeout(JavaNetHttpClients.getTimeout(clientProperties.getTimeout(), Timeout::getRequest));
			return CloseableHttpRequestFactory.of(requestFactory, httpClient);
		}

		/**
		 * Private constructor.
		 */
		private JavaNetHttp() {
			// empty
		}
	}
}

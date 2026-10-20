package org.apiphany.client.http;

import org.apiphany.client.ClientProperties;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/**
 * Properties for configuring the Spring REST clients such as {@link RestClient} or {@link RestTemplate}.
 *
 * @author Radu Sebastian LAZIN
 */
public class SpringRestClientProperties {

	/**
	 * The root property prefix for RestClient configuration.
	 */
	public static final String ROOT = "rest-client";

	/**
	 * The client library to use for the RestClient exchange client.
	 * <p>
	 * This property can be used to specify the client library to use for the {@link SpringRestExchangeClient} or
	 * {@link RestTemplateExchangeClient}, such as "http-client5", "okhttp", or "simple". If not specified, the default
	 * client library will be used based on the presence of Apache HttpClient 5 in the classpath.
	 */
	private String clientLibrary;

	/**
	 * Default constructor.
	 */
	public SpringRestClientProperties() {
		// empty
	}

	/**
	 * Adds an explicit client library {@link SpringRestClientProperties#getClientLibrary()} override to the given
	 * properties.
	 *
	 * @param properties client properties to augment
	 * @param clientLibrary the name of the client library (present in {@code ROOT} static member for supported ones)
	 * @return the same properties instance
	 */
	public static ClientProperties withClientLibrary(final ClientProperties properties, final String clientLibrary) {
		properties.setCustomProperties(SpringRestClientProperties.ROOT, new SpringRestClientProperties() {
			{
				setClientLibrary(clientLibrary);
			}
		});
		return properties;
	}

	/**
	 * Returns the client library to use for the {@link SpringRestExchangeClient}.
	 *
	 * @return the client library to use for the RestClient exchange client
	 */
	public String getClientLibrary() {
		return clientLibrary;
	}

	/**
	 * Sets the client library to use for the {@link SpringRestExchangeClient}.
	 *
	 * @param clientLibrary the client library to use for the RestClient exchange client
	 */
	public void setClientLibrary(final String clientLibrary) {
		this.clientLibrary = clientLibrary;
	}
}

package org.apiphany.client.http;

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
	 * {@link RestTemplateExchangeClient}, such as "http-client5" or "simple". If not specified, the default client library
	 * will be used based on the presence of Apache HttpClient 5 in the classpath.
	 */
	private String clientLibrary;

	/**
	 * Default constructor.
	 */
	public SpringRestClientProperties() {
		// empty
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

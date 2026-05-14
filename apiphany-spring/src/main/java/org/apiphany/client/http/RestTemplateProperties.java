package org.apiphany.client.http;

/**
 * Properties for configuring the RestTemplate exchange client.
 *
 * @author Radu Sebastian LAZIN
 */
public class RestTemplateProperties {

	/**
	 * The root property prefix for RestTemplate configuration.
	 */
	public static final String ROOT = "rest-template";

	/**
	 * The client library to use for the RestTemplate exchange client.
	 * <p>
	 * This property can be used to specify the client library to use for the RestTemplate exchange client, such as
	 * "http-client5" or "simple". If not specified, the default client library will be used based on the presence of Apache
	 * HttpClient 5 in the classpath.
	 */
	private String clientLibrary;

	/**
	 * Default constructor.
	 */
	public RestTemplateProperties() {
		// empty
	}

	/**
	 * Returns the client library to use for the RestTemplate exchange client.
	 *
	 * @return the client library to use for the RestTemplate exchange client
	 */
	public String getClientLibrary() {
		return clientLibrary;
	}

	/**
	 * Sets the client library to use for the RestTemplate exchange client.
	 *
	 * @param clientLibrary the client library to use for the RestTemplate exchange client
	 */
	public void setClientLibrary(final String clientLibrary) {
		this.clientLibrary = clientLibrary;
	}
}

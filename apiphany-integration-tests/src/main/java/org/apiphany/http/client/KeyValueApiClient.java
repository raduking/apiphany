package org.apiphany.http.client;

import java.util.List;
import java.util.Map;

import org.apiphany.ApiClient;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.http.server.KeyValueHttpServer;
import org.morphix.reflection.GenericClass;

/**
 * Simple key-value API client for testing purposes that uses the {@link JavaNetHttpExchangeClient} as underlying HTTP
 * client. This client exposes basic CRUD operations for a key-value store API implemented in
 * {@link KeyValueHttpServer}.
 *
 * @author Radu Sebastian LAZIN
 */
public class KeyValueApiClient extends ApiClient {

	/**
	 * API base path.
	 */
	private static final String KEYS = "keys";

	/**
	 * Map generic type.
	 */
	private static final GenericClass<Map<String, String>> MAP_TYPE = typeObject();

	/**
	 * Constructs a key-value API client.
	 *
	 * @param baseUrl the base URL
	 * @param properties the client properties
	 */
	public KeyValueApiClient(final String baseUrl, final ClientProperties properties) {
		super(baseUrl, with(properties));
	}

	/**
	 * Retrieves the value for the given key.
	 *
	 * @param key the key
	 * @return the value
	 */
	public String get(final String key) {
		return client()
				.http()
				.get()
				.path(API, KEYS, key)
				.retrieve(String.class)
				.orNull();
	}

	/**
	 * Retrieves all key-value pairs.
	 *
	 * @return all key-value pairs
	 */
	public Map<String, String> getAll() {
		return client()
				.http()
				.get()
				.path(API, KEYS)
				.retrieve(MAP_TYPE)
				.orNull();
	}

	/**
	 * Sets the value for the given key.
	 *
	 * @param key the key
	 * @param value the value
	 * @return the value
	 */
	public String set(final String key, final String value) {
		return client()
				.http()
				.put()
				.path(API, KEYS, key)
				.body(value)
				.retrieve(String.class)
				.orNull();
	}

	/**
	 * Adds a new key-value pair.
	 *
	 * @param key the key
	 * @param value the value
	 * @return the value
	 */
	public String add(final String key, final String value) {
		return client()
				.http()
				.post()
				.path(API, KEYS)
				.body(key + ":" + value)
				.retrieve(String.class)
				.orNull();
	}

	/**
	 * Deletes the value for the given key.
	 *
	 * @param key the key
	 * @return the value
	 */
	public String delete(final String key) {
		return client()
				.http()
				.delete()
				.path(API, KEYS, key)
				.retrieve(String.class)
				.orNull();
	}

	/**
	 * Appends the given value to the existing value for the given key.
	 *
	 * @param key the key
	 * @param appended the value to append
	 * @return the new value
	 */
	public String append(final String key, final String appended) {
		return client()
				.http()
				.patch()
				.path(API, KEYS, key)
				.body(appended)
				.retrieve(String.class)
				.orNull();
	}

	/**
	 * Performs a HEAD request for the given key.
	 *
	 * @param key the key
	 * @return the response headers
	 */
	public Map<String, List<String>> head(final String key) {
		return client()
				.http()
				.head()
				.path(API, KEYS, key)
				.retrieve()
				.getHeaders();
	}

	/**
	 * Performs an OPTIONS request.
	 *
	 * @return the response headers
	 */
	public Map<String, List<String>> options() {
		return client()
				.http()
				.options()
				.path(API, KEYS)
				.retrieve()
				.getHeaders();
	}

	/**
	 * Performs a TRACE request.
	 *
	 * @return the API response
	 */
	public ApiResponse<String> trace() {
		return client()
				.http()
				.trace()
				.path(API, KEYS)
				.retrieve(String.class);
	}
}

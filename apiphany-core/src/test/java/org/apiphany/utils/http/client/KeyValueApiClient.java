package org.apiphany.utils.http.client;

import java.util.List;
import java.util.Map;

import org.apiphany.ApiClient;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.utils.http.server.KeyValueHttpServer;
import org.morphix.reflection.GenericClass;

/**
 * Simple key-value API client for testing purposes that uses the {@link JavaNetHttpExchangeClient} as underlying HTTP
 * client. This client exposes basic CRUD operations for a key-value store API implemented in
 * {@link KeyValueHttpServer}.
 *
 * @author Radu Sebastian LAZIN
 */
public class KeyValueApiClient extends ApiClient {

	private static final String KEYS = "keys";

	private static final GenericClass<Map<String, String>> MAP_TYPE = typeObject();

	public KeyValueApiClient(final String baseUrl, final ClientProperties properties) {
		super(baseUrl, with(properties));
	}

	public String get(final String key) {
		return client()
				.http()
				.get()
				.path(API, KEYS, key)
				.retrieve(String.class)
				.orNull();
	}

	public Map<String, String> getAll() {
		return client()
				.http()
				.get()
				.path(API, KEYS)
				.retrieve(MAP_TYPE)
				.orNull();
	}

	public String set(final String key, final String value) {
		return client()
				.http()
				.put()
				.path(API, KEYS, key)
				.body(value)
				.retrieve(String.class)
				.orNull();
	}

	public String add(final String key, final String value) {
		return client()
				.http()
				.post()
				.path(API, KEYS)
				.body(key + ":" + value)
				.retrieve(String.class)
				.orNull();
	}

	public String delete(final String key) {
		return client()
				.http()
				.delete()
				.path(API, KEYS, key)
				.retrieve(String.class)
				.orNull();
	}

	public String append(final String key, final String appended) {
		return client()
				.http()
				.patch()
				.path(API, KEYS, key)
				.body(appended)
				.retrieve(String.class)
				.orNull();
	}

	public Map<String, List<String>> head(final String key) {
		return client()
				.http()
				.head()
				.path(API, KEYS, key)
				.retrieve()
				.getHeaders();
	}

	public Map<String, List<String>> options() {
		return client()
				.http()
				.options()
				.path(API, KEYS)
				.retrieve()
				.getHeaders();
	}

	public ApiResponse<String> trace() {
		return client()
				.http()
				.trace()
				.path(API, KEYS)
				.retrieve(String.class);
	}
}

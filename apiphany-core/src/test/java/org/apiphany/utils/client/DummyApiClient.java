package org.apiphany.utils.client;

import java.util.List;
import java.util.Map;

import org.apiphany.ApiClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.utils.TestDto;
import org.morphix.reflection.GenericClass;

public class DummyApiClient extends ApiClient {

	public static final GenericClass<List<String>> LIST_TYPE_1 = ApiClient.typeObject();

	public static final GenericClass<List<String>> LIST_TYPE_2 = new GenericClass<>() {
		// empty
	};

	public static final GenericClass<List<Map<String, Object>>> LIST_TYPE_3 = ApiClient.typeObject();

	public DummyApiClient(final String baseUrl, final ExchangeClient apiAuthClient) {
		super(baseUrl, apiAuthClient);
	}

	public TestDto getTest(final String... paths) {
		return client()
				.http()
				.get()
				.path(paths)
				.retrieve(TestDto.class)
				.orDefault(TestDto.EMPTY);
	}

}

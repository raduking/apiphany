package org.apiphany.client.http;

import org.apiphany.ApiRequest;
import org.apiphany.client.ClientProperties;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.ResponseEntityExtractor;
import org.apiphany.http.SpringHttpSupport;
import org.apiphany.lang.Strings;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.RequestHeadersSpec;

/**
 * Exchange client implemented with {@link RestClient}.
 *
 * @author Radu Sebastian LAZIN
 */
public class SpringRestExchangeClient extends AbstractSpringExchangeClient {

	/**
	 * The underlying REST client.
	 */
	private final RestClient restClient;

	/**
	 * Constructor with client properties and REST client builder used to build the underlying {@link RestClient}.
	 *
	 * @param clientProperties client properties
	 * @param restClientBuilder REST client builder
	 */
	public SpringRestExchangeClient(final ClientProperties clientProperties, final RestClient.Builder restClientBuilder) {
		super(clientProperties);
		this.restClient = customize(restClientBuilder, clientProperties).build();
	}

	/**
	 * Constructor with client properties and custom REST client. The custom REST client will be mutated to apply the client
	 * properties and message converters, and then built to create the underlying REST client.
	 *
	 * @param clientProperties client properties
	 * @param customRestClient custom REST client to use as a base for the underlying REST client
	 */
	public SpringRestExchangeClient(final ClientProperties clientProperties, final RestClient customRestClient) {
		this(clientProperties, customRestClient.mutate());
	}

	/**
	 * Constructor with client properties.
	 *
	 * @param clientProperties client properties
	 */
	public SpringRestExchangeClient(final ClientProperties clientProperties) {
		this(clientProperties, RestClient.builder());
	}

	/**
	 * Default constructor.
	 */
	public SpringRestExchangeClient() {
		this(ClientProperties.defaults());
	}

	/**
	 * Customizes the REST client builder by applying the client properties and message converters. This method sets the
	 * base URL of the REST client if it is specified in the client properties, and configures the request factory and
	 * message converters to ensure that the underlying REST client behaves according to apiphany's expectations and allows
	 * for better control over the HTTP interactions.
	 *
	 * @param restClientBuilder the REST client builder to customize
	 * @param clientProperties the client properties to apply to the REST client builder
	 * @return the customized REST client builder
	 */
	@SuppressWarnings("resource")
	private RestClient.Builder customize(final RestClient.Builder restClientBuilder, final ClientProperties clientProperties) {
		if (Strings.isNotEmpty(clientProperties.getBaseUrl())) {
			restClientBuilder.baseUrl(clientProperties.getBaseUrl());
		}
		restClientBuilder.requestFactory(getRequestFactory());
		restClientBuilder.messageConverters(getMessageConverters());
		return restClientBuilder;
	}

	/**
	 * @see AbstractSpringExchangeClient#sendRequest(ApiRequest, HttpEntity)
	 */
	@Override
	protected <T, U> ResponseEntity<U> sendRequest(final ApiRequest<T> apiRequest, final HttpEntity<T> httpEntity) {
		HttpMethod httpMethod = apiRequest.getMethod();
		var springHttpMethod = SpringHttpSupport.getHttpMethod(httpMethod.value());

		RequestBodySpec requestSpec = restClient.method(springHttpMethod)
				.uri(apiRequest.getUri())
				.headers(headers -> headers.addAll(httpEntity.getHeaders()));
		RequestHeadersSpec<?> headerSpec;
		if (null != httpEntity.getBody()) {
			headerSpec = requestSpec.body(httpEntity.getBody()); // NOSONAR
		} else {
			headerSpec = requestSpec;
		}
		Class<U> responseType = getResponseType(apiRequest);
		return headerSpec.exchange((request, response) -> {
			ResponseEntityExtractor<U> responseExtractor = new ResponseEntityExtractor<>(responseType, getMessageConverters());
			return responseExtractor.extractData(response);
		});
	}
}

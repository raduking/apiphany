package org.apiphany.client.http;

import org.apiphany.ApiRequest;
import org.apiphany.client.ClientProperties;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.ResponseEntityExtractor;
import org.apiphany.http.SpringHttpSupport;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;

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
		super(clientProperties, SpringHttpSupport.getRequestFactory(restClientBuilder));
		this.restClient = customize(restClientBuilder).build();
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
	 * Customizes the REST client builder by applying message converters and, when the builder has no request factory, the
	 * factory detected from client properties. An already configured request factory is left unchanged so caller-provided
	 * transport settings are preserved.
	 *
	 * @param restClientBuilder the REST client builder to customize
	 * @return the customized REST client builder
	 */
	@SuppressWarnings("resource")
	private RestClient.Builder customize(final RestClient.Builder restClientBuilder) {
		if (null == SpringHttpSupport.getRequestFactory(restClientBuilder)) {
			restClientBuilder.requestFactory(getRequestFactory());
		}
		return restClientBuilder.messageConverters(getMessageConverters());
	}

	/**
	 * @see AbstractSpringExchangeClient#sendRequest(ApiRequest, HttpEntity)
	 */
	@Override
	protected <T, U> ResponseEntity<U> sendRequest(final ApiRequest<T> apiRequest, final HttpEntity<T> httpEntity) {
		HttpMethod httpMethod = apiRequest.getMethod();
		var springHttpMethod = SpringHttpSupport.getHttpMethod(httpMethod.value());
		T body = httpEntity.getBody();

		RequestBodySpec requestSpec = restClient
				.method(springHttpMethod)
				.uri(apiRequest.getUri())
				.headers(headers -> headers.addAll(httpEntity.getHeaders()));
		if (null != body) {
			requestSpec = requestSpec.body(body);
		}
		return requestSpec.exchange((request, response) -> {
			Class<U> responseType = getResponseType(apiRequest);
			ResponseEntityExtractor<U> responseExtractor = new ResponseEntityExtractor<>(responseType, getMessageConverters(),
					getMaxResponseBodySize());
			return responseExtractor.extractData(response);
		});
	}
}

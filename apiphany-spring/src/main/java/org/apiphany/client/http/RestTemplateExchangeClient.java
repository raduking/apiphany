package org.apiphany.client.http;

import java.net.URI;

import org.apiphany.ApiRequest;
import org.apiphany.client.ClientProperties;
import org.apiphany.http.HttpMethod;
import org.apiphany.spring.http.SpringHttpRequests;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Exchange client implemented with {@link RestTemplate}.
 *
 * @author Radu Sebastian LAZIN
 */
public class RestTemplateExchangeClient extends AbstractSpringExchangeClient {

	/**
	 * The underlying REST template.
	 */
	private RestTemplate restTemplate;

	/**
	 * Constructor with client properties and REST template builder used to build the underlying {@link RestTemplate}.
	 *
	 * @param clientProperties client properties
	 * @param restTemplateBuilder REST template builder
	 */
	public RestTemplateExchangeClient(final ClientProperties clientProperties, final RestTemplateBuilder restTemplateBuilder) {
		super(clientProperties);
		this.restTemplate = restTemplateBuilder.build();
	}

	/**
	 * Constructor with client properties.
	 *
	 * @param clientProperties client properties
	 */
	public RestTemplateExchangeClient(final ClientProperties clientProperties) {
		this(clientProperties, new RestTemplateBuilder());
	}

	/**
	 * Default constructor.
	 */
	public RestTemplateExchangeClient() {
		this(ClientProperties.defaults());
	}

	/**
	 * @see #close()
	 */
	@Override
	public void close() throws Exception {
		// empty
	}

	/**
	 * Sends the HTTP request using the underlying {@link RestTemplate} and returns the response entity. This method can be
	 * overridden by subclasses to provide custom logic for sending the request.
	 *
	 * @param <T> request entity type
	 * @param <U> response entity type
	 *
	 * @param apiRequest the request object
	 * @param httpEntity the HTTP request entity
	 * @return the response entity
	 */
	@Override
	protected <T, U> ResponseEntity<U> sendRequest(final ApiRequest<T> apiRequest, final HttpEntity<T> httpEntity) {
		URI uri = SpringHttpRequests.getUriComponentsBuilder(apiRequest.getUrl(), apiRequest.getParams()).build().toUri();
		HttpMethod httpMethod = apiRequest.getMethod();
		var springHttpMethod = SpringHttpRequests.getHttpMethod(httpMethod.value());

		return restTemplate.exchange(uri, springHttpMethod, httpEntity, getResponseType(apiRequest));
	}
}

package org.apiphany.client.http;

import org.apiphany.ApiRequest;
import org.apiphany.client.ClientProperties;
import org.apiphany.http.HttpEntityRequestCallback;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.ResponseEntityExtractor;
import org.apiphany.http.SpringHttpSupport;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
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
	private final RestTemplate restTemplate;

	/**
	 * Constructor with client properties and REST template builder used to build the underlying {@link RestTemplate}.
	 *
	 * @param clientProperties client properties
	 * @param restTemplateBuilder REST template builder
	 */
	public RestTemplateExchangeClient(final ClientProperties clientProperties, final RestTemplateBuilder restTemplateBuilder) {
		super(clientProperties);
		this.restTemplate = customize(restTemplateBuilder).build();
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
	 * Customizes the REST template builder so that the underlying {@link RestTemplate} does not add any extra logic to the
	 * HTTP request, such as following redirects. This ensures that the behavior of the client is consistent with apiphany's
	 * expectations and allows for better control over the HTTP interactions.
	 *
	 * @param restTemplateBuilder the REST template builder to customize
	 * @return the customized REST template builder
	 */
	private RestTemplateBuilder customize(final RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder
				.requestFactory(this::getRequestFactory)
				.messageConverters(getMessageConverters());
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
		HttpMethod httpMethod = apiRequest.getMethod();
		var springHttpMethod = SpringHttpSupport.getHttpMethod(httpMethod.value());
		Class<U> responseType = getResponseType(apiRequest);

		RequestCallback requestCallback = httpEntityCallback(httpEntity);
		ResponseExtractor<ResponseEntity<U>> responseExtractor = responseEntityExtractor(responseType);
		return restTemplate.execute(apiRequest.getUri(), springHttpMethod, requestCallback, responseExtractor);
	}

	/**
	 * Creates a request callback that writes the HTTP request entity to the HTTP request.
	 *
	 * @param <T> request entity type
	 *
	 * @param requestEntity the HTTP request entity to write to the HTTP request
	 * @return the request callback
	 */
	protected <T> RequestCallback httpEntityCallback(final HttpEntity<T> requestEntity) {
		return new HttpEntityRequestCallback<>(requestEntity, restTemplate.getMessageConverters());
	}

	/**
	 * Creates a response extractor that extracts the response entity from the HTTP response.
	 *
	 * @param <T> response entity type
	 *
	 * @param responseType the class of the response entity
	 * @return the response extractor
	 */
	protected <T> ResponseExtractor<ResponseEntity<T>> responseEntityExtractor(final Class<T> responseType) {
		return new ResponseEntityExtractor<>(responseType, restTemplate.getMessageConverters());
	}
}

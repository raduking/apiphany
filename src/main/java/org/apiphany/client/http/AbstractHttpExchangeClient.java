package org.apiphany.client.http;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apiphany.ApiRequest;
import org.apiphany.RequestMethod;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ContentConverter;
import org.apiphany.client.ExchangeClient;
import org.apiphany.header.HeaderValuesChain;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.json.JsonBuilder;
import org.apiphany.json.jackson.JacksonJsonHttpContentConverter;
import org.morphix.lang.JavaObjects;

/**
 * Abstract HTTP exchange client which holds all the common information needed to build a HTTP exchange client.
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class AbstractHttpExchangeClient implements ExchangeClient {

	/**
	 * Client properties.
	 */
	private final ClientProperties clientProperties;

	/**
	 * Content converters.
	 */
	private final List<ContentConverter<?>> contentConverters = new LinkedList<>();

	/**
	 * Header values chain.
	 */
	private final HeaderValuesChain headerValuesChain = new HeaderValuesChain();

	/**
	 * Initialize the client with the given client properties.
	 *
	 * @param clientProperties client properties
	 */
	protected AbstractHttpExchangeClient(final ClientProperties clientProperties) {
		this.clientProperties = clientProperties;
		addDefaultContentConverters(contentConverters);
	}

	/**
	 * Adds the default content converters to a given content converters list.
	 *
	 * @param contentConverters list of content converters to
	 */
	public static void addDefaultContentConverters(final List<ContentConverter<?>> contentConverters) {
		contentConverters.add(new StringHttpContentConverter());

		if (JsonBuilder.isJacksonPresent()) {
			contentConverters.add(new JacksonJsonHttpContentConverter<>());
		}
	}

	/**
	 * Converts the response body to the desired type based on the request configuration and available content converters.
	 * This method:
	 * <ul>
	 * <li>Iterates through registered content converters to find a compatible one</li>
	 * <li>Falls back to string conversion if the target type is String</li>
	 * <li>Throws an exception if no suitable converter is found</li>
	 * </ul>
	 *
	 * @param <T> the type of the original request body
	 * @param <U> the target type for the response body
	 * @param <H> the type of response headers
	 *
	 * @param apiRequest the API request containing response type information
	 * @param headers the response headers used for content type negotiation
	 * @param body the raw response body to be converted
	 * @return the converted body of type {@code U}
	 * @throws UnsupportedOperationException if no compatible content converter is found
	 */
	protected <T, U, H> U convertBody(final ApiRequest<T> apiRequest, final H headers, final Object body) {
		Supplier<StringHttpContentConverter> stringConverterSupplier = StringHttpContentConverter::new;
		for (ContentConverter<?> contentConverter : getContentConverters()) {
			if (contentConverter.isConvertible(apiRequest, headers, getHeaderValuesChain())) {
				ContentConverter<U> typeConverter = JavaObjects.cast(contentConverter);
				return ContentConverter.convertBody(typeConverter, apiRequest, body);
			}
			if (contentConverter instanceof StringHttpContentConverter stringConverter) {
				stringConverterSupplier = () -> stringConverter;
			}
		}
		if (String.class.equals(apiRequest.getClassResponseType())) {
			return JavaObjects.cast(stringConverterSupplier.get().from(body, String.class));
		}
		throw new UnsupportedOperationException("No content converter found to convert response to: " + apiRequest.getResponseType().getTypeName()
				+ ", for the response content type: " + getHeaderValuesChain().get(HttpHeader.CONTENT_TYPE.value(), headers));
	}

	/**
	 * Returns the client properties for this client.
	 *
	 * @return the client properties for this client
	 */
	public ClientProperties getClientProperties() {
		return clientProperties;
	}

	/**
	 * Returns the content converters.
	 *
	 * @return the content converters
	 */
	public List<ContentConverter<?>> getContentConverters() { // NOSONAR the converters can have any generic type
		return contentConverters;
	}

	/**
	 * Returns the header values chain.
	 *
	 * @return the header values chain
	 */
	public HeaderValuesChain getHeaderValuesChain() {
		return headerValuesChain;
	}

	/**
	 * Redact the {@link HttpHeader#AUTHORIZATION} header.
	 *
	 * @return redacted headers predicate
	 */
	@Override
	public Predicate<String> getRedactedHeaderPredicate() {
		return HttpHeader.AUTHORIZATION::matches;
	}

	/**
	 * Returns the GET request method.
	 *
	 * @return the GET request method
	 */
	@Override
	public RequestMethod get() {
		return HttpMethod.GET;
	}

	/**
	 * Returns the PUT request method.
	 *
	 * @return the PUT request method
	 */
	@Override
	public RequestMethod put() {
		return HttpMethod.PUT;
	}

	/**
	 * Returns the POST request method.
	 *
	 * @return the POST request method
	 */
	@Override
	public RequestMethod post() {
		return HttpMethod.POST;
	}

	/**
	 * Returns the DELETE request method.
	 *
	 * @return the DELETE request method
	 */
	@Override
	public RequestMethod delete() {
		return HttpMethod.DELETE;
	}

	/**
	 * Returns the PATCH request method.
	 *
	 * @return the PATCH request method
	 */
	@Override
	public RequestMethod patch() {
		return HttpMethod.PATCH;
	}

	/**
	 * Returns the HEAD request method.
	 *
	 * @return the HEAD request method
	 */
	@Override
	public RequestMethod head() {
		return HttpMethod.HEAD;
	}

	/**
	 * Returns the TRACE request method.
	 *
	 * @return the TRACE request method
	 */
	@Override
	public RequestMethod trace() {
		return HttpMethod.TRACE;
	}

}

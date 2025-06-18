package org.apiphany.client.http;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apiphany.ApiRequest;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ContentConverter;
import org.apiphany.header.HeaderValuesChain;
import org.apiphany.header.Headers;
import org.apiphany.header.MapHeaderValues;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpHeaderValues;
import org.apiphany.http.TracingHeader;
import org.apiphany.json.JsonBuilder;
import org.apiphany.json.jackson.JacksonJsonHttpContentConverter;
import org.apiphany.lang.Strings;
import org.morphix.lang.JavaObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Abstract HTTP exchange client which holds all the common information needed to build an HTTP exchange client.
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class AbstractHttpExchangeClient implements HttpExchangeClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHttpExchangeClient.class);

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
		LOGGER.debug("Initializing: {}", getClass().getSimpleName());
		this.clientProperties = clientProperties;
		addDefaultContentConverters(contentConverters);
		addDefaultHeaderValues(headerValuesChain);
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
	 * Adds default value handlers to the header values chain.
	 *
	 * @param headerValuesChain the header values chain object
	 */
	public static void addDefaultHeaderValues(final HeaderValuesChain headerValuesChain) {
		headerValuesChain.add(new HttpHeaderValues());
		headerValuesChain.add(new MapHeaderValues());
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
		if (String.class.equals(apiRequest.getClassResponseType())) {
			return JavaObjects.cast(StringHttpContentConverter.instance().from(body, String.class));
		}
		for (ContentConverter<?> contentConverter : getContentConverters()) {
			if (contentConverter.isConvertible(apiRequest, headers, getHeaderValuesChain())) {
				ContentConverter<U> typeConverter = JavaObjects.cast(contentConverter);
				return ContentConverter.convertBody(typeConverter, apiRequest, body);
			}
		}
		throw new UnsupportedOperationException("No content converter found to convert response to: " + apiRequest.getResponseType().getTypeName()
				+ ", for the response content type: " + getHeaderValuesChain().get(HttpHeader.CONTENT_TYPE, headers));
	}

	/**
	 * Adds tracing headers to the given headers.
	 * <p>
	 * TODO: make it more generic
	 *
	 * @param headers headers to add tracing headers to
	 */
	public static void addTracingHeaders(final Map<String, List<String>> headers) {
		String traceId = MDC.get("traceId");
        if (Strings.isNotEmpty(traceId)) {
        	String spanId = MDC.get("spanId");
        	Headers.addTo(headers, TracingHeader.B3_TRACE_ID, traceId);
        	Headers.addTo(headers, TracingHeader.B3_SPAN_ID, spanId);
        }
	}

	/**
	 * Returns the client properties for this client.
	 *
	 * @return the client properties for this client
	 */
	@Override
	public <T extends ClientProperties> T getClientProperties() {
		return JavaObjects.cast(clientProperties);
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

}

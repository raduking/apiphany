package org.apiphany.client.http;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.SSLContext;

import org.apiphany.ApiMimeType;
import org.apiphany.ApiRequest;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ContentConverter;
import org.apiphany.header.HeaderValues;
import org.apiphany.header.MapHeaderValues;
import org.apiphany.http.HttpHeaderValues;
import org.apiphany.json.JsonBuilder;
import org.apiphany.json.jackson2.Jackson2JsonHttpContentConverter;
import org.apiphany.security.ssl.SSLContexts;
import org.apiphany.security.ssl.SSLProperties;
import org.morphix.lang.JavaObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract HTTP exchange client which holds all the common information needed to build an HTTP exchange client.
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class AbstractHttpExchangeClient implements HttpExchangeClient {

	/**
	 * Class logger.
	 */
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
	private final HeaderValues headerValuesChain;

	/**
	 * The SSL context for HTTPS if configured in client properties via {@link SSLProperties}.
	 */
	private SSLContext sslContext;

	/**
	 * Initialize the client with the given client properties.
	 *
	 * @param clientProperties client properties
	 */
	protected AbstractHttpExchangeClient(final ClientProperties clientProperties) {
		LOGGER.debug("Initializing: {}", getClass().getSimpleName());
		this.clientProperties = Objects.requireNonNull(clientProperties, "clientProperties cannot be null");
		initialize();
		addDefaultContentConverters(contentConverters);
		this.headerValuesChain = addDefaultHeaderValues(new HeaderValues());
	}

	/**
	 * Initializes the properties.
	 */
	private void initialize() {
		SSLProperties sslProperties = getCustomProperties(SSLProperties.class);
		if (null == sslProperties) {
			return;
		}
		this.sslContext = SSLContexts.create(sslProperties);
	}

	/**
	 * Adds the default content converters to a given content converters list.
	 *
	 * @param contentConverters list of content converters to
	 */
	public static void addDefaultContentConverters(final List<ContentConverter<?>> contentConverters) {
		contentConverters.add(new StringHttpContentConverter());

		if (JsonBuilder.isJacksonPresent()) {
			contentConverters.add(new Jackson2JsonHttpContentConverter<>());
		}
	}

	/**
	 * Adds default value handlers to the header values chain.
	 *
	 * @param headerValues the header values chain object
	 * @return the header values chain with default handlers added
	 */
	public static HeaderValues addDefaultHeaderValues(final HeaderValues headerValues) {
		return headerValues
				.addFirst(new MapHeaderValues())
				.addFirst(new HttpHeaderValues());
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
	 * @param mimeType the response content type
	 * @param headers the response headers used for content type negotiation
	 * @param body the raw response body to be converted
	 * @return the converted body of type {@code U}
	 * @throws UnsupportedOperationException if no compatible content converter is found
	 */
	protected <T, U, H> U convertBody(final ApiRequest<T> apiRequest, final ApiMimeType mimeType, final H headers, final Object body) {
		if (body instanceof byte[] bytes && bytes.length == 0) {
			return null;
		}
		if (String.class.equals(apiRequest.getClassResponseType())) {
			return JavaObjects.cast(StringHttpContentConverter.instance().from(body, mimeType, String.class));
		}
		for (ContentConverter<?> contentConverter : getContentConverters()) {
			if (contentConverter.isConvertible(apiRequest, mimeType, headers, getHeaderValuesChain())) {
				ContentConverter<U> typeConverter = JavaObjects.cast(contentConverter);
				return ContentConverter.convertBody(typeConverter, apiRequest, mimeType, body);
			}
		}
		throw new UnsupportedOperationException("No content converter found to convert response to: " + apiRequest.getResponseTypeName()
				+ ", for the response content type: " + mimeType);
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
	 * Returns the header values extractor chain.
	 *
	 * @return the header values extractor chain
	 */
	public HeaderValues getHeaderValuesChain() {
		return headerValuesChain;
	}

	/**
	 * Returns the SSL context.
	 *
	 * @return the SSL context
	 */
	public SSLContext getSslContext() {
		return sslContext;
	}

}

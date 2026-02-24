package org.apiphany;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apiphany.header.Headers;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.annotation.FieldName;
import org.apiphany.lang.annotation.Ignored;
import org.apiphany.lang.collections.Maps;

/**
 * Represents a message in an API context, containing a body and a set of headers. This class is generic, allowing the
 * body to be of any type.
 *
 * @param <T> the type of the message body.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiMessage<T> {

	/**
	 * The body of the message, which can be any object.
	 */
	protected T body;

	/**
	 * A map of headers to be included in the message. Each header can have multiple values.
	 */
	protected final Map<String, List<String>> headers;

	/**
	 * Constructs an API message object with the given body and headers. The headers must not be {@code null}.
	 *
	 * @param body message body
	 * @param headers message headers
	 */
	protected ApiMessage(final T body, final Map<String, List<String>> headers) {
		this.body = body;
		this.headers = Objects.requireNonNull(headers, "headers cannot be null");
	}

	/**
	 * Constructs an API message without a body and empty headers.
	 */
	protected ApiMessage() {
		this(null, new HashMap<>());
	}

	/**
	 * Returns a JSON representation of this {@link ApiMessage}.
	 *
	 * @return a JSON string representing this object
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the body of the message.
	 *
	 * @return the body of the message.
	 */
	public T getBody() {
		return body;
	}

	/**
	 * Returns the headers for the message.
	 *
	 * @return a map of headers, where each key is a header name and the value is a list of header values.
	 */
	@Ignored
	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	/**
	 * Returns all headers for display purposes. This method can be overridden by subclasses to customize the headers
	 * displayed.
	 *
	 * @return all headers for display
	 */
	@FieldName("headers")
	public Map<String, List<String>> getDisplayHeaders() {
		return Maps.safe(getHeaders());
	}

	/**
	 * Returns true if the message has a body, false otherwise.
	 *
	 * @return true if the message has a body, false otherwise
	 */
	public boolean hasBody() {
		return null != getBody();
	}

	/**
	 * Returns true if the message doesn't have a body, false otherwise.
	 *
	 * @return true if the message doesn't have a body, false otherwise
	 */
	public boolean hasNoBody() {
		return !hasBody();
	}

	/**
	 * Returns true if the headers contain the given header with the given value, false otherwise.
	 *
	 * @param <N> header name type
	 * @param <V> header value type
	 *
	 * @param headerName header name
	 * @param headerValue header value
	 * @return true if the headers contain the given header with the given value, false otherwise
	 * @see Headers#contains(Object, Object, Map)
	 */
	public <N, V> boolean containsHeader(final N headerName, final V headerValue) {
		return Headers.contains(headerName, headerValue, getHeaders());
	}

	/**
	 * Returns true if the headers contain the given header, false otherwise.
	 *
	 * @param <N> header name type
	 *
	 * @param headerName header name
	 * @return true if the headers contain the given header, false otherwise
	 * @see Headers#contains(Object, Map)
	 */
	public <N> boolean containsHeader(final N headerName) {
		return Headers.contains(headerName, getHeaders());
	}

	/**
	 * Adds the given headers to the message's headers.
	 *
	 * @param <N> header name type
	 * @param <V> header value type
	 *
	 * @param headersToAdd the headers to add
	 * @see Headers#addTo(Map, Map)
	 */
	public <N, V> void addHeaders(final Map<N, V> headersToAdd) {
		Headers.addTo(getHeaders(), headersToAdd);
	}

	/**
	 * Adds the given header to the message's headers.
	 *
	 * @param <N> header name type
	 * @param <V> header value type
	 *
	 * @param headerName the name of the header to add
	 * @param headerValue the value of the header to add
	 * @see Headers#addTo(Map, Object, Object)
	 */
	public <N, V> void addHeader(final N headerName, final V headerValue) {
		Headers.addTo(getHeaders(), headerName, headerValue);
	}
}

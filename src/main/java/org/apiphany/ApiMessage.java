package org.apiphany;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apiphany.json.JsonBuilder;
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
	protected Map<String, List<String>> headers;

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
	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	/**
	 * Returns all headers as {@link String}. This method helps log the headers of a message, and subsequent implementations
	 * can override this method to show only the wanted headers.
	 *
	 * @return all headers as string
	 */
	public String getHeadersAsString() {
		return Maps.safe(getHeaders()).toString();
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

}

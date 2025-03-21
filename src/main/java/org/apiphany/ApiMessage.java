package org.apiphany;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a message in an API context, containing a body and a set of headers.
 * This class is generic, allowing the body to be of any type.
 *
 * @param <T> the type of the message body.
 */
public class ApiMessage<T> {

    /**
     * The body of the message, which can be any object.
     */
    protected T body;

    /**
     * A map of headers to be included in the message. Each header can have multiple values.
     */
    protected Map<String, List<String>> headers = new HashMap<>();

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

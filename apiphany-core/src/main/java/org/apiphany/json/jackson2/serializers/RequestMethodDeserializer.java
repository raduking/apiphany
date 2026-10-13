package org.apiphany.json.jackson2.serializers;

import java.io.IOException;
import java.util.Objects;

import org.apiphany.ApiMethod;
import org.apiphany.RequestMethod;
import org.apiphany.http.HttpMethod;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Custom JSON deserializer for converting JSON values into {@link RequestMethod} instances.
 * <p>
 * This deserializer attempts to first parse the input as an HTTP method using {@link HttpMethod#fromString(String)}. If
 * that fails, it falls back to creating an API-specific method using {@link ApiMethod#of(String)}.
 * <p>
 * The deserialization handles both standard HTTP methods (GET, POST, etc.) and custom API methods.
 *
 * @author Radu Sebastian LAZIN
 */
public class RequestMethodDeserializer extends JsonDeserializer<RequestMethod> {

	/**
	 * Default constructor.
	 */
	public RequestMethodDeserializer() {
		// empty
	}

	/**
	 * Deserializes JSON content into a {@link RequestMethod} instance.
	 * <p>
	 * The method processes the JSON input as follows:
	 * <ol>
	 * <li>Reads the JSON value as text (a JSON {@code null} becomes an empty string)</li>
	 * <li>Attempts to parse it as a standard HTTP method</li>
	 * <li>If that fails, creates an API-specific method instance</li>
	 * </ol>
	 *
	 * @param p the JSON parser containing the input to deserialize
	 * @param ct the deserialization context
	 * @return the deserialized {@link RequestMethod} instance
	 * @throws IOException if there is an error reading the JSON content
	 */
	@Override
	public RequestMethod deserialize(final JsonParser p, final DeserializationContext ct) throws IOException {
		String nodeText = Objects.toString(p.getValueAsString(), "");
		try {
			return HttpMethod.fromString(nodeText);
		} catch (IllegalArgumentException e) {
			return ApiMethod.of(nodeText);
		}
	}
}

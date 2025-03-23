package org.apiphany;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apiphany.auth.AuthenticationType;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.retry.Retry;
import org.apiphany.meters.BasicMeters;
import org.morphix.lang.JavaObjects;
import org.morphix.reflection.GenericClass;

/**
 * Represents properties for an API Client request. It includes details such as method, URL, headers, request body,
 * response type, and additional configurations like retry logic and metrics tracking. This class also supports URL
 * encoding, streaming, and custom character sets.
 *
 * @param <T> the request body type
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiRequest<T> extends ApiMessage<T> {

	/**
	 * The method to be used for the request (e.g., GET, POST, PUT, etc.).
	 */
	protected RequestMethod method;

	/**
	 * The URL to which the request will be sent.
	 */
	protected String url;

	/**
	 * Indicates whether the request parameters should be URL-encoded.
	 */
	protected boolean urlEncoded;

	/**
	 * The expected response type as a class.
	 */
	protected Class<?> classResponseType;

	/**
	 * The expected response type as a generic class.
	 */
	protected GenericClass<?> genericResponseType;

	/**
	 * A map of query parameters to be included in the request.
	 */
	protected Map<String, String> params;

	/**
	 * The character set to be used for the request. Defaults to UTF-8.
	 */
	protected Charset charset = StandardCharsets.UTF_8;

	/**
	 * Indicates whether the response should be handled as a stream.
	 */
	protected boolean stream;

	/**
	 * Configuration for retry logic in case of request failures.
	 */
	protected Retry retry;

	/**
	 * Metrics tracking for the request, such as success/failure counts and latency.
	 */
	protected BasicMeters meters;

	/**
	 * The authentication type.
	 */
	protected AuthenticationType authenticationType;

	/**
	 * Compares this {@link ApiRequest} with another object for equality.
	 *
	 * @param obj the object to compare with
	 * @return true if the objects are equal, false otherwise
	 */
	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	/**
	 * Generates a hash code for this {@link ApiRequest}.
	 *
	 * @return the hash code for this object
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	/**
	 * Returns a JSON representation of this {@link ApiRequest}.
	 *
	 * @return a JSON string representing this object
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the method for the request.
	 *
	 * @return the method
	 */
	public <R extends RequestMethod> R getMethod() {
		return JavaObjects.cast(method);
	}

	/**
	 * Returns the URL for the request.
	 *
	 * @return the URL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Constructs and returns the full URI for the request, including query parameters.
	 *
	 * @return the full URI
	 */
	public URI getUri() {
		return URI.create(getUrl() + RequestParameters.asUrlSuffix(getParams()));
	}

	/**
	 * Returns the query parameters for the request.
	 *
	 * @return a map of query parameters
	 */
	public Map<String, String> getParams() {
		return params;
	}

	/**
	 * Returns the generic response type for the request.
	 *
	 * @param <U> the return type
	 *
	 * @return the generic response type
	 */
	public <U> GenericClass<U> getGenericResponseType() {
		return JavaObjects.cast(genericResponseType);
	}

	/**
	 * Checks if the request has a generic response type defined.
	 *
	 * @return true if a generic response type is defined, false otherwise
	 */
	public boolean hasGenericType() {
		return null != genericResponseType;
	}

	/**
	 * Returns the class response type for the request.
	 *
	 * @param <U> the return type
	 *
	 * @return the class response type
	 */
	public <U> Class<U> getClassResponseType() {
		return JavaObjects.cast(classResponseType);
	}

	/**
	 * Returns the response type.
	 *
	 * @return the response type
	 * @throws IllegalStateException if the request doesn't have a response type set
	 */
	public Type getResponseType() {
		if (hasGenericType()) {
			return genericResponseType.getType();
		}
		if (null != classResponseType) {
			return classResponseType;
		}
		throw new IllegalStateException("No response type defined!");
	}

	/**
	 * Returns the retry configuration for the request.
	 *
	 * @return the retry configuration
	 */
	public Retry getRetry() {
		return retry;
	}

	/**
	 * Returns the metrics tracking configuration for the request.
	 *
	 * @return the metrics configuration
	 */
	public BasicMeters getMeters() {
		return meters;
	}

	/**
	 * Checks if the request parameters should be URL-encoded.
	 *
	 * @return true if URL encoding is enabled, false otherwise
	 */
	public boolean isUrlEncoded() {
		return urlEncoded;
	}

	/**
	 * Checks if the response should be handled as a stream.
	 *
	 * @return true if streaming is enabled, false otherwise
	 */
	public boolean isStream() {
		return stream;
	}

	/**
	 * Returns the character set for the request.
	 *
	 * @return the character set
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * Returns the authentication type.
	 *
	 * @return the authentication type
	 */
	public AuthenticationType getAuthenticationType() {
		return authenticationType;
	}

}

package org.apiphany.client;

import java.util.List;

import org.apiphany.ApiMessage;
import org.apiphany.ApiMimeType;
import org.apiphany.ApiRequest;
import org.apiphany.header.HeaderValues;
import org.apiphany.http.ContentEncoding;
import org.apiphany.lang.gzip.GZip;
import org.morphix.reflection.GenericClass;

/**
 * An interface for converting content from one format to another. This interface provides methods to convert objects to
 * specific types and check if conversion is possible.
 *
 * @param <T> the target type of the conversion.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ContentConverter<T> {

	/**
	 * Converts the given object to an instance of the specified class.
	 *
	 * @param obj the object to convert
	 * @param contentType the content type
	 * @param targetClass the target class to convert the object to
	 * @return the converted object as an instance of the specified class
	 */
	T from(Object obj, ApiMimeType contentType, Class<T> targetClass);

	/**
	 * Converts the given object to an instance of the specified generic class generic type.
	 *
	 * @param obj the object to convert
	 * @param contentType the content type
	 * @param targetGenericClass the target generic class to convert the object to
	 * @return the converted object as an instance of the specified generic class generic type
	 */
	T from(Object obj, ApiMimeType contentType, GenericClass<T> targetGenericClass);

	/**
	 * Checks if the converter can convert the content of the given {@link ApiMessage} to the specified type, considering
	 * the provided headers.
	 *
	 * @param <U> the type of the message body.
	 * @param <H> the type of the headers.
	 *
	 * @param message the {@link ApiMessage} containing the content to convert
	 * @param contentType the content type
	 * @param headers the headers that may influence the conversion
	 * @param chain chain of header values extractor that will be used to get a specific header list
	 * @return true if the converter can perform the conversion, false otherwise
	 */
	<U, H> boolean isConvertible(ApiMessage<U> message, ApiMimeType contentType, H headers, HeaderValues chain);

	/**
	 * Retrieves the values of a specific header from the provided headers object.
	 *
	 * @param <H> the type of the headers object
	 * @param <N> header name type
	 *
	 * @param header the name of the header whose values are to be retrieved
	 * @param headers the headers object from which to retrieve the values
	 * @param chain chain of header values extractor that will be used to get a specific header list
	 * @return a list of values for the specified header. If the header is not found or the {@code headers} parameter is of
	 * an unsupported type, an empty list is returned
	 */
	default <H, N> List<String> getHeaderValues(final N header, final H headers, final HeaderValues chain) {
		return chain.get(header, headers);
	}

	/**
	 * Converts the response body to the desired type using the provided {@link ContentConverter}. This method handles both
	 * generic and non-generic response types based on the {@link ApiRequest} configuration.
	 *
	 * @param <U> the target type to convert the body to
	 * @param <T> the type of the original request body
	 *
	 * @param typeConverter the content converter to use for the conversion
	 * @param apiRequest the API request containing response type information
	 * @param mimeType the content type
	 * @param body the response body to be converted
	 * @return the converted body of type {@code U}
	 * @throws IllegalArgumentException if the conversion fails
	 * @throws UnsupportedOperationException if the request specifies an unsupported conversion
	 */
	static <U, T> U convertBody(final ContentConverter<U> typeConverter, final ApiRequest<T> apiRequest, final ApiMimeType mimeType,
			final Object body) {
		return apiRequest.hasGenericType()
				? typeConverter.from(body, mimeType, apiRequest.getGenericResponseType())
				: typeConverter.from(body, mimeType, apiRequest.getClassResponseType());
	}

	/**
	 * Decodes the body based on the given content encoding.
	 * <p>
	 * TODO: support more content encodings
	 *
	 * @param <T> body type
	 *
	 * @param body the body
	 * @param contentEncoding content encoding
	 * @return decoded body
	 */
	static <T> T decodeBody(final T body, final ContentEncoding contentEncoding) {
		if (null == body) {
			return null;
		}
		if (null == contentEncoding) {
			return body;
		}
		return switch (contentEncoding) {
			case GZIP -> {
				try {
					yield GZip.decompress(body);
				} catch (Exception e) {
					throw new IllegalStateException("Failed to decode response body with encoding: " + contentEncoding, e);
				}
			}
			default -> throw new UnsupportedOperationException("Content encoding " + contentEncoding + " is not supported!");
		};
	}
}

package org.apiphany.client;

import java.util.List;

import org.apiphany.ApiMessage;
import org.apiphany.ApiRequest;
import org.apiphany.header.HeaderValuesChain;
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
	 * @param obj the object to convert.
	 * @param dstClass the target class to convert the object to.
	 * @return the converted object as an instance of the specified class.
	 */
	T from(Object obj, Class<T> dstClass);

	/**
	 * Converts the given object to an instance of the specified generic class.
	 *
	 * @param obj the object to convert.
	 * @param genericDstClass the target generic class to convert the object to.
	 * @return the converted object as an instance of the specified generic class.
	 */
	T from(Object obj, GenericClass<T> genericDstClass);

	/**
	 * Checks if the converter can convert the content of the given {@link ApiMessage} to the specified type, considering
	 * the provided headers.
	 *
	 * @param <U> the type of the message body.
	 * @param <V> the type of the headers.
	 *
	 * @param message the {@link ApiMessage} containing the content to convert.
	 * @param headers the headers that may influence the conversion.
	 * @param headerValuesChain chain of header values that will be used to get a specific header list
	 * @return true if the converter can perform the conversion, false otherwise.
	 */
	<U, V> boolean isConvertible(ApiMessage<U> message, V headers, HeaderValuesChain headerValuesChain);

	/**
	 * Retrieves the values of a specific header from the provided headers object.
	 *
	 * @param <V> the type of the headers object
	 * @param <N> header name type
	 *
	 * @param headers the headers object from which to retrieve the values.
	 * @param header the name of the header whose values are to be retrieved.
	 * @param headerValuesChain chain of header values that will be used to get a specific header list
	 * @return a list of values for the specified header. If the header is not found or the headers object is of an
	 * unsupported type, an empty list is returned.
	 */
	default <V, N> List<String> getHeaderValues(final V headers, final N header, final HeaderValuesChain headerValuesChain) {
		return headerValuesChain.get(header, headers);
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
	 * @param body the response body to be converted
	 * @return the converted body of type {@code U}
	 * @throws IllegalArgumentException if the conversion fails
	 * @throws UnsupportedOperationException if the request specifies an unsupported conversion
	 */
	static <U, T> U convertBody(final ContentConverter<U> typeConverter, final ApiRequest<T> apiRequest, final Object body) {
		return apiRequest.hasGenericType()
				? typeConverter.from(body, apiRequest.getGenericResponseType())
				: typeConverter.from(body, apiRequest.getClassResponseType());
	}
}

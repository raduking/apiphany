package org.apiphany;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Maps;
import org.morphix.reflection.Constructors;

/**
 * Utility class for building and manipulating request parameters. This class provides methods for creating parameter
 * maps, encoding parameters, and converting them into URL-friendly formats. It also includes a functional interface
 * {@link ParameterFunction} for defining parameter insertion logic.
 *
 * @author Radu Sebastian LAZIN
 */
public class RequestParameters {

	/**
	 * Request parameters separator.
	 */
	public static final String SEPARATOR = "&";

	/**
	 * Creates a new map and populates it with the given external put functions.
	 *
	 * @param paramFunctions the {@link ParameterFunction}s to execute
	 * @return a new map containing the inserted parameters
	 */
	public static Map<String, String> of(final ParameterFunction... paramFunctions) {
		if (null == paramFunctions || 0 == paramFunctions.length) {
			return Collections.emptyMap();
		}
		var map = new HashMap<String, String>();
		for (ParameterFunction paramFunction : paramFunctions) {
			paramFunction.putInto(map);
		}
		return map;
	}

	/**
	 * Transforms the request parameters map into a string usable in URLs/URIs. The string will start with the {@code '?'}
	 * character, so no additional concatenation is needed. If the map is empty, an empty string is returned.
	 *
	 * @param params the request parameters map
	 * @return a URL-friendly string representation of the parameters
	 */
	public static String asUrlSuffix(final Map<String, String> params) {
		String result = asString(params);
		if (Strings.isNotEmpty(result)) {
			result = "?" + result;
		}
		return result;
	}

	/**
	 * Transforms the request parameters map into a string usable in request bodies. If the map is empty, an empty string is
	 * returned.
	 *
	 * @param params the request parameters map
	 * @return a string representation of the parameters
	 */
	public static String asString(final Map<String, String> params) {
		if (Maps.isEmpty(params)) {
			return "";
		}
		String[] paramsArray = params.entrySet().stream()
				.map(entry -> String.join("=", entry.getKey(), entry.getValue()))
				.toArray(String[]::new);
		return String.join(SEPARATOR, paramsArray);
	}

	/**
	 * Transforms the request parameters string into a request parameters {@link Map}. If the string is empty, an empty
	 * immutable map is returned. It assumes the body character encoding is {@link Strings#DEFAULT_CHARSET}.
	 *
	 * @param body the request parameters string
	 * @return a {@link Map} representation of the parameters
	 */
	public static Map<String, String> from(final String body) {
		return from(body, Strings.DEFAULT_CHARSET);
	}

	/**
	 * Transforms the request parameters string into a request parameters {@link Map}. If the string is empty, an empty
	 * immutable map is returned.
	 *
	 * @param body the request parameters string
	 * @param encoding the body character encoding
	 * @return a {@link Map} representation of the parameters
	 */
	public static Map<String, String> from(final String body, final Charset encoding) {
		if (Strings.isEmpty(body)) {
			return Collections.emptyMap();
		}
		String[] params = body.split(SEPARATOR);
		Map<String, String> paramsMap = new HashMap<>();

		for (String param : params) {
			int index = param.indexOf('=');
			final String key;
			final String value;

			if (index >= 0) {
				key = param.substring(0, index);
				value = param.substring(index + 1);
			} else {
				key = param;
				value = "";
			}
			String decodedKey = URLDecoder.decode(key, encoding);
			String decodedValue = URLDecoder.decode(value, encoding);
			paramsMap.put(decodedKey, decodedValue);
		}
		return paramsMap;
	}

	/**
	 * Encodes the given request parameters using UTF-8 encoding. Returns a new map with all parameters encoded.
	 *
	 * @param requestParameters the request parameters to encode
	 * @return a new map containing the encoded parameters
	 */
	public static Map<String, String> encode(final Map<String, String> requestParameters) {
		return encode(requestParameters, Strings.DEFAULT_CHARSET);
	}

	/**
	 * Encodes the given request parameters using the specified character set. Returns a new map with all parameters
	 * encoded.
	 *
	 * @param requestParameters the request parameters to encode
	 * @param encoding the character set to use for encoding
	 * @return a new map containing the encoded parameters
	 */
	public static Map<String, String> encode(final Map<String, String> requestParameters, final Charset encoding) {
		Map<String, String> encodedParams = HashMap.newHashMap(requestParameters.size());
		requestParameters.forEach((key, value) -> {
			String encodedName = URLEncoder.encode(key, encoding);
			String encodedValue = URLEncoder.encode(value, encoding);
			encodedParams.put(encodedName, encodedValue);
		});
		return encodedParams;
	}

	/**
	 * Hide constructor.
	 */
	private RequestParameters() {
		throw Constructors.unsupportedOperationException();
	}
}

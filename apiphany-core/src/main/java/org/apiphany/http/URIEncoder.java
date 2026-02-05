package org.apiphany.http;

import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * Name space for URI encoding utilities.
 *
 * @author Radu Sebastian LAZIN
 */
public interface URIEncoder {

	/**
	 * Encodes the given string for safe inclusion in a URI path component.
	 * <p>
	 * Follows RFC 3986, spaces are encoded as {@code %20} instead of {@code +}
	 *
	 * @param string the string to encode
	 * @return the encoded string
	 */
	static String encodePath(final String string, final Charset charset) {
		return URLEncoder.encode(string, charset).replaceAll("\\+", "%20");
	}

	/**
	 * Encodes the given string for safe inclusion as a parameter name in a URI.
	 * <p>
	 * Conservative approach for query names (%20) - avoids edge cases.
	 *
	 * @param string the string to encode
	 * @return the encoded string
	 */
	static String encodeParamName(final String string, final Charset charset) {
		return encodePath(string, charset);
	}

	/**
	 * Encodes the given string for safe inclusion as a parameter value in a URI.
	 * <p>
	 * {@code application/x-www-form-urlencoded} for query values (+) - practical compatibility.
	 *
	 * @param string the string to encode
	 * @return the encoded string
	 */
	static String encodeParamValue(final String string, final Charset charset) {
		return URLEncoder.encode(string, charset);
	}
}

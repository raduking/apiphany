package org.apiphany.multipart;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apiphany.ApiMessage;
import org.apiphany.header.Headers;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpHeaderValues;
import org.apiphany.io.BinaryRepresentable;
import org.apiphany.io.ContentType;
import org.apiphany.lang.Bytes;
import org.apiphany.lang.Strings;
import org.morphix.lang.Nullables;
import org.morphix.lang.collections.Lists;

/**
 * Represents a single part in a multipart message.
 *
 * @param <T> the type of the body content of this multipart part
 *
 * @author Radu Sebastian LAZIN
 */
public class MultipartPart<T> extends ApiMessage<T> implements BinaryRepresentable {

	/**
	 * Constructs a new {@code MultipartPart} with the specified headers and body. The headers must not be {@code null}.
	 *
	 * @param headers the headers associated with this multipart part
	 * @param body the body of this multipart part, which can be of any type
	 * @throws NullPointerException if {@code headers} is {@code null}
	 */
	public MultipartPart(final Map<String, List<String>> headers, final T body) {
		super(body, headers);
	}

	/**
	 * @see BinaryRepresentable#toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		T body = getBody();
		return switch (body) {
			case null -> Bytes.EMPTY;
			case String str -> str.getBytes(StandardCharsets.UTF_8);
			case byte[] bytes -> bytes;
			default -> throw new IllegalStateException("Unsupported body type: " + body.getClass().getName());
		};
	}

	/**
	 * Retrieves the name of this multipart part from the Content-Disposition header. The method looks for a header value
	 * that starts with "name=" and extracts the name from it. If no such header is found, or if the header value is
	 * malformed, this method returns {@code null}.
	 *
	 * @return the name of this multipart part, or {@code null} if not found
	 */
	protected String getName() {
		List<String> dispositions = getHeaderValues(HttpHeader.CONTENT_DISPOSITION);
		for (String disposition : Lists.safe(dispositions)) {
			String[] parts = disposition.split(";");
			for (String part : parts) {
				String trimmed = part.trim();
				if (trimmed.startsWith("name=")) {
					return trimmed.substring(5).replaceAll("(^\")|(\"$)", "");
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves the value of this multipart part as a string. The method assumes that the body of the part is encoded in
	 * UTF-8 and decodes it accordingly. If the body is {@code null}, this method returns {@code null}.
	 *
	 * @return the value of this multipart part as a string, or {@code null} if the body is {@code null}
	 */
	protected String getValue() {
		T body = getBody();
		return switch (body) {
			case null -> null;
			case String str -> str;
			case byte[] bytes -> new String(bytes, StandardCharsets.UTF_8);
			default -> throw new IllegalStateException("Unsupported body type: " + body.getClass().getName());
		};
	}

	/**
	 * Factory method to create a multipart part representing a form field with the given name and value. The method
	 * constructs the appropriate headers for the multipart part, including the Content-Disposition header with the
	 * specified name and a value of "form-data". The body of the multipart part is set to the provided value, which can be
	 * of any type (e.g., string, byte array).
	 *
	 * @param <T> the type of the value, which can be of any type (e.g., string, byte array)
	 *
	 * @param name the field name for this multipart part
	 * @param value the value of this multipart part, which can be of any type (e.g., string, byte array)
	 * @return the filename of this multipart part, or {@code null} if not found
	 */
	public static <T> MultipartPart<T> ofField(final String name, final T value) {
		Map<String, List<String>> headers = new LinkedHashMap<>();
		Headers.addTo(headers, HttpHeader.CONTENT_DISPOSITION, HttpHeaderValues.FORM_DATA + "; name=\"" + name + "\"");
		return new MultipartPart<>(headers, value);
	}

	/**
	 * Factory method to create a multipart part representing a file upload with the given parameters. The method constructs
	 * the appropriate headers for the multipart part, including the Content-Disposition header with the specified name and
	 * filename, and the Content-Type header with the specified content type. If the content type is {@code null}, it
	 * defaults to {@code application/octet-stream}.
	 *
	 * @param <T> the type of the file data, which can be of any type (e.g., byte array, string)
	 *
	 * @param name the field name for this file part
	 * @param filename the original filename of the uploaded file
	 * @param contentType the content type of the uploaded file, or {@code null} to default to
	 *     {@code application/octet-stream}
	 * @param data the file data, which can be of any type (e.g., byte array, string)
	 * @return a new {@code MultipartPart} instance representing the file upload
	 */
	public static <T> MultipartPart<T> ofFile(final String name, final String filename, final String contentType, final T data) {
		Map<String, List<String>> headers = new LinkedHashMap<>();
		String disposition = HttpHeaderValues.FORM_DATA + "; name=\"" + name + "\"";
		if (Strings.isNotBlank(filename)) {
			disposition += "; filename=\"" + filename + "\"";
		}
		Headers.addTo(headers, HttpHeader.CONTENT_DISPOSITION, disposition);
		String actualContentType = Nullables.nonNullOrDefault(contentType, ContentType.Value.APPLICATION_OCTET_STREAM);
		Headers.addTo(headers, HttpHeader.CONTENT_TYPE, actualContentType);
		return new MultipartPart<>(headers, data);
	}
}

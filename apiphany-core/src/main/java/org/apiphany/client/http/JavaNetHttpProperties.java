package org.apiphany.client.http;

import java.net.http.HttpClient.Version;
import java.util.Objects;

import org.apiphany.client.ClientProperties;
import org.apiphany.http.HttpMessages;
import org.apiphany.http.JavaNetHttpLibrary;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.annotation.Ignored;

/**
 * Represents properties for configuring the {@code java.net.http} HTTP client. This class includes nested properties
 * for HTTP request configurations.
 * <p>
 * To configure these properties in the {@link ClientProperties} under the {@code custom} root, use the prefix
 * {@code java-net-http} (as defined in {@link #ROOT}). For example:
 *
 * <pre>
 * my-client-properties.custom.java-net-http.request.version=HTTP/2
 * </pre>
 *
 * or in YAML:
 *
 * <pre>
 * my-client-properties:
 *  custom:
 *    java-net-http:
 *      request:
 *        version: HTTP/2
 * </pre>
 *
 * The default HTTP version is HTTP/1.1 ({@link Request.Default#HTTP_VERSION}).
 *
 * @author Radu Sebastian LAZIN
 */
public class JavaNetHttpProperties {

	/**
	 * The root key for {@code java.net.http} HTTP client properties.
	 */
	public static final String ROOT = JavaNetHttpLibrary.CLIENT_NAME;

	/**
	 * The request properties for the HTTP client.
	 */
	private Request request = new Request();

	/**
	 * Default constructor.
	 */
	public JavaNetHttpProperties() {
		// empty
	}

	/**
	 * Returns a JSON representation of this {@link JavaNetHttpProperties} object.
	 *
	 * @return a JSON string representing this object.
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof JavaNetHttpProperties that) {
			return Objects.equals(this.request, that.request);
		}
		return false;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(request);
	}

	/**
	 * Returns the request properties for the HTTP client.
	 *
	 * @return the request properties.
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * Sets the request properties for the HTTP client.
	 *
	 * @param request the request properties to set.
	 */
	public void setRequest(final Request request) {
		this.request = request;
	}

	/**
	 * Represents properties for configuring HTTP requests.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Request {

		/**
		 * Constants for default connection property values.
		 *
		 * @author Radu Sebastian LAZIN
		 */
		public static class Default {

			/**
			 * The default HTTP version for requests (HTTP/1.1).
			 */
			public static final Version HTTP_VERSION = Version.HTTP_1_1;

			private Default() {
				// empty
			}
		}

		/**
		 * The HTTP version as a string (e.g., "HTTP/1.1" or "HTTP/2").
		 */
		private String version = HttpMessages.toProtocolString(Default.HTTP_VERSION);

		/**
		 * Default constructor.
		 */
		public Request() {
			// empty
		}

		/**
		 * Returns a JSON representation of this {@link Request} object.
		 *
		 * @return a JSON string representing this object.
		 */
		@Override
		public String toString() {
			return JsonBuilder.toJson(this);
		}

		/**
		 * Returns the HTTP version as a string.
		 *
		 * @return the HTTP version as a string.
		 */
		public String getVersion() {
			return version;
		}

		/**
		 * @see Object#equals(Object)
		 */
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof Request that) {
				return Objects.equals(this.version, that.version);
			}
			return false;
		}

		/**
		 * @see Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(version);
		}

		/**
		 * Returns the HTTP version as a {@link Version} enum. If parsing fails, the default HTTP version (HTTP/1.1) is
		 * returned.
		 *
		 * @return the HTTP version as a {@link Version} enum.
		 */
		@Ignored
		public Version getHttpVersion() {
			try {
				return HttpMessages.parseJavaNetHttpVersion(version);
			} catch (Exception e) {
				return Default.HTTP_VERSION;
			}
		}

		/**
		 * Sets the HTTP version as a string.
		 *
		 * @param version the HTTP version to set (e.g., "HTTP/1.1" or "HTTP/2").
		 */
		public void setVersion(final String version) {
			this.version = version;
		}
	}
}

package org.apiphany.client.http;

import java.util.function.Predicate;

import org.apiphany.RequestMethod;
import org.apiphany.client.ExchangeClient;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;

/**
 * Adds the HTTP methods to the {@link ExchangeClient} interface.
 *
 * @author Radu Sebastian LAZIN
 */
public interface HttpExchangeClient extends ExchangeClient {

	/**
	 * Returns the GET request method.
	 *
	 * @return the GET request method
	 */
	default RequestMethod get() {
		return HttpMethod.GET;
	}

	/**
	 * Returns the PUT request method.
	 *
	 * @return the PUT request method
	 */
	default RequestMethod put() {
		return HttpMethod.PUT;
	}

	/**
	 * Returns the POST request method.
	 *
	 * @return the POST request method
	 */
	default RequestMethod post() {
		return HttpMethod.POST;
	}

	/**
	 * Returns the DELETE request method.
	 *
	 * @return the DELETE request method
	 */
	default RequestMethod delete() {
		return HttpMethod.DELETE;
	}

	/**
	 * Returns the PATCH request method.
	 *
	 * @return the PATCH request method
	 */
	default RequestMethod patch() {
		return HttpMethod.PATCH;
	}

	/**
	 * Returns the HEAD request method.
	 *
	 * @return the HEAD request method
	 */
	default RequestMethod head() {
		return HttpMethod.HEAD;
	}

	/**
	 * Returns the OPTIONS request method.
	 *
	 * @return the OPTIONS request method
	 */
	default RequestMethod options() {
		return HttpMethod.OPTIONS;
	}

	/**
	 * Returns the TRACE request method.
	 *
	 * @return the TRACE request method
	 */
	default RequestMethod trace() {
		return HttpMethod.TRACE;
	}

	/**
	 * Redact the {@link HttpHeader#AUTHORIZATION} header.
	 *
	 * @return redacted headers predicate
	 */
	@Override
	default Predicate<String> isSensitiveHeader() {
		return HttpHeader.AUTHORIZATION::matches;
	}

}

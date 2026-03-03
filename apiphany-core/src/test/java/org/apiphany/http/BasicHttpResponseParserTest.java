package org.apiphany.http;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

class BasicHttpResponseParserTest {

	@Test
	void shouldParseSimpleHttpResponse() {
		String response = """
				HTTP/1.1 200 OK\r
				Content-Type: text/plain\r
				Content-Length: 13\r
				\r
				Hello, World!""";

		BasicHttpResponseParser parser = new BasicHttpResponseParser(response);

		assertThat(parser.getStatus(), is("HTTP/1.1 200 OK"));
		assertThat(parser.getStatusCode(), is(200));
		assertThat(parser.getHeader("Content-Type"), is("text/plain"));
		assertThat(parser.getHeader("Content-Length"), is("13"));
		assertThat(parser.getBody(), is("Hello, World!"));
		assertThat(parser.isComplete(), is(true));
	}

	@Test
	void shouldHandleChunkedTransferEncoding() {
		String response = """
				HTTP/1.1 200 OK\r
				Transfer-Encoding: chunked\r
				\r
				5\r
				Hello\r
				6\r
				, Worl\r
				2\r
				d!\r
				0\r
				\r
				""";

		BasicHttpResponseParser parser = new BasicHttpResponseParser(response);

		assertThat(parser.getStatusCode(), is(200));
		assertThat(parser.getHeader("Transfer-Encoding"), is("chunked"));
		assertThat(parser.getBody(), is("Hello, World!"));
		assertThat(parser.isComplete(), is(true));
	}

	@Test
	void shouldHandleIncrementalDataAppends() {
		String headerPart = """
				HTTP/1.1 200 OK\r
				Transfer-Encoding: chunked\r
				\r
				""";
		String chunk1 = "5\r\nHello\r\n";
		String chunk2 = "6\r\n, Worl\r\n";
		String chunk3 = "2\r\nd!\r\n";
		String chunk4 = "0\r\n\r\n";

		BasicHttpResponseParser parser = new BasicHttpResponseParser(headerPart);
		assertThat(parser.isComplete(), is(false));

		parser.appendData(chunk1);
		assertThat(parser.getBody(), is("Hello"));
		assertThat(parser.isComplete(), is(false));

		parser.appendData(chunk2);
		assertThat(parser.getBody(), is("Hello, Worl"));
		assertThat(parser.isComplete(), is(false));

		parser.appendData(chunk3);
		assertThat(parser.getBody(), is("Hello, World!"));
		assertThat(parser.isComplete(), is(false));

		parser.appendData(chunk4);
		assertThat(parser.getBody(), is("Hello, World!"));
		assertThat(parser.isComplete(), is(true));
	}

	@Test
	void shouldHandleResponseWithoutBody() {
		String response = """
			HTTP/1.1 204 No Content\r
			Content-Length: 0\r
			\r
			""";

		BasicHttpResponseParser parser = new BasicHttpResponseParser(response);

		assertThat(parser.getStatusCode(), is(204));
		assertThat(parser.getHeader("Content-Type"), is(nullValue()));
		assertThat(parser.getBody(), is(""));
		assertThat(parser.isComplete(), is(true));
	}
}

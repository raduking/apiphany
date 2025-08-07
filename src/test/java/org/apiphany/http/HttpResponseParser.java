package org.apiphany.http;

import java.util.Map;
import java.util.TreeMap;

public class HttpResponseParser {

	private final String statusLine;
	private final Map<String, String> headers;
	private final String body;

	public HttpResponseParser(final String rawResponse) {
		String[] parts = rawResponse.split("\r?\n\r?\n", 2);

		String[] headerLines = parts[0].split("\r?\n");
		this.statusLine = headerLines[0];
		this.headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		for (int i = 1; i < headerLines.length; i++) {
			String[] header = headerLines[i].split(":\\s*", 2);
			if (header.length == 2) {
				headers.put(header[0], header[1]);
			}
		}
		this.body = (parts.length > 1) ? parts[1] : "";
	}

	public String getStatus() {
		return statusLine;
	}

	public int getStatusCode() {
		return Integer.parseInt(statusLine.split(" ")[1]);
	}

	public String getHeader(final String name) {
		return headers.get(name);
	}

	public String getBody() {
		return body;
	}
}

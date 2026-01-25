package org.apiphany.utils.http;

import java.util.Map;
import java.util.TreeMap;

public class BasicHttpResponseParser {

	private final String statusLine;
	private final Map<String, String> headers;
	private final boolean chunked;
	private final Integer contentLength;

	private final StringBuilder bodyBuilder = new StringBuilder();
	private String buffer = "";

	private boolean complete = false;

	public BasicHttpResponseParser(final String rawResponse) {
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

		this.chunked = "chunked".equalsIgnoreCase(headers.get("transfer-encoding"));
		this.contentLength = headers.containsKey("content-length") ? Integer.parseInt(headers.get("content-length")) : null;

		this.buffer = (parts.length > 1) ? parts[1] : "";

		if (chunked) {
			processChunks();
		} else {
			bodyBuilder.append(buffer);
			checkCompletion();
		}
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
		return bodyBuilder.toString();
	}

	public boolean isComplete() {
		return complete;
	}

	public void appendData(final String data) {
		buffer += data;
		if (chunked) {
			processChunks();
		} else {
			bodyBuilder.append(data);
			checkCompletion();
		}
	}

	private void processChunks() {
		while (true) {
			int endIndex = buffer.indexOf("\r\n");
			if (endIndex == -1) {
				return; // not enough data for size
			}

			String sizeStr = buffer.substring(0, endIndex).trim();
			int chunkSize = Integer.parseInt(sizeStr, 16);

			if (buffer.length() < endIndex + 2 + chunkSize + 2) {
				return; // wait for more data
			}

			if (chunkSize == 0) {
				complete = true;
				buffer = ""; // discard trailing headers if any
				return;
			}

			bodyBuilder.append(buffer, endIndex + 2, endIndex + 2 + chunkSize);
			buffer = buffer.substring(endIndex + 2 + chunkSize + 2);
		}
	}

	private void checkCompletion() {
		if (contentLength != null && bodyBuilder.length() >= contentLength) {
			complete = true;
		}
	}
}

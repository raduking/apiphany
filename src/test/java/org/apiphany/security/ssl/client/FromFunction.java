package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

public interface FromFunction<T extends TLSObject> {

	T from(InputStream is, int size) throws IOException;

	interface NoSize<T extends TLSObject> {
		T from(InputStream is) throws IOException;
	}

	static <T extends TLSObject> FromFunction<T> ignoreSize(final NoSize<T> noSize) {
		return (is, size) -> noSize.from(is);
	}
}

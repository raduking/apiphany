package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

public interface FromFunction<T extends TLSObject> {

	T from(InputStream is, int size) throws IOException;

	static <T extends TLSObject> FromFunction<T> ignoreSize(NoSize<T> fromFunction) {
		return (is, size) -> fromFunction.from(is);
	}

	interface NoSize<T extends TLSObject> {
		T from(InputStream is) throws IOException;
	}
}

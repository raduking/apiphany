package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

public interface FromFunction<T extends TLSObject> {

	T from(InputStream is) throws IOException;

}

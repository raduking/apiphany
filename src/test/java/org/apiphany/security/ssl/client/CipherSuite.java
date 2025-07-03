package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class CipherSuite {

	private CipherSuiteName cipher;

	public CipherSuite(final CipherSuiteName cypher) {
		this.cipher = cypher;
	}

	public CipherSuite(final short cypher) {
		this(CipherSuiteName.fromValue(cypher));
	}

	public static CipherSuite from(final InputStream is) throws IOException {
		Int16 cypher = Int16.from(is);

		return new CipherSuite(cypher.getValue());
	}

	public byte[] toByteArray() {
		return Bytes.from(cipher.value());
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public CipherSuiteName getCipher() {
		return cipher;
	}
}

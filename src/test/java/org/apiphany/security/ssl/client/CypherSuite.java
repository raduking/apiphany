package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class CypherSuite {

	private CypherSuiteName cypher;

	public CypherSuite(final CypherSuiteName cypher) {
		this.cypher = cypher;
	}

	public CypherSuite(final short cypher) {
		this(CypherSuiteName.fromValue(cypher));
	}

	public static CypherSuite from(final InputStream is) throws IOException {
		Int16 cypher = Int16.from(is);

		return new CypherSuite(cypher.getValue());
	}

	public byte[] toByteArray() {
		return Bytes.from(cypher.value());
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public CypherSuiteName getCypher() {
		return cypher;
	}
}

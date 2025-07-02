package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class CypherSuite {

	private Int16 cypher;

	public CypherSuite(Int16 cypher) {
		this.cypher = cypher;
	}

	public CypherSuite(short cypher) {
		this(new Int16(cypher));
	}

	public static CypherSuite from(InputStream is) throws IOException {
		Int16 cypher = Int16.from(is);

		return new CypherSuite(cypher);
	}

	public byte[] toByteArray() {
		return Bytes.from(cypher.getValue());
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public Int16 getCypher() {
		return cypher;
	}
}

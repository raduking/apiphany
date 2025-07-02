package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CypherSuites {

	private Int16 size;

	private List<CypherSuite> cypherSuites;

	public CypherSuites(final Int16 size, final List<CypherSuite> cypherSuites) {
		this.size = size;
		this.cypherSuites = new ArrayList<>(size.getValue());
		this.cypherSuites.addAll(cypherSuites);
	}

	public CypherSuites(final short size, final List<CypherSuite> cypherSuites) {
		this(new Int16(size), cypherSuites);
	}

	public CypherSuites(final List<CypherSuite> cypherSuites) {
		this((short) (cypherSuites.size() * 2), cypherSuites);
	}

	public CypherSuites(final CypherSuite... cypherSuites) {
		this(List.of(cypherSuites));
	}

	public CypherSuites(final CypherSuiteName... cypherSuites) {
		this(List.of(cypherSuites).stream().map(CypherSuite::new).toList());
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(size.toByteArray());
		for (CypherSuite cypherSuite : cypherSuites) {
			dos.write(cypherSuite.toByteArray());
		}

		return bos.toByteArray();
	}
}

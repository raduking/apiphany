package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CipherSuites {

	private Int16 size;

	private List<CipherSuite> cypherSuites;

	public CipherSuites(final Int16 size, final List<CipherSuite> cypherSuites) {
		this.size = size;
		this.cypherSuites = new ArrayList<>(size.getValue());
		this.cypherSuites.addAll(cypherSuites);
	}

	public CipherSuites(final short size, final List<CipherSuite> cypherSuites) {
		this(new Int16(size), cypherSuites);
	}

	public CipherSuites(final List<CipherSuite> cypherSuites) {
		this((short) (cypherSuites.size() * 2), cypherSuites);
	}

	public CipherSuites(final CipherSuite... cypherSuites) {
		this(List.of(cypherSuites));
	}

	public CipherSuites(final CipherSuiteName... cypherSuites) {
		this(List.of(cypherSuites).stream().map(CipherSuite::new).toList());
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(size.toByteArray());
		for (CipherSuite cypherSuite : cypherSuites) {
			dos.write(cypherSuite.toByteArray());
		}

		return bos.toByteArray();
	}
}

package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.io.UInt16;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ByteSizeable;
import org.apiphany.security.tls.TLSObject;

public class CipherSuites implements TLSObject {

	private final UInt16 size;

	private final List<CipherSuite> cipherSuites;

	public CipherSuites(final UInt16 size, final List<CipherSuite> cipherSuites) {
		this.size = size;
		this.cipherSuites = new ArrayList<>(size.getValue());
		this.cipherSuites.addAll(cipherSuites);
	}

	public CipherSuites(final short size, final List<CipherSuite> cipherSuites) {
		this(UInt16.of(size), cipherSuites);
	}

	public CipherSuites(final List<CipherSuite> cipherSuites) {
		this((short) (cipherSuites.size() * 2), cipherSuites);
	}

	public CipherSuites(final CipherSuite cipherSuites) {
		this(List.of(cipherSuites));
	}

	public CipherSuites(final CipherSuite... cipherSuites) {
		this(List.of(cipherSuites));
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(size.toByteArray());
		for (CipherSuite cypherSuite : cipherSuites) {
			buffer.put(cypherSuite.toByteArray());
		}
		return buffer.array();
	}

	public static CipherSuites from(final InputStream is) throws IOException {
		UInt16 size = UInt16.from(is);
		List<CipherSuite> cipherSuites = new ArrayList<>();
		for (int i = 0; i < size.getValue() / 2; ++i) {
			UInt16 int16 = UInt16.from(is);
			CipherSuite cipherSuite = CipherSuite.fromValue(int16.getValue());
			cipherSuites.add(cipherSuite);
		}

		return new CipherSuites(size, cipherSuites);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return size.sizeOf() + ByteSizeable.sizeOf(cipherSuites);
	}

	public UInt16 getSize() {
		return size;
	}

	public List<CipherSuite> getCipherSuites() {
		return cipherSuites;
	}
}

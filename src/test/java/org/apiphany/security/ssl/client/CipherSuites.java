package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class CipherSuites implements TLSObject {

	private Int16 size;

	private List<CipherSuite> cipherSuites;

	public CipherSuites(final Int16 size, final List<CipherSuite> cipherSuites) {
		this.size = size;
		this.cipherSuites = new ArrayList<>(size.getValue());
		this.cipherSuites.addAll(cipherSuites);
	}

	public CipherSuites(final short size, final List<CipherSuite> cipherSuites) {
		this(new Int16(size), cipherSuites);
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
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(size.toByteArray());
			for (CipherSuite cypherSuite : cipherSuites) {
				dos.write(cypherSuite.toByteArray());
			}
		}).run();
		return bos.toByteArray();
	}

	public static CipherSuites from(final InputStream is) throws IOException {
		Int16 size = Int16.from(is);
		List<CipherSuite> cipherSuites = new ArrayList<>();
		for (int i = 0; i < size.getValue() / 2; ++i) {
			Int16 int16 = Int16.from(is);
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
	public int size() {
		int result = size.size();
		for (CipherSuite cipherSuite : cipherSuites) {
			result += cipherSuite.size();
		}
		return result;
	}

	public Int16 getSize() {
		return size;
	}

	public List<CipherSuite> getCipherSuites() {
		return cipherSuites;
	}
}

package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.collections.Lists;
import org.apiphany.security.ssl.SSLProtocol;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.function.ThrowingRunnable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TLSRecord implements TLSObject {

	private RecordHeader header;

	private List<TLSObject> fragments;

	public TLSRecord(final RecordHeader header, final List<TLSObject> fragments, final boolean updateHeader) {
		this.header = header;
		this.fragments = fragments;
		if (updateHeader) {
			short length = 0;
			for (Sizeable fragment : fragments) {
				length += fragment.size();
			}
			header.getLength().setValue(length);
		}
	}

	public TLSRecord(final RecordHeader header, final List<TLSObject> messages) {
		this(header, messages, true);
	}

	public TLSRecord(final RecordContentType type, final SSLProtocol protocol, final TLSObject fragment) {
		this(new RecordHeader(type, protocol), List.of(fragment), true);
	}

	public TLSRecord(final SSLProtocol protocol, final TLSObject fragment) {
		this(RecordContentType.from(fragment), protocol, fragment);
	}

	public TLSRecord(final SSLProtocol protocol, final TLSHandshakeBody handshakeObject) {
		this(new RecordHeader(RecordContentType.HANDSHAKE, protocol), List.of(new TLSHandshake(handshakeObject)));
	}

	public static TLSRecord from(final InputStream is) throws IOException {
		RecordHeader header = RecordHeader.from(is);
		RecordContentType recordType = header.getType();

		List<TLSObject> fragments = new ArrayList<>();
		int currentLength = header.getLength().getValue();
		while (currentLength > 0) {
			TLSObject fragment = recordType.fragment().from(is);
			fragments.add(fragment);
			currentLength -= fragment.size();
		}
		return new TLSRecord(header, fragments, false);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(header.toByteArray());
			for (TLSObject fragment : fragments) {
				dos.write(fragment.toByteArray());
			}
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		int result = header.size();
		for (TLSObject fragment : fragments) {
			result += fragment.size();
		}
		return result;
	}

	public RecordHeader getHeader() {
		return header;
	}

	public List<TLSObject> getFragments() {
		return fragments;
	}

	public TLSHandshake getHandshake(final int index) {
		return JavaObjects.cast(fragments.get(index));
	}

	public <T extends TLSObject> List<T> getFragments(final Class<T> tlsObjectClass) {
		if (Lists.isEmpty(fragments)) {
			return Collections.emptyList();
		}
		List<T> result = new ArrayList<>(fragments.size());
		for (TLSObject fragment : fragments) {
			if (tlsObjectClass.isAssignableFrom(fragment.getClass())) {
				result.add(JavaObjects.cast(fragment));
			}
		}
		return result;
	}

	@JsonIgnore
	public TLSHandshake getHandshake() {
		return getHandshake(0);
	}

	public <T extends TLSHandshakeBody> boolean hasHandshake(final Class<T> tlsHandshakeClass) {
		for (TLSObject fragment : fragments) {
			if (TLSHandshake.class.isAssignableFrom(fragment.getClass())) {
				TLSHandshake handshake = JavaObjects.cast(fragment);
				if (handshake.is(tlsHandshakeClass)) {
					return true;
				}
			}
		}
		return false;
	}

	public <T extends TLSHandshakeBody> T getHandshake(final Class<T> tlsHandshakeClass) {
		for (TLSObject fragment : fragments) {
			if (TLSHandshake.class.isAssignableFrom(fragment.getClass())) {
				TLSHandshake handshake = JavaObjects.cast(fragment);
				if (handshake.is(tlsHandshakeClass)) {
					return handshake.get(tlsHandshakeClass);
				}
			}
		}
		throw new IllegalArgumentException("No handhsake of type " + tlsHandshakeClass + " found in record");
	}
}

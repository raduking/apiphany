package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import org.apiphany.io.UInt16;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ByteSizeable;
import org.apiphany.lang.collections.Lists;
import org.apiphany.security.ssl.SSLProtocol;
import org.apiphany.security.tls.TLSObject;
import org.morphix.lang.JavaObjects;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Record implements TLSObject {

	private final RecordHeader header;

	private final List<TLSObject> fragments;

	public Record(final RecordHeader header, final List<TLSObject> fragments, final boolean updateHeader) {
		this.header = updateHeader
				? new RecordHeader(header.getType(), header.getVersion(), UInt16.of((short) ByteSizeable.sizeOf(fragments)))
				: header;
		this.fragments = fragments;
	}

	public Record(final RecordHeader header, final List<TLSObject> messages) {
		this(header, messages, true);
	}

	public Record(final RecordContentType type, final SSLProtocol protocol, final TLSObject fragment) {
		this(new RecordHeader(type, protocol), List.of(fragment), true);
	}

	public Record(final SSLProtocol protocol, final TLSObject fragment) {
		this(RecordContentType.from(fragment), protocol, fragment);
	}

	public Record(final SSLProtocol protocol, final TLSHandshakeBody handshakeObject) {
		this(new RecordHeader(RecordContentType.HANDSHAKE, protocol), List.of(new Handshake(handshakeObject)));
	}

	public static Record from(final InputStream is) throws IOException {
		RecordHeader header = RecordHeader.from(is);
		RecordContentType recordType = header.getType();

		List<TLSObject> fragments = new ArrayList<>();
		int currentLength = header.getLength().getValue();
		while (currentLength > 0) {
			TLSObject fragment = recordType.fragment().from(is, currentLength);
			fragments.add(fragment);
			currentLength -= fragment.sizeOf();
		}
		return new Record(header, fragments, false);
	}

	public static Record from(final InputStream is, final BiFunction<InputStream, Short, TLSObject> fragmentReader) throws IOException {
		RecordHeader header = RecordHeader.from(is);
		TLSObject fragment = fragmentReader.apply(is, header.getLength().getValue());

		return new Record(header, List.of(fragment), false);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(header.toByteArray());
		for (TLSObject fragment : fragments) {
			buffer.put(fragment.toByteArray());
		}
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return header.sizeOf() + ByteSizeable.sizeOf(fragments);
	}

	public RecordHeader getHeader() {
		return header;
	}

	public List<TLSObject> getFragments() {
		return fragments;
	}

	public Handshake getHandshake(final int index) {
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
	public Handshake getHandshake() {
		return getHandshake(0);
	}

	public <T extends TLSHandshakeBody> boolean hasHandshake(final Class<T> tlsHandshakeClass) {
		for (TLSObject fragment : fragments) {
			if (Handshake.class.isAssignableFrom(fragment.getClass())) {
				Handshake handshake = JavaObjects.cast(fragment);
				if (handshake.is(tlsHandshakeClass)) {
					return true;
				}
			}
		}
		return false;
	}

	public <T extends TLSHandshakeBody> T getHandshake(final Class<T> tlsHandshakeClass) {
		for (TLSObject fragment : fragments) {
			if (Handshake.class.isAssignableFrom(fragment.getClass())) {
				Handshake handshake = JavaObjects.cast(fragment);
				if (handshake.is(tlsHandshakeClass)) {
					return handshake.get(tlsHandshakeClass);
				}
			}
		}
		throw new IllegalArgumentException("No handhsake of type " + tlsHandshakeClass + " found in record");
	}
}

package org.apiphany.security.ssl.client;

import java.nio.ByteBuffer;

import org.apiphany.io.UInt16;
import org.apiphany.io.UInt64;
import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;
import org.apiphany.security.tls.TLSObject;

public class AdditionalAuthenticatedData implements TLSObject {

	private final UInt64 sequenceNumber;

	private final RecordContentType type;

	private final Version protocolVersion;

	private final UInt16 length;

	public AdditionalAuthenticatedData(final UInt64 sequenceNumber, final RecordContentType type, final Version protocolVersion, final UInt16 length) {
		this.sequenceNumber = sequenceNumber;
		this.type = type;
		this.protocolVersion = protocolVersion;
		this.length = length;
	}

	public AdditionalAuthenticatedData(final long sequenceNumber, final RecordContentType type, final SSLProtocol protocol, final short length) {
		this(UInt64.of(sequenceNumber), type, new Version(protocol), UInt16.of(length));
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(sequenceNumber.toByteArray());
		buffer.put(type.toByteArray());
		buffer.put(protocolVersion.toByteArray());
		buffer.put(length.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return sequenceNumber.sizeOf()
				+ type.sizeOf()
				+ protocolVersion.sizeOf()
				+ length.sizeOf();
	}

	public UInt64 getSequenceNumber() {
		return sequenceNumber;
	}

	public RecordContentType getType() {
		return type;
	}

	public Version getProtocolVersion() {
		return protocolVersion;
	}

	public UInt16 getLength() {
		return length;
	}
}

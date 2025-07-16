package org.apiphany.security.ssl.client;

import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;

public class AdditionalAuthenticatedData implements TLSObject {

	private Int64 sequenceNumber;

	private RecordContentType type;

	private Version protocolVersion;

	private Int16 length;

	public AdditionalAuthenticatedData(final Int64 sequenceNumber, final RecordContentType type, final Version protocolVersion, final Int16 length) {
		this.sequenceNumber = sequenceNumber;
		this.type = type;
		this.protocolVersion = protocolVersion;
		this.length = length;
	}

	public AdditionalAuthenticatedData(final long sequenceNumber, final RecordContentType type, final SSLProtocol protocol, final short length) {
		this(new Int64(sequenceNumber), type, new Version(protocol), new Int16(length));
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

	public Int64 getSequenceNumber() {
		return sequenceNumber;
	}

	public RecordContentType getType() {
		return type;
	}

	public Version getProtocolVersion() {
		return protocolVersion;
	}

	public Int16 getLength() {
		return length;
	}
}

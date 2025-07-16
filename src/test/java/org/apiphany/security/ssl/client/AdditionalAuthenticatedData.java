package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;
import org.morphix.lang.function.ThrowingRunnable;

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
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(sequenceNumber.toByteArray());
			dos.write(type.value());
			dos.write(protocolVersion.toByteArray());
			dos.write(length.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return sequenceNumber.size()
				+ type.size()
				+ protocolVersion.size()
				+ length.size();
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

package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;

public class StatusRequest implements TLSExtension {

	private final ExtensionType type;

	private final Int16 length;

	private final Int8 certificateStatusType; // OCSP

	private final Int16 responderIDInfoSize;

	private final Int16 requestExtensionInfoSize;

	public StatusRequest(
			final ExtensionType type,
			final Int16 length,
			final Int8 certificateStatusType,
			final Int16 responderIDInfoSize,
			final Int16 requestExtensionInfoSize) {
		this.type = type;
		this.length = length;
		this.certificateStatusType = certificateStatusType;
		this.responderIDInfoSize = responderIDInfoSize;
		this.requestExtensionInfoSize = requestExtensionInfoSize;
	}

	public StatusRequest() {
		this(ExtensionType.STATUS_REQUEST, new Int16((short) 0x0005), new Int8((byte) 0x01), new Int16((short) 0x0000), new Int16((short) 0x0000));
	}

	public static StatusRequest from(final InputStream is) throws IOException {
		Int16 int16 = Int16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	public static StatusRequest from(final InputStream is, final ExtensionType type) throws IOException {
		Int16 length = Int16.from(is);
		Int8 certificateStatusType = Int8.from(is);
		Int16 responderIDInfoSize = Int16.from(is);
		Int16 requestExtensionInfoSize = Int16.from(is);

		return new StatusRequest(type, length, certificateStatusType, responderIDInfoSize, requestExtensionInfoSize);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(length.toByteArray());
		buffer.put(certificateStatusType.toByteArray());
		buffer.put(responderIDInfoSize.toByteArray());
		buffer.put(requestExtensionInfoSize.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return type.sizeOf()
				+ length.sizeOf()
				+ certificateStatusType.sizeOf()
				+ responderIDInfoSize.sizeOf()
				+ requestExtensionInfoSize.sizeOf();
	}

	@Override
	public ExtensionType getType() {
		return type;
	}

	public Int16 getLength() {
		return length;
	}

	public Int8 getCertificateStatusType() {
		return certificateStatusType;
	}

	public Int16 getResponderIDInfoSize() {
		return responderIDInfoSize;
	}

	public Int16 getRequestExtensionInfoSize() {
		return requestExtensionInfoSize;
	}
}

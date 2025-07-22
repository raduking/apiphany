package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.UInt16;
import org.apiphany.io.UInt8;
import org.apiphany.json.JsonBuilder;

public class StatusRequest implements TLSExtension {

	private final ExtensionType type;

	private final UInt16 length;

	private final UInt8 certificateStatusType; // OCSP

	private final UInt16 responderIDInfoSize;

	private final UInt16 requestExtensionInfoSize;

	public StatusRequest(
			final ExtensionType type,
			final UInt16 length,
			final UInt8 certificateStatusType,
			final UInt16 responderIDInfoSize,
			final UInt16 requestExtensionInfoSize) {
		this.type = type;
		this.length = length;
		this.certificateStatusType = certificateStatusType;
		this.responderIDInfoSize = responderIDInfoSize;
		this.requestExtensionInfoSize = requestExtensionInfoSize;
	}

	public StatusRequest() {
		this(ExtensionType.STATUS_REQUEST, UInt16.of((short) 0x0005), UInt8.of((byte) 0x01), UInt16.ZERO, UInt16.ZERO);
	}

	public static StatusRequest from(final InputStream is) throws IOException {
		UInt16 int16 = UInt16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	public static StatusRequest from(final InputStream is, final ExtensionType type) throws IOException {
		UInt16 length = UInt16.from(is);
		UInt8 certificateStatusType = UInt8.from(is);
		UInt16 responderIDInfoSize = UInt16.from(is);
		UInt16 requestExtensionInfoSize = UInt16.from(is);

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

	public UInt16 getLength() {
		return length;
	}

	public UInt8 getCertificateStatusType() {
		return certificateStatusType;
	}

	public UInt16 getResponderIDInfoSize() {
		return responderIDInfoSize;
	}

	public UInt16 getRequestExtensionInfoSize() {
		return requestExtensionInfoSize;
	}
}

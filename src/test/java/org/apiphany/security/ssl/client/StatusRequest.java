package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class StatusRequest implements Extension {

	private ExtensionType type;

	private Int16 length;

	private Int8 certificateStatusType; // OCSP

	private Int16 responderIDInfoSize;

	private Int16 requestExtensionInfoSize;

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
		Int16 value = Int16.from(is);
		ExtensionType type = ExtensionType.fromValue(value.getValue());

		return from(is, type);
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
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.writeShort(type.value());
			dos.write(length.toByteArray());
			dos.write(certificateStatusType.toByteArray());
			dos.write(responderIDInfoSize.toByteArray());
			dos.write(requestExtensionInfoSize.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return type.size()
				+ length.size()
				+ certificateStatusType.size()
				+ responderIDInfoSize.size()
				+ requestExtensionInfoSize.size();
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

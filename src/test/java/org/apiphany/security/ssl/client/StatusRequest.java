package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StatusRequest implements Sizeable {

	private ExtensionType type = ExtensionType.STATUS_REQUEST;

	private Int16 size = new Int16((short) 0x0005);

	private Int8 certificateStatusType = new Int8((byte) 0x01); // OCSP

	private Int16 responderIDInfoSize = new Int16((short) 0x0000);

	private Int16 requestExtensionInfoSize = new Int16((short) 0x0000);

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.writeShort(type.value());
		dos.write(size.toByteArray());
		dos.write(certificateStatusType.toByteArray());
		dos.write(responderIDInfoSize.toByteArray());
		dos.write(requestExtensionInfoSize.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public int size() {
		return type.size()
				+ size.size()
				+ certificateStatusType.size()
				+ responderIDInfoSize.size()
				+ requestExtensionInfoSize.size();
	}

	public ExtensionType getType() {
		return type;
	}

	public Int16 getSize() {
		return size;
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

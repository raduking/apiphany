package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class Extensions {

	public static class StatusRequest {

		ExtensionType type = ExtensionType.STATUS_REQUEST;

		short size = 0x0005;

		byte certificateStatusType = 0x01; // OCSP

		short responderIDInfoSize = 0x0000;

		short requestExtensionInfoSize = 0x0000;

		public byte[] toByteArray() throws IOException {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);

			dos.writeShort(type.value());
			dos.writeShort(size);
			dos.writeByte(certificateStatusType);
			dos.writeShort(responderIDInfoSize);
			dos.writeShort(requestExtensionInfoSize);

			return bos.toByteArray();
		}
	}

	public static class SupportedGroups {

		public static final short[] GROUPS = {
				(short) 0x001D, // x25519
				(short) 0x0017, // secp256r1
				(short) 0x0018, // secp384r1
				(short) 0x0019  // secp521r1
		};

		ExtensionType type = ExtensionType.SUPPORTED_GROUPS;

		short size;

		short listSize;

		public SupportedGroups() {
			this.listSize = (short) (GROUPS.length * 2);
			this.size = (short) (listSize + 2); // 2 is sizeof(listSize)
		}

		public byte[] toByteArray() throws IOException {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);

			dos.writeShort(type.value());
			dos.writeShort(size);
			dos.writeShort(listSize);
			for (short group : GROUPS) {
				dos.writeShort(group);
			}

			return bos.toByteArray();
		}
	}

	public static class ECPointFormats {

		ExtensionType type = ExtensionType.EC_POINTS_FORMAT;

		short size = 0x0002;

		byte listSize = 0x01;

		byte format = 0x00;

		public byte[] toByteArray() throws IOException {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);

			dos.writeShort(type.value());
			dos.writeShort(size);
			dos.writeByte(listSize);
			dos.writeByte(format);

			return bos.toByteArray();
		}
	}

	public static class SignatureAlgorithms {

		public static final short[] ALGORITHMS = {
				(short) 0x0401, // RSA/PKCS1/SHA256
				(short) 0x0403, // ECDSA/SECP256r1/SHA256
				(short) 0x0501, // RSA/PKCS1/SHA384
				(short) 0x0503, // ECDSA/SECP384r1/SHA384
				(short) 0x0601, // RSA/PKCS1/SHA512
				(short) 0x0603, // ECDSA/SECP521r1/SHA512
				(short) 0x0201, // RSA/PKCS1/SHA1
				(short) 0x0203  // ECDSA/SHA1
		};

		ExtensionType type = ExtensionType.SIGNATURE_ALGORITHMS;

		short size;

		short listSize;

		public SignatureAlgorithms() {
			this.listSize = (short) (ALGORITHMS.length * 2);
			this.size = (short) (listSize + 2); // 2 is sizeof(listSize)
		}

		public byte[] toByteArray() throws IOException {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);

			dos.writeShort(type.value());
			dos.writeShort(size);
			dos.writeShort(listSize);
			for (short group : ALGORITHMS) {
				dos.writeShort(group);
			}

			return bos.toByteArray();
		}
	}

	public static class SignedCertificateTimestamp {

		ExtensionType type = ExtensionType.SCT;

		short size = 0x0000;

		public byte[] toByteArray() throws IOException {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);

			dos.writeShort(type.value());
			dos.writeShort(size);

			return bos.toByteArray();
		}
	}

	private static final int EXTENSIONS_SIZE_INDEX = 0;

	Int16 length = new Int16();

	ServerNames serverName;

	StatusRequest statusRequest = new StatusRequest();

	SupportedGroups supportedGroups = new SupportedGroups();

	ECPointFormats ecPointFormats = new ECPointFormats();

	SignatureAlgorithms signatureAlgorithms = new SignatureAlgorithms();

	RenegotiationInfo renegotiationInfo = new RenegotiationInfo();

	SignedCertificateTimestamp sct = new SignedCertificateTimestamp();

	public Extensions(final List<String> serverNames) {
		this.serverName = new ServerNames(serverNames);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		// Extensions Length
		dos.write(length.toByteArray());

		// Server Name
		dos.write(serverName.toByteArray());

		// Status Request
		dos.write(statusRequest.toByteArray());

		// Supported Groups
		dos.write(supportedGroups.toByteArray());

		// EC Point Formats
		dos.write(ecPointFormats.toByteArray());

		// Signature Algorithms
		dos.write(signatureAlgorithms.toByteArray());

		// Re-negotiation Info
		dos.write(renegotiationInfo.toByteArray());

		// SCT
		dos.write(sct.toByteArray());

		byte[] bytes = bos.toByteArray();

		// write actual size
		short extensionsSize = (short) (bytes.length - 2); // 2 is sizeof(length)
		bytes[EXTENSIONS_SIZE_INDEX] = (byte) ((extensionsSize >> 8) & 0xFF);
		bytes[EXTENSIONS_SIZE_INDEX + 1] = (byte) (extensionsSize & 0xFF);

		return bytes;
	}
}

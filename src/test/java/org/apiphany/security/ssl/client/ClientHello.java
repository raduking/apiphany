package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.security.ssl.SSLProtocol;

/**
 * Minimal Client Hello builder.
 *
 * @author Radu Sebastian LAZIN
 */
public class ClientHello {

	public static class CypherSuites {

		static final short[] CYPHER_SUITES = {
				(short) 0xCCA8, // TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256
				(short) 0xCCA9, // TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256
				(short) 0xC02F, // TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
				(short) 0xC030, // TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
				(short) 0xC02B, // TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
				(short) 0xC02C, // TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384
				(short) 0xC013, // TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA
				(short) 0xC009, // TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA
				(short) 0xC014, // TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA
				(short) 0xC00A, // TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA
				(short) 0x009C, // TLS_RSA_WITH_AES_128_GCM_SHA256
				(short) 0x009D, // TLS_RSA_WITH_AES_256_GCM_SHA384
				(short) 0x002F, // TLS_RSA_WITH_AES_128_CBC_SHA
				(short) 0x0035, // TLS_RSA_WITH_AES_256_CBC_SHA
				(short) 0xC012, // TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA
				(short) 0x000A  // TLS_RSA_WITH_3DES_EDE_CBC_SHA
		};

		short size = (short) (CYPHER_SUITES.length * 2);

		short[] cypherSuites = CYPHER_SUITES;

		public byte[] toByteArray() throws IOException {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);

			dos.writeShort(size);
			for (short cypherSuite : cypherSuites) {
				dos.writeShort(cypherSuite);
			}

			return bos.toByteArray();
		}
	}

	public static class Extensions {

		public static class ServerName {

			public static class ServerNameEntry {

				short size;

				byte type = 0x00; // DNS hostname

				short nameSize;

				byte[] name;

				ServerNameEntry(String name) {
					this.name = name.getBytes(StandardCharsets.US_ASCII);
					this.nameSize = (short) name.length();
					this.size = (short) (nameSize + 3); // 3 is sizeof(nameSize) + sizeof(type) in bytes
				}

				public byte[] bytes() throws IOException {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(bos);

					dos.writeShort(size);
					dos.writeByte(type);
					dos.writeShort(nameSize);
					dos.write(name);

					return bos.toByteArray();
				}
			}

			private static final int ENTRIES_SIZE_INDEX = 2;

			ExtensionType type = ExtensionType.SERVER_NAME;

			short size = 0x0000;

			List<ServerNameEntry> entries = new ArrayList<>();

			public ServerName(String... names) {
				for (String name : names) {
					entries.add(new ServerNameEntry(name));
				}
			}

			public byte[] toByteArray() throws IOException {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);

				dos.writeShort(type.value());
				dos.writeShort(size);

				short entriesSize = 0;
				for (ServerNameEntry entry : entries) {
					byte[] entryBytes = entry.bytes();
					dos.write(entryBytes);
					entriesSize += entryBytes.length;
				}

				byte[] bytes = bos.toByteArray();

				// write actual size
				bytes[ENTRIES_SIZE_INDEX] = (byte) ((entriesSize >> 8) & 0xFF);
				bytes[ENTRIES_SIZE_INDEX + 1] = (byte) (entriesSize & 0xFF);

				return bytes;
			}
		}

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

		ServerName serverName;

		StatusRequest statusRequest = new StatusRequest();

		SupportedGroups supportedGroups = new SupportedGroups();

		ECPointFormats ecPointFormats = new ECPointFormats();

		SignatureAlgorithms signatureAlgorithms = new SignatureAlgorithms();

		RenegotiationInfo renegotiationInfo = new RenegotiationInfo();

		SignedCertificateTimestamp sct = new SignedCertificateTimestamp();

		public Extensions(String... serverNames) {
			this.serverName = new ServerName(serverNames);
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

	RecordHeader recordHeader = new RecordHeader(RecordHeaderType.HANDSHAKE_RECORD, SSLProtocol.TLS_1_0);

	HandshakeHeader handshakeHeader = new HandshakeHeader(HandshakeMessageType.CLIENT_HELLO);

	Version clientVersion = new Version(SSLProtocol.TLS_1_2);

	HandshakeRandom clientRandom = new HandshakeRandom();

	SessionId sessionId = new SessionId();

	CypherSuites cypherSuites = new CypherSuites();

	CompressionMethods compressionMethods = new CompressionMethods();

	Extensions extensions;

	public ClientHello(String... serverNames) {
		extensions = new Extensions(serverNames);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		// Record Header
		dos.write(recordHeader.toByteArray());

		// Handshake Header
		dos.write(handshakeHeader.toByteArray());

		// Client Version
		dos.write(clientVersion.toByteArray());

		// Client Random
		dos.write(clientRandom.toByteArray());

		// Session ID
		dos.write(sessionId.toByteArray());

		// Cipher Suites
		dos.write(cypherSuites.toByteArray());

		// Compression Methods
		dos.write(compressionMethods.toByteArray());

		// Extensions Length
		dos.write(extensions.toByteArray());

		byte[] bytes = bos.toByteArray();

		// bytes after record header
		short bytesAfterRecordHeader = (short) (bytes.length - RecordHeader.SIZE);
		bytes[3] = (byte) ((bytesAfterRecordHeader >> 8) & 0xFF);
		bytes[4] = (byte) (bytesAfterRecordHeader & 0xFF);

		// bytes after handshake header
		short bytesAfterHandshakeHeader = (short) (bytesAfterRecordHeader - HandshakeHeader.SIZE);
		bytes[7] = (byte) ((bytesAfterHandshakeHeader >> 8) & 0xFF);
		bytes[8] = (byte) (bytesAfterHandshakeHeader & 0xFF);

		return bytes;
	}
}

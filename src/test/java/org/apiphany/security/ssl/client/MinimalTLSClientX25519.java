package org.apiphany.security.ssl.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MinimalTLSClientX25519 {

	public byte[] performHandshake(final String host, final int port) throws Exception {
		Socket socket = new Socket(host, port);
		InputStream in = socket.getInputStream();
		OutputStream out = socket.getOutputStream();

		SecureRandom rng = SecureRandom.getInstanceStrong();
		byte[] clientRandom = new byte[32];
		rng.nextBytes(clientRandom);

		KeyPairGenerator kpg = KeyPairGenerator.getInstance("X25519");
		kpg.initialize(new NamedParameterSpec("X25519"));
		KeyPair clientKp = kpg.generateKeyPair();

		ByteArrayOutputStream handshakeTranscript = new ByteArrayOutputStream();
		// byte[] clientHello = buildClientHello(clientRandom);
		byte[] clientHello = buildClientHello(host);
		handshakeTranscript.write(clientHello, 5, clientHello.length - 5);
		out.write(clientHello);

		List<HandshakeMessage> messages = new ArrayList<>();
		while (messages.size() < 3) {
			TLSRecord record = TLSRecord.read(in);
			if (record.contentType == 22) {
				messages.addAll(HandshakeMessage.parseMessages(record.fragment));
			}
		}

		byte[] serverRandom = parseServerRandom(messages.get(0).body);
		handshakeTranscript.write(messages.get(0).body);
		handshakeTranscript.write(messages.get(1).body);
		handshakeTranscript.write(messages.get(2).body);

		byte[] serverPubKeyBytes = parseX25519PublicKeyFromServerKeyExchange(messages.get(2).body);
		PublicKey serverPubKey = loadX25519PublicKey(serverPubKeyBytes);

		ByteArrayOutputStream ckx = new ByteArrayOutputStream();
		ckx.write(0x10); // Handshake Type: ClientKeyExchange
		byte[] clientPub = clientKp.getPublic().getEncoded();

		ckx.write(0x00); // 3-byte length follows
		ckx.write(0x00);
		ckx.write(0x21); // Length = 33: 1 byte for length, 32 for key

		ckx.write(0x20); // Public key length (32)
		ckx.write(clientPub, clientPub.length - 32, 32); // Last 32 bytes are actual X25519 key

		byte[] ckxMessage = ckx.toByteArray();
		handshakeTranscript.write(Arrays.copyOfRange(ckxMessage, 4, ckxMessage.length)); // skip handshake header for transcript

		ByteArrayOutputStream ckxRecord = new ByteArrayOutputStream();
		ckxRecord.write(0x16); // Handshake ContentType
		ckxRecord.write(0x03); // TLS 1.2
		ckxRecord.write(0x03);
		TLSUtils.writeUint16(ckxRecord, ckxMessage.length);
		ckxRecord.write(ckxMessage);

		out.write(ckxRecord.toByteArray());
		byte[] preMasterSecret = computeSharedSecret(clientKp.getPrivate(), serverPubKey);
		byte[] masterSecret = tlsPrf(preMasterSecret, "master secret", concat(clientRandom, serverRandom), 48);

		byte[] keyBlock = tlsPrf(masterSecret, "key expansion", concat(serverRandom, clientRandom), 40);
		byte[] clientWriteKey = Arrays.copyOfRange(keyBlock, 0, 16);
		byte[] serverWriteKey = Arrays.copyOfRange(keyBlock, 16, 32);
		byte[] clientIV = Arrays.copyOfRange(keyBlock, 32, 36);
		byte[] serverIV = Arrays.copyOfRange(keyBlock, 36, 40);

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] handshakeHash = md.digest(handshakeTranscript.toByteArray());
		byte[] verifyData = tlsPrf(masterSecret, "client finished", handshakeHash, 12);

		ByteArrayOutputStream finished = new ByteArrayOutputStream();
		finished.write(0x14);
		TLSUtils.writeUint24(finished, 12);
		finished.write(verifyData);
		byte[] plaintext = finished.toByteArray();

		ByteArrayOutputStream record = new ByteArrayOutputStream();
		record.write(0x16);
		record.write(0x03);
		record.write(0x03);
		long seq = 0;
		byte[] explicitNonce = ByteBuffer.allocate(8).putLong(seq).array();
		byte[] nonce = concat(clientIV, explicitNonce);

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec spec = new GCMParameterSpec(128, nonce);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(clientWriteKey, "AES"), spec);

		ByteArrayOutputStream aad = new ByteArrayOutputStream();
		aad.write(0x16);
		aad.write(0x03);
		aad.write(0x03);
		TLSUtils.writeUint16(aad, plaintext.length + 16 + 8);
		cipher.updateAAD(aad.toByteArray());
		byte[] ciphertext = cipher.doFinal(plaintext);

		record.write(aad.toByteArray());
		record.write(explicitNonce);
		record.write(ciphertext);

		// change cipher spec
		out.write(new byte[] {
				0x14,
				0x03, 0x03,
				0x00, 0x01,
				0x01
		});

		out.write(record.toByteArray());
		out.flush();

		System.out.println("Sent Client Finished. Waiting for Server Finished...");
		socket.setSoTimeout(2000);

		TLSRecord serverFinishedRecord = TLSRecord.read(in);
		ByteBuffer buf = ByteBuffer.wrap(serverFinishedRecord.fragment);
		byte[] recvNonce = new byte[8];
		buf.get(recvNonce);
		byte[] recvCiphertext = new byte[buf.remaining()];
		buf.get(recvCiphertext);

		Cipher dec = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec recvSpec = new GCMParameterSpec(128, concat(serverIV, recvNonce));
		dec.init(Cipher.DECRYPT_MODE, new SecretKeySpec(serverWriteKey, "AES"), recvSpec);
		dec.updateAAD(serverFinishedRecord.header());
		byte[] decrypted = dec.doFinal(recvCiphertext);

		byte[] expected = tlsPrf(masterSecret, "server finished", handshakeHash, 12);
		if (!Arrays.equals(expected, Arrays.copyOfRange(decrypted, 4, 16))) {
			throw new SecurityException("Server Finished verify_data mismatch");
		}

		System.out.println("TLS handshake complete");
		return new byte[] {};
	}

	public static byte[] buildClientHello(final byte[] clientRandom) throws IOException {
		ByteArrayOutputStream hello = new ByteArrayOutputStream();
		hello.write(0x03);
		hello.write(0x03);
		hello.write(clientRandom);
		hello.write(0);
		TLSUtils.writeUint16(hello, 2);
		TLSUtils.writeUint16(hello, 0xC030);
		hello.write(1);
		hello.write(0);

		ByteArrayOutputStream ext = new ByteArrayOutputStream();

		TLSUtils.writeUint16(ext, 0x000A);
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		TLSUtils.writeUint16(body, 2);
		TLSUtils.writeUint16(body, 0x001D);
		byte[] groupList = body.toByteArray();
		TLSUtils.writeUint16(ext, groupList.length);
		ext.write(groupList);

		TLSUtils.writeUint16(ext, 0x000D);
		body.reset();
		TLSUtils.writeUint16(body, 2);
		TLSUtils.writeUint16(body, 0x0403);
		byte[] sigAlgs = body.toByteArray();
		TLSUtils.writeUint16(ext, sigAlgs.length);
		ext.write(sigAlgs);

		byte[] extBytes = ext.toByteArray();
		TLSUtils.writeUint16(hello, extBytes.length);
		hello.write(extBytes);

		byte[] handshake = hello.toByteArray();
		ByteArrayOutputStream record = new ByteArrayOutputStream();
		record.write(0x16);
		record.write(0x03);
		record.write(0x03);
		TLSUtils.writeUint16(record, handshake.length + 4);
		record.write(0x01);
		TLSUtils.writeUint24(record, handshake.length);
		record.write(handshake);

		return record.toByteArray();
	}

	public static byte[] buildClientHello(final String host) {
		return new TLSRecordClientHello(List.of(host), new CipherSuites(CipherSuiteName.values()), List.of(CurveName.values())).toByteArray();
	}

	public static byte[] parseServerRandom(final byte[] serverHello) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(serverHello);
		in.read();
		in.read();
		byte[] serverRandom = in.readNBytes(32);
		int sessionIdLen = in.read();
		in.skip(sessionIdLen);
		in.read();
		in.read();
		in.read();
		return serverRandom;
	}

	public static byte[] parseX25519PublicKeyFromServerKeyExchange(final byte[] skx) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(skx);
		if (in.read() != 3)
			throw new IOException("Bad curve type");
		int namedCurve = ((in.read() & 0xFF) << 8) | (in.read() & 0xFF);
		if (namedCurve != 0x001D)
			throw new IOException("Not X25519");
		if (in.read() != 32)
			throw new IOException("Bad key length");
		return in.readNBytes(32);
	}

	public static PublicKey loadX25519PublicKey(final byte[] keyBytes) throws Exception {
		KeyFactory kf = KeyFactory.getInstance("X25519");
		return kf.generatePublic(new XECPublicKeySpec(new NamedParameterSpec("X25519"), new BigInteger(1, keyBytes)));
	}

	public static byte[] computeSharedSecret(final PrivateKey privateKey, final PublicKey serverPub) throws Exception {
		KeyAgreement ka = KeyAgreement.getInstance("X25519");
		ka.init(privateKey);
		ka.doPhase(serverPub, true);
		return ka.generateSecret();
	}

	public static byte[] tlsPrf(final byte[] secret, final String label, final byte[] seed, final int length) throws Exception {
		return pHash(secret, concat(label.getBytes(StandardCharsets.US_ASCII), seed), length);
	}

	public static byte[] pHash(final byte[] secret, final byte[] seed, final int length) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(secret, "HmacSHA256"));
		byte[] result = new byte[length];
		byte[] a = mac.doFinal(seed);
		int pos = 0;
		while (pos < length) {
			byte[] output = mac.doFinal(concat(a, seed));
			int copy = Math.min(output.length, length - pos);
			System.arraycopy(output, 0, result, pos, copy);
			pos += copy;
			a = mac.doFinal(a);
		}
		return result;
	}

	public static byte[] concat(final byte[]... arrays) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (byte[] arr : arrays) {
			try {
				out.write(arr);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		return out.toByteArray();
	}

	static class TLSUtils {
		public static void writeUint16(final OutputStream out, final int val) throws IOException {
			out.write((val >> 8) & 0xFF);
			out.write(val & 0xFF);
		}

		public static void writeUint24(final OutputStream out, final int val) throws IOException {
			out.write((val >> 16) & 0xFF);
			out.write((val >> 8) & 0xFF);
			out.write(val & 0xFF);
		}
	}

	static class TLSRecord {

		public int contentType;
		public int major;
		public int minor;
		public byte[] fragment;

		public static TLSRecord read(final InputStream in) throws IOException {
			byte[] header = in.readNBytes(5);
			if (header.length < 5) {
				throw new EOFException("Unexpected end of stream while reading TLS record header");
			}

			int type = header[0] & 0xFF;
			int len = ((header[3] & 0xFF) << 8) | (header[4] & 0xFF);
			byte[] fragment = in.readNBytes(len);

			if (fragment.length < len) {
				throw new EOFException("Unexpected end of stream while reading TLS record fragment");
			}

			TLSRecord rec = new TLSRecord();
			rec.contentType = type;
			rec.major = header[1];
			rec.minor = header[2];
			rec.fragment = fragment;
			System.out.println("Received TLS record of type: " + type + " length=" + len);
			return rec;
		}

		public byte[] header() {
			return new byte[] {
					(byte) contentType, (byte) major, (byte) minor,
					(byte) ((fragment.length >> 8) & 0xFF), (byte) (fragment.length & 0xFF)
			};
		}
	}

	static class HandshakeMessage {

		public int type;
		public byte[] body;

		public static List<HandshakeMessage> parseMessages(final byte[] data) throws IOException {
			List<HandshakeMessage> list = new ArrayList<>();
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			while (in.available() > 0) {
				int type = in.read();
				int len = (in.read() << 16) | (in.read() << 8) | in.read();
				byte[] body = in.readNBytes(len);
				HandshakeMessage msg = new HandshakeMessage();
				msg.type = type;
				msg.body = body;
				list.add(msg);
			}
			return list;
		}
	}

}

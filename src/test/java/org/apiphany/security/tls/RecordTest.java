package org.apiphany.security.tls;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apiphany.security.ssl.SSLProtocol;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Record}.
 *
 * @author Radu Sebastian LAZIN
 */
class RecordTest {

	private static final Version TLS_VERSION = new Version(SSLProtocol.TLS_1_2);

	@Test
	void shouldReadFragmentedTLSRecord() throws IOException {
		@SuppressWarnings("resource")
		InputStream is = createFragmentedCertificateStream();

		Record tlsRecord = Record.from(is);

		assertThat(tlsRecord, notNullValue());
	}

	public static InputStream createFragmentedCertificateStream() {
		// Fake certificate bytes (use any bytes; in a real test you may want real DER)
		byte[] certBytes = new byte[500];
		Arrays.fill(certBytes, (byte) 0xAB);

		// Per-certificate length fields
		int perCertLen = certBytes.length;
		// certificate_list_length is sum of (3 + certLen) for each cert; for 1 cert: 3 + perCertLen
		int certListLen = 3 + perCertLen;
		// handshake body length = 3 (certificate_list_length) + sum(each cert entry (3 + certLen))
		int handshakeBodyLen = 3 + (3 + perCertLen); // for single cert: 6 + perCertLen

		// Build handshake header: type (1) + length (3)
		byte[] handshakeHeader = new byte[4];
		handshakeHeader[0] = HandshakeType.CERTIFICATE.value();
		handshakeHeader[1] = (byte) ((handshakeBodyLen >> 16) & 0xFF);
		handshakeHeader[2] = (byte) ((handshakeBodyLen >> 8) & 0xFF);
		handshakeHeader[3] = (byte) (handshakeBodyLen & 0xFF);

		// certificate_list_length (3 bytes, big-endian)
		byte[] certListLenBytes = new byte[3];
		certListLenBytes[0] = (byte) ((certListLen >> 16) & 0xFF);
		certListLenBytes[1] = (byte) ((certListLen >> 8) & 0xFF);
		certListLenBytes[2] = (byte) (certListLen & 0xFF);

		// per-certificate length (3 bytes, big-endian)
		byte[] certLenBytes = new byte[3];
		certLenBytes[0] = (byte) ((perCertLen >> 16) & 0xFF);
		certLenBytes[1] = (byte) ((perCertLen >> 8) & 0xFF);
		certLenBytes[2] = (byte) (perCertLen & 0xFF);

		// Assemble full handshake message (header + body)
		byte[] fullHandshake = new byte[handshakeHeader.length + certListLenBytes.length + certLenBytes.length + certBytes.length];
		int pos = 0;
		System.arraycopy(handshakeHeader, 0, fullHandshake, pos, handshakeHeader.length);
		pos += handshakeHeader.length;
		System.arraycopy(certListLenBytes, 0, fullHandshake, pos, certListLenBytes.length);
		pos += certListLenBytes.length;
		System.arraycopy(certLenBytes, 0, fullHandshake, pos, certLenBytes.length);
		pos += certLenBytes.length;
		System.arraycopy(certBytes, 0, fullHandshake, pos, certBytes.length);

		// Split the fullHandshake into two fragments (so it spans two records)
		// Choose splitPoint anywhere between 1 and fullHandshake.length-1. Here choose 300.
		int splitPoint = 300;
		if (splitPoint <= 0 || splitPoint >= fullHandshake.length) {
			splitPoint = fullHandshake.length / 2;
		}
		byte[] fragment1 = Arrays.copyOfRange(fullHandshake, 0, splitPoint);
		byte[] fragment2 = Arrays.copyOfRange(fullHandshake, splitPoint, fullHandshake.length);

		// Build TLS record 1 (header + fragment1)
		byte[] record1 = new byte[5 + fragment1.length];
		record1[0] = RecordContentType.HANDSHAKE.value();
		System.arraycopy(TLS_VERSION.toByteArray(), 0, record1, 1, 2);
		record1[3] = (byte) ((fragment1.length >> 8) & 0xFF);
		record1[4] = (byte) (fragment1.length & 0xFF);
		System.arraycopy(fragment1, 0, record1, 5, fragment1.length);

		// Build TLS record 2 (header + fragment2)
		byte[] record2 = new byte[5 + fragment2.length];
		record2[0] = RecordContentType.HANDSHAKE.value();
		System.arraycopy(TLS_VERSION.toByteArray(), 0, record2, 1, 2);
		record2[3] = (byte) ((fragment2.length >> 8) & 0xFF);
		record2[4] = (byte) (fragment2.length & 0xFF);
		System.arraycopy(fragment2, 0, record2, 5, fragment2.length);

		// Combine into one InputStream
		byte[] combined = new byte[record1.length + record2.length];
		System.arraycopy(record1, 0, combined, 0, record1.length);
		System.arraycopy(record2, 0, combined, record1.length, record2.length);

		return new ByteArrayInputStream(combined);
	}
}

package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apiphany.io.BytesWrapper;
import org.apiphany.lang.Bytes;
import org.apiphany.security.ssl.SSLProtocol;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Record}.
 *
 * @author Radu Sebastian LAZIN
 */
class RecordTest {

	private static final int SPLIT_POINT = 300;

	private static final int CERTIFICATE_SIZE = 500;
	private static final byte CERTIFICATE_BYTE = (byte) 0xAB;
	private static final byte[] CERTIFICATE_BYTES = new byte[CERTIFICATE_SIZE];
	static {
		Arrays.fill(CERTIFICATE_BYTES, CERTIFICATE_BYTE);
	}

	@Test
	void shouldReadCertificateTLSRecord() throws IOException {
		@SuppressWarnings("resource")
		InputStream is = createCertificateStream();

		Record tlsRecord = Record.from(is);

		assertThat(tlsRecord, notNullValue());

		byte[] result = tlsRecord.getHandshake(Certificates.class).first().getData().toByteArray();
		for (int i = 0; i < CERTIFICATE_SIZE; ++i) {
			assertThat(result[i], equalTo(CERTIFICATE_BYTE));
		}
	}

	@Test
	void shouldReadFragmentedCertificateTLSRecord() throws IOException {
		@SuppressWarnings("resource")
		InputStream is = createFragmentedCertificateStream();

		Record tlsRecord = Record.from(is);

		assertThat(tlsRecord, notNullValue());

		byte[] result = tlsRecord.getHandshake(Certificates.class).first().getData().toByteArray();
		for (int i = 0; i < CERTIFICATE_SIZE; ++i) {
			assertThat(result[i], equalTo(CERTIFICATE_BYTE));
		}
	}

	public static InputStream createFragmentedCertificateStream() {
		int perCertLen = CERTIFICATE_BYTES.length;
		int certListLen = 3 + perCertLen;
		short handshakeBodyLen = (short) (3 + certListLen);

		HandshakeHeader handshakeHeader = new HandshakeHeader(HandshakeType.CERTIFICATE, handshakeBodyLen);

		Certificate certificate = new Certificate(CERTIFICATE_BYTES);
		Certificates certificates = new Certificates(List.of(certificate));

		byte[] handshakeBodyBytes = certificates.toByteArray();

		byte[] fragment1 = Arrays.copyOfRange(handshakeBodyBytes, 0, SPLIT_POINT);
		byte[] fragment2 = Arrays.copyOfRange(handshakeBodyBytes, SPLIT_POINT, handshakeBodyBytes.length);

		RawHandshakeBody handshakeBody1 = new RawHandshakeBody(HandshakeType.CERTIFICATE, new BytesWrapper(fragment1));
		RawHandshakeBody handshakeBody2 = new RawHandshakeBody(HandshakeType.CERTIFICATE, new BytesWrapper(fragment2));

		Record record1 = new Record(RecordContentType.HANDSHAKE, SSLProtocol.TLS_1_2, new Handshake(handshakeHeader, handshakeBody1, false));
		Record record2 = new Record(SSLProtocol.TLS_1_2, new Handshake(handshakeBody2));

		return new ByteArrayInputStream(Bytes.concatenate(record1.toByteArray(), record2.toByteArray()));
	}

	public static InputStream createCertificateStream() {
		Certificate certificate = new Certificate(CERTIFICATE_BYTES);
		Certificates certificates = new Certificates(List.of(certificate));

		Record tlsRecord = new Record(RecordContentType.HANDSHAKE, SSLProtocol.TLS_1_2, new Handshake(certificates));

		return new ByteArrayInputStream(tlsRecord.toByteArray());
	}
}

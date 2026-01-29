package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

	private static final int SPLIT_POINT = 30;

	private static final int CERTIFICATE_SIZE = 50;
	private static final byte CERTIFICATE_BYTE = (byte) 0xAB;
	private static final byte[] CERTIFICATE_BYTES = new byte[CERTIFICATE_SIZE];
	static {
		Arrays.fill(CERTIFICATE_BYTES, CERTIFICATE_BYTE);
	}

	@Test
	void shouldHaveTheMaxSize16kb() {
		assertThat(Record.MAX_SIZE, equalTo(16384));
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
	void shouldReturnFragmentsFromRecord() throws IOException {
		@SuppressWarnings("resource")
		InputStream is = createCertificateStream();
		Record tlsRecord = Record.from(is);

		Certificate certificate = new Certificate(CERTIFICATE_BYTES);
		Certificates certificates = new Certificates(List.of(certificate));
		Handshake handshake = new Handshake(certificates);

		List<Handshake> handshakes = tlsRecord.getFragments(Handshake.class);

		assertThat(handshakes.size(), equalTo(1));
		assertThat(handshakes.getFirst(), equalTo(handshake));
	}

	@Test
	void shouldReturnFragmentFromRecord() throws IOException {
		@SuppressWarnings("resource")
		InputStream is = createCertificateStream();
		Record tlsRecord = Record.from(is);

		Certificate certificate = new Certificate(CERTIFICATE_BYTES);
		Certificates certificates = new Certificates(List.of(certificate));
		Handshake expectedHandshake = new Handshake(certificates);

		Handshake handshake = tlsRecord.getFragment(Handshake.class);

		assertThat(handshake, equalTo(expectedHandshake));
	}

	@Test
	void shouldReturnFirstHandshakeFromRecord() throws IOException {
		@SuppressWarnings("resource")
		InputStream is = createCertificateStream();
		Record tlsRecord = Record.from(is);

		Certificate certificate = new Certificate(CERTIFICATE_BYTES);
		Certificates certificates = new Certificates(List.of(certificate));
		Handshake expectedHandshake = new Handshake(certificates);

		Handshake handshake = tlsRecord.getFirstHandshake();

		assertThat(handshake, equalTo(expectedHandshake));
	}

	@Test
	void shouldReturnTrueForExistingHandshake() throws IOException {
		@SuppressWarnings("resource")
		InputStream is = createCertificateStream();
		Record tlsRecord = Record.from(is);

		boolean hasHandshake = tlsRecord.hasHandshake(Certificates.class);

		assertTrue(hasHandshake);
	}

	@Test
	void shouldReadServerFinishedRecord() throws IOException {
		Finished finished = new Finished(new BytesWrapper(Bytes.fromHex("0102030405")));
		Record tlsRecord = new Record(RecordContentType.HANDSHAKE, SSLProtocol.TLS_1_2, finished);

		InputStream is = new ByteArrayInputStream(tlsRecord.toByteArray());

		Record recordFinished = Record.from(is, Finished::from);

		Finished receivedFinished = recordFinished.getFragment(Finished.class);

		assertThat(recordFinished, notNullValue());
		assertThat(receivedFinished, equalTo(finished));
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

	@Test
	void shouldReadHandshakeByteByByteFragmentedTLSRecord() throws IOException {
		@SuppressWarnings("resource")
		InputStream is = createFragmentedByteByByteCertificateStream();

		Record tlsRecord = Record.from(is);

		assertThat(tlsRecord, notNullValue());

		byte[] result = tlsRecord.getHandshake(Certificates.class).first().getData().toByteArray();
		for (int i = 0; i < CERTIFICATE_SIZE; ++i) {
			assertThat(result[i], equalTo(CERTIFICATE_BYTE));
		}
	}

	private static InputStream createCertificateStream() {
		Certificate certificate = new Certificate(CERTIFICATE_BYTES);
		Certificates certificates = new Certificates(List.of(certificate));

		Record tlsRecord = new Record(RecordContentType.HANDSHAKE, SSLProtocol.TLS_1_2, new Handshake(certificates));

		return new ByteArrayInputStream(tlsRecord.toByteArray());
	}

	private static InputStream createFragmentedCertificateStream() {
		int perCertLen = CERTIFICATE_BYTES.length;
		int certListLen = 3 + perCertLen;
		short handshakeBodyLen = (short) (3 + certListLen);

		HandshakeHeader handshakeHeader = new HandshakeHeader(HandshakeType.CERTIFICATE, handshakeBodyLen);

		Certificate certificate = new Certificate(CERTIFICATE_BYTES);
		Certificates certificates = new Certificates(List.of(certificate));
		byte[] handshakeBytes = new Handshake(handshakeHeader, certificates).toByteArray(); // includes 4-byte handshake header

		// split the handshake into two fragments (simulate TLS record fragmentation)
		int firstFragmentLen = SPLIT_POINT;
		byte[] fragment1 = Arrays.copyOfRange(handshakeBytes, 0, firstFragmentLen);
		byte[] fragment2 = Arrays.copyOfRange(handshakeBytes, firstFragmentLen, handshakeBytes.length);

		// build the first TLS record with HANDSHAKE type
		RecordHeader header1 = new RecordHeader(RecordContentType.HANDSHAKE, SSLProtocol.TLS_1_2, (short) fragment1.length);
		byte[] record1Bytes = Bytes.concatenate(header1.toByteArray(), fragment1);

		// build the second TLS record with HANDSHAKE type, only continuation bytes
		RecordHeader header2 = new RecordHeader(RecordContentType.HANDSHAKE, SSLProtocol.TLS_1_2, (short) fragment2.length);
		byte[] record2Bytes = Bytes.concatenate(header2.toByteArray(), fragment2);

		// combine both records into a single input stream
		return new ByteArrayInputStream(Bytes.concatenate(record1Bytes, record2Bytes));
	}

	private static InputStream createFragmentedByteByByteCertificateStream() throws IOException {
		// create full handshake bytes (including header)
		Certificate certificate = new Certificate(CERTIFICATE_BYTES);
		Certificates certificates = new Certificates(List.of(certificate));
		Handshake handshake = new Handshake(certificates);
		byte[] handshakeBytes = handshake.toByteArray(); // includes 4-byte handshake header

		// build an input stream with each byte in its own TLS record
		ByteArrayOutputStream fragmentedStream = new ByteArrayOutputStream();
		for (byte b : handshakeBytes) {
			// each TLS record contains a single byte of handshake data
			RecordHeader header = new RecordHeader(RecordContentType.HANDSHAKE, SSLProtocol.TLS_1_2, (short) 1);
			fragmentedStream.write(header.toByteArray());
			fragmentedStream.write(b);
		}
		return new ByteArrayInputStream(fragmentedStream.toByteArray());
	}
}

package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apiphany.io.BytesWrapper;
import org.apiphany.lang.Bytes;
import org.apiphany.lang.Strings;
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

		byte[] result = tlsRecord.getHandshakeBody(Certificates.class).first().getData().toByteArray();
		for (int i = 0; i < CERTIFICATE_SIZE; ++i) {
			assertThat(result[i], equalTo(CERTIFICATE_BYTE));
		}
	}

	@Test
	void shouldSerializeToString() {
		Record tlsRecord = new Record(SSLProtocol.TLS_1_2, new ApplicationData(new Encrypted(Bytes.EMPTY)));

		String result = tlsRecord.toString();

		assertNotNull(result);
		assertFalse(Strings.isBlank(result));
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
	void shouldReturnCertificateFragmentFromRecord() throws IOException {
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
	void shouldReturnFirstHandshakeFromRecord() {
		Certificate certificate = new Certificate(CERTIFICATE_BYTES);
		Certificates certificates = new Certificates(List.of(certificate));
		Record tlsRecord = new Record(SSLProtocol.TLS_1_2, certificates);
		Handshake expectedHandshake = new Handshake(certificates);

		Handshake handshake = tlsRecord.getFirstHandshake();

		assertThat(handshake, equalTo(expectedHandshake));
	}

	@Test
	void shouldCheckForExistingHandshakeBody() throws IOException {
		@SuppressWarnings("resource")
		InputStream is = createCertificateStream();
		Record tlsRecord = Record.from(is);

		boolean hasHandshake = tlsRecord.containsHandshakeBody(Certificates.class);
		boolean doedNotHaveHandshake = tlsRecord.doesNotContainHandshakeBody(Certificates.class);

		assertTrue(hasHandshake);
		assertFalse(doedNotHaveHandshake);
	}

	@Test
	void shouldCheckForNonExistingHandshakeBody() throws IOException {
		@SuppressWarnings("resource")
		InputStream is = createCertificateStream();
		Record tlsRecord = Record.from(is);

		boolean hasHandshake = tlsRecord.containsHandshakeBody(Finished.class);
		boolean doedNotHaveHandshake = tlsRecord.doesNotContainHandshakeBody(Finished.class);

		assertFalse(hasHandshake);
		assertTrue(doedNotHaveHandshake);
	}

	@Test
	void shouldCheckForNonExistingHandshakeBodyWhenRecordIsNotHandshake() {
		Record tlsRecord = new Record(SSLProtocol.TLS_1_2, new ApplicationData(new Encrypted(Bytes.EMPTY)));

		boolean doedNotHaveHandshake = tlsRecord.containsHandshakeBody(Finished.class);

		assertFalse(doedNotHaveHandshake);
	}

	@Test
	void shouldThrowExceptionIfRecordHasNoHandshakeFragmentWhenTryingToRetrieveOne() {
		Record tlsRecord = new Record(SSLProtocol.TLS_1_2, new ApplicationData(new Encrypted(Bytes.EMPTY)));

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> tlsRecord.getHandshakeBody(Certificates.class));

		assertThat(e.getMessage(), equalTo("No handshake of type " + Certificates.class + " found in record"));
	}

	@Test
	void shouldThrowExceptionIfRecordHasMultipleFragmentsOfTheRequiredTypeOnGetFragment() {
		ApplicationData fragment1 = new ApplicationData(new Encrypted(Bytes.EMPTY));
		ApplicationData fragment2 = new ApplicationData(new Encrypted(Bytes.EMPTY));
		Record tlsRecord = new Record(
				new RecordHeader(RecordContentType.APPLICATION_DATA, SSLProtocol.TLS_1_2),
				List.of(fragment1, fragment2));

		IllegalStateException e = assertThrows(IllegalStateException.class, () -> tlsRecord.getFragment(ApplicationData.class));

		assertThat(e.getMessage(), equalTo("More than one fragments of type " + ApplicationData.class + " are present in the record."));
	}

	@Test
	void shouldReturnNullIfRecordHasNoFragmentsOnGetFragment() {
		Record tlsRecord = new Record(
				new RecordHeader(RecordContentType.APPLICATION_DATA, SSLProtocol.TLS_1_2),
				List.of());

		TLSObject fragment = tlsRecord.getFragment(ApplicationData.class);

		assertNull(fragment);
	}

	@Test
	void shouldReturnNullIfRecordHasNoFragmentsOfTheRequiredTypeOnGetFragment() {
		ApplicationData fragment1 = new ApplicationData(new Encrypted(Bytes.EMPTY));
		ApplicationData fragment2 = new ApplicationData(new Encrypted(Bytes.EMPTY));
		Record tlsRecord = new Record(
				new RecordHeader(RecordContentType.APPLICATION_DATA, SSLProtocol.TLS_1_2),
				List.of(fragment1, fragment2));

		TLSObject fragment = tlsRecord.getFragment(Certificates.class);

		assertNull(fragment);
	}

	@Test
	void shouldReturnAllMatchingFragmentsOnGetFragment() {
		Finished finished = new Finished(new BytesWrapper(Bytes.fromHex("0102030405")));
		ApplicationData fragment1 = new ApplicationData(new Encrypted(Bytes.EMPTY));
		ApplicationData fragment2 = new ApplicationData(new Encrypted(Bytes.EMPTY));
		Record tlsRecord = new Record(
				new RecordHeader(RecordContentType.APPLICATION_DATA, SSLProtocol.TLS_1_2),
				List.of(finished, fragment1, fragment2));
		List<ApplicationData> expected = List.of(fragment1, fragment2);

		List<ApplicationData> fragments = tlsRecord.getFragments(ApplicationData.class);

		assertThat(fragments, hasSize(2));
		assertThat(fragments, equalTo(expected));
	}

	@Test
	void shouldReturnEmptyListIfRecordHasNoFragmentsOnGetFragments() {
		Record tlsRecord = new Record(
				new RecordHeader(RecordContentType.APPLICATION_DATA, SSLProtocol.TLS_1_2),
				List.of());

		List<ApplicationData> fragments = tlsRecord.getFragments(ApplicationData.class);

		assertThat(fragments, hasSize(0));
	}

	@Test
	void shouldReturnFinishedFragmentFromRecord() throws IOException {
		Finished finished = new Finished(new BytesWrapper(Bytes.fromHex("0102030405")));
		Record tlsRecord = new Record(RecordContentType.HANDSHAKE, SSLProtocol.TLS_1_2, finished);
		InputStream is = new ByteArrayInputStream(tlsRecord.toByteArray());
		Record recordFinished = Record.from(is, Finished::from);

		Finished receivedFinished = recordFinished.getFragment(Finished.class);

		assertThat(recordFinished, notNullValue());
		assertThat(receivedFinished, equalTo(finished));
	}

	@Test
	void shouldBuildRecordHeaderForFinished() {
		Finished finished = new Finished(new BytesWrapper(Bytes.fromHex("0102030405")));
		Record tlsRecord = new Record(RecordContentType.HANDSHAKE, SSLProtocol.TLS_1_2, finished);

		RecordHeader header = tlsRecord.getHeader();

		assertThat(header.getLength().toUnsignedInt(), equalTo(finished.sizeOf()));
		assertThat(header.getVersion().getProtocol(), equalTo(SSLProtocol.TLS_1_2));
		assertThat(header.getType(), equalTo(RecordContentType.HANDSHAKE));
	}

	@Test
	void shouldBuildRecordHeaderForAlert() {
		Alert alert = new Alert(AlertLevel.WARNING, AlertDescription.ACCESS_DENIED);
		Record tlsRecord = new Record(SSLProtocol.TLS_1_2, alert);

		RecordHeader header = tlsRecord.getHeader();

		assertThat(header.getLength().toUnsignedInt(), equalTo(alert.sizeOf()));
		assertThat(header.getVersion().getProtocol(), equalTo(SSLProtocol.TLS_1_2));
		assertThat(header.getType(), equalTo(RecordContentType.ALERT));
	}

	@Test
	void shouldReadAlertRecord() throws IOException {
		Alert alert = new Alert(AlertLevel.WARNING, AlertDescription.ACCESS_DENIED);
		Record tlsRecord = new Record(SSLProtocol.TLS_1_2, alert);

		tlsRecord = Record.from(new ByteArrayInputStream(tlsRecord.toByteArray()));

		assertThat(tlsRecord.getHeader().getLength().toUnsignedInt(), equalTo(alert.sizeOf()));
		assertThat(tlsRecord.getHeader().getVersion().getProtocol(), equalTo(SSLProtocol.TLS_1_2));
		assertThat(tlsRecord.getHeader().getType(), equalTo(RecordContentType.ALERT));
	}

	@Test
	void shouldReadFragmentedCertificateTLSRecord() throws IOException {
		@SuppressWarnings("resource")
		InputStream is = createFragmentedCertificateStream();

		Record tlsRecord = Record.from(is);

		assertThat(tlsRecord, notNullValue());

		byte[] result = tlsRecord.getHandshakeBody(Certificates.class).first().getData().toByteArray();
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

		byte[] result = tlsRecord.getHandshakeBody(Certificates.class).first().getData().toByteArray();
		for (int i = 0; i < CERTIFICATE_SIZE; ++i) {
			assertThat(result[i], equalTo(CERTIFICATE_BYTE));
		}
	}

	@Test
	void shouldReturnFragmentNames() {
		Finished fragment1 = new Finished(new BytesWrapper(Bytes.EMPTY));
		ApplicationData fragment2 = new ApplicationData(new Encrypted(Bytes.EMPTY));
		Record tlsRecord = new Record(
				new RecordHeader(RecordContentType.APPLICATION_DATA, SSLProtocol.TLS_1_2),
				List.of(fragment1, fragment2));
		String[] expectedNames = new String[] {
				Finished.class.getSimpleName(),
				ApplicationData.class.getSimpleName()
		};

		String[] names = tlsRecord.getFragmentNames();

		assertThat(names, equalTo(expectedNames));
	}

	@Test
	void shouldReturnHandshakeBodyFragmentNames() {
		Finished fragment = new Finished(new BytesWrapper(Bytes.EMPTY));
		Record tlsRecord = new Record(SSLProtocol.TLS_1_2, new Handshake(fragment));
		String[] expectedNames = new String[] {
				Finished.class.getSimpleName()
		};

		String[] names = tlsRecord.getFragmentNames();

		assertThat(names, equalTo(expectedNames));
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

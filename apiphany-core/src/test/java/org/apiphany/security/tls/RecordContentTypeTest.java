package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link RecordContentType}.
 *
 * @author Radu Sebastian LAZIN
 */
class RecordContentTypeTest {

	private static final int BYTES = 1;
	private static final byte[] DATA = new byte[] { 0x01, 0x02, 0x03, 0x04 };

	@ParameterizedTest
	@EnumSource(RecordContentType.class)
	void shouldBuildRecordContentTypeFromValue(final RecordContentType expectedType) {
		RecordContentType actualType = RecordContentType.fromValue(expectedType.value());

		assertThat(actualType, equalTo(expectedType));
		assertThat(actualType.sizeOf(), equalTo(BYTES));
	}

	@Test
	void shouldReturnTheCorrectBytesConstant() {
		assertThat(RecordContentType.BYTES, equalTo(BYTES));
	}

	@Test
	void shouldReturnTheCorrectValueForEachType() {
		assertThat(RecordContentType.CHANGE_CIPHER_SPEC.value(), equalTo((byte) 20));
		assertThat(RecordContentType.ALERT.value(), equalTo((byte) 21));
		assertThat(RecordContentType.HANDSHAKE.value(), equalTo((byte) 22));
		assertThat(RecordContentType.APPLICATION_DATA.value(), equalTo((byte) 23));
		assertThat(RecordContentType.HEARTBEAT.value(), equalTo((byte) 24));
	}

	@Test
	void shouldReturnTheCorrectTypeForEachValidTLSObject() {
		Finished finished = new Finished(DATA);
		RecordContentType finishedHandshakeType = RecordContentType.from(finished);

		Handshake handshake = new Handshake(finished);
		RecordContentType handshakeType = RecordContentType.from(handshake);

		Encrypted encrypted = new Encrypted(DATA);
		EncryptedHandshake encryptedHandshake = new EncryptedHandshake(encrypted);
		RecordContentType encryptedHandshakeType = RecordContentType.from(encryptedHandshake);

		ChangeCipherSpec changeCipherSpec = new ChangeCipherSpec();
		RecordContentType changeCipherSpecType = RecordContentType.from(changeCipherSpec);

		ApplicationData applicationData = new ApplicationData(encrypted);
		RecordContentType applicationDataType = RecordContentType.from(applicationData);

		EncryptedAlert encryptedAlert = new EncryptedAlert(encrypted);
		RecordContentType encryptedAlertType = RecordContentType.from(encryptedAlert);

		assertThat(finishedHandshakeType, equalTo(RecordContentType.HANDSHAKE));
		assertThat(handshakeType, equalTo(RecordContentType.HANDSHAKE));
		assertThat(encryptedHandshakeType, equalTo(RecordContentType.HANDSHAKE));
		assertThat(changeCipherSpecType, equalTo(RecordContentType.CHANGE_CIPHER_SPEC));
		assertThat(applicationDataType, equalTo(RecordContentType.APPLICATION_DATA));
		assertThat(encryptedAlertType, equalTo(RecordContentType.ALERT));
	}

	@Test
	void shouldThrowUnsupportedOperationExceptionForInvalidTLSObjects() {
		SessionId sessionId = new SessionId("sample-session-id");

		UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
				() -> RecordContentType.from(sessionId));

		assertThat(exception.getMessage(), equalTo("Unknown TLS object type: " + sessionId.getClass()));
	}
}

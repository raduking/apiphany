package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Alert}.
 *
 * @author Radu Sebastian LAZIN
 */
class AlertTest {

	private static final byte[] ALERT_HANDSHAKE_FAILURE_FATAL = new byte[] {
			// Alert Level: Fatal (0x02)
			0x02,
			// Alert Description: Handshake Failure (0x28)
			0x28
	};

	@Test
	void shouldCreateAlertWithTLSObjectsConstructor() {
		Alert alert = new Alert(AlertLevel.FATAL, AlertDescription.HANDSHAKE_FAILURE);

		assertArrayEquals(ALERT_HANDSHAKE_FAILURE_FATAL, alert.toByteArray());
	}

	@Test
	void shouldCreateAlertWithPrimitiveValuesConstructor() {
		Alert alert = new Alert((byte) 0x02, (byte) 0x28);

		assertArrayEquals(ALERT_HANDSHAKE_FAILURE_FATAL, alert.toByteArray());
	}

	@Test
	void shouldCreateAlertFromInputStream() throws Exception {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(ALERT_HANDSHAKE_FAILURE_FATAL);

		Alert alert = Alert.from(byteArrayInputStream);

		assertArrayEquals(ALERT_HANDSHAKE_FAILURE_FATAL, alert.toByteArray());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		Alert alert1 = new Alert(AlertLevel.FATAL, AlertDescription.HANDSHAKE_FAILURE);
		Alert alert2 = new Alert(AlertLevel.FATAL, AlertDescription.HANDSHAKE_FAILURE);

		// same reference
		assertEquals(alert1, alert1);

		// different instance, same values
		assertEquals(alert1, alert2);
		assertEquals(alert2, alert1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(alert1.hashCode(), alert2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		Alert alert1 = new Alert(AlertLevel.FATAL, AlertDescription.HANDSHAKE_FAILURE);
		Alert alert2 = new Alert(AlertLevel.WARNING, AlertDescription.CLOSE_NOTIFY);

		assertNotEquals(alert1, alert2);
		assertNotEquals(alert2, alert1);

		assertNotEquals(alert1, null);
		assertNotEquals(alert2, "some string");
	}

	@Test
	void shouldBuildHashCodeWithAllAttributes() {
		Alert alert = new Alert(AlertLevel.WARNING, AlertDescription.CLOSE_NOTIFY);

		int expectedHashCode = Objects.hash(alert.getLevel(), alert.getDescription());

		assertEquals(expectedHashCode, alert.hashCode());
	}

	@Test
	void shouldReturnDisplayDescription() {
		Alert alert = new Alert(AlertLevel.FATAL, AlertDescription.HANDSHAKE_FAILURE);

		String expectedDescription = AlertDescription.HANDSHAKE_FAILURE.toString();

		assertEquals(expectedDescription, alert.getDisplayDescription());
	}
}

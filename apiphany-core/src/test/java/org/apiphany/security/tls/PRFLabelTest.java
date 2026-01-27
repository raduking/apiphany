package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link PRFLabel}.
 *
 * @author Radu Sebastian LAZIN
 */
class PRFLabelTest {

	@ParameterizedTest
	@EnumSource(PRFLabel.class)
	void shouldReturnNonNullNonEmptyStringAndByteArray(final PRFLabel label) {
		String labelString = label.getLabel();
		byte[] labelBytes = label.toByteArray();

		assertNotNull(labelString);
		assertFalse(labelString.isEmpty());
		assertNotNull(labelBytes);

		assertArrayEquals(label.toByteArray(), labelString.getBytes(StandardCharsets.US_ASCII));
	}

	@ParameterizedTest
	@EnumSource(PRFLabel.class)
	void shouldBuildFromString(final PRFLabel label) {
		PRFLabel fromString = PRFLabel.fromValue(label.getLabel());

		assertNotNull(fromString);
	}
}

package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link HandshakeType}.
 *
 * @author Radu Sebastian LAZIN
 */
class HandshakeTypeTest {

	@ParameterizedTest
	@EnumSource(HandshakeType.class)
	void shouldHaveTheCorrectSize(final HandshakeType handshakeType) {
		int size = handshakeType.sizeOf();

		assertThat(size, equalTo(1));
	}
}

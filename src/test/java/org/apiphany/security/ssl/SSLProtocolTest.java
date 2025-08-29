package org.apiphany.security.ssl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@link SSLProtocol}.
 *
 * @author Radu Sebastian LAZIN
 */
class SSLProtocolTest {

	@ParameterizedTest
	@EnumSource(SSLProtocol.class)
	void shouldBuildWithFromStringWithValidValue(final SSLProtocol sslProtocol) {
		String stringValue = sslProtocol.value();
		SSLProtocol result = SSLProtocol.fromString(stringValue);
		assertThat(result, equalTo(sslProtocol));
	}

	@SuppressWarnings("deprecation")
	private static Stream<Arguments> provideHandshakeArguments() {
		return Stream.of(
				Arguments.of(SSLProtocol.SSL_2_0, Short.valueOf((short) 0x0200)),
				Arguments.of(SSLProtocol.SSL_3_0, Short.valueOf((short) 0x0300)),
				Arguments.of(SSLProtocol.TLS_1_0, Short.valueOf((short) 0x0301)),
				Arguments.of(SSLProtocol.TLS_1_1, Short.valueOf((short) 0x0302)),
				Arguments.of(SSLProtocol.TLS_1_2, Short.valueOf((short) 0x0303)),
				Arguments.of(SSLProtocol.TLS_1_3, Short.valueOf((short) 0x0304)));
	}

	@ParameterizedTest
	@MethodSource("provideHandshakeArguments")
	void shouldReturnCorrectHandshakeVersion(SSLProtocol sslProtocol, Short version) {
		boolean validVersion = sslProtocol.handshakeVersion() == version;
		assertTrue(validVersion);
	}
}

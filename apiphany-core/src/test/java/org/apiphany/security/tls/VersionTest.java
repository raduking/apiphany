package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apiphany.security.ssl.SSLProtocol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link Version}.
 *
 * @author Radu Sebastian LAZIN
 */
class VersionTest {

	@ParameterizedTest
	@EnumSource(SSLProtocol.class)
	void shouldBuildVersionFromSSLProtocol(final SSLProtocol sslProtocol) throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[] {
				sslProtocol.majorVersion(),
				sslProtocol.minorVersion()
		});

		Version version = Version.from(byteArrayInputStream);

		assertThat(version.getProtocol(), equalTo(sslProtocol));
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		Version version1 = new Version(SSLProtocol.TLS_1_2);
		Version version2 = new Version(SSLProtocol.TLS_1_2);

		// same reference
		assertEquals(version1, version1);

		// different instance, same values
		assertEquals(version1, version2);
		assertEquals(version2, version1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(version1.hashCode(), version2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		Version version1 = new Version(SSLProtocol.TLS_1_2);
		Version version2 = new Version(SSLProtocol.TLS_1_3);

		// different values
		assertNotEquals(version1, version2);
		assertNotEquals(version2, version1);

		// different types
		assertNotEquals(version1, null);
		assertNotEquals(version2, "some string");
	}
}

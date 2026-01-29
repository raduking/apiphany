package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link KeyExchangeAlgorithm}.
 *
 * @author Radu Sebastian LAZIN
 */
class KeyExchangeAlgorithmTest {

	@ParameterizedTest
	@EnumSource(KeyExchangeAlgorithm.class)
	void shouldBuildEnumFromString(final KeyExchangeAlgorithm algorithm) {
		String name = algorithm.name();
		KeyExchangeAlgorithm fromString = KeyExchangeAlgorithm.fromString(name);

		assertNotNull(fromString);
		assertEquals(algorithm, fromString);
	}
}

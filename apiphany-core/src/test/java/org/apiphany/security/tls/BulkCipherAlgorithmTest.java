package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link BulkCipherAlgorithm}.
 *
 * @author Radu Sebastian LAZIN
 */
class BulkCipherAlgorithmTest {

	@ParameterizedTest
	@EnumSource(BulkCipherAlgorithm.class)
	void shouldReturnJCANameOnToString(final BulkCipherAlgorithm algorithm) {
		assertThat(algorithm.toString(), equalTo(algorithm.jcaName()));
	}

}

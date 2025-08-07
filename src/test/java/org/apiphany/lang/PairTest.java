package org.apiphany.lang;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Pair}.
 *
 * @author Radu Sebastian LAZIN
 */
class PairTest {

	private static final String SOME_STRING = "someString";
	private static final Integer SOME_INTEGER = Integer.MAX_VALUE;

	@Test
	void shouldBuildAPairWithBothValues() {
		Pair<String, Integer> pair = Pair.of(SOME_STRING, SOME_INTEGER);

		assertThat(pair.left(), equalTo(SOME_STRING));
		assertThat(pair.right(), equalTo(SOME_INTEGER));
	}

	@Test
	void shouldConvertAPairToMap() {
		Pair<String, Integer> pair = Pair.of(SOME_STRING, SOME_INTEGER);

		Map<String, Integer> map = pair.toMap();

		assertThat(map.entrySet(), hasSize(1));

		assertThat(map.get(SOME_STRING), equalTo(SOME_INTEGER));
	}
}

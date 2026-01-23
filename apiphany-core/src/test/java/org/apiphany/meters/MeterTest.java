package org.apiphany.meters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Meter}.
 *
 * @author Radu Sebastian LAZIN
 */
class MeterTest {

	private static final String NAME = "name";

	@Test
	void shouldUnwrapTheMeterClass() {
		Meter testMeter = new TestMeter();

		TestMeter result = testMeter.unwrap(TestMeter.class);

		assertThat(result, equalTo(testMeter));
		assertThat(result.getName(), equalTo(NAME));
	}

	@Test
	void shouldThrowExceptionIfTheMeterClassIsWrong() {
		Meter testMeter = () -> NAME;

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> testMeter.unwrap(TestMeter.class));

		assertThat(e, notNullValue());
		assertThat(e.getMessage(), equalTo("The meter class " + testMeter.getClass() + " is not of type " + TestMeter.class));
	}

	static class TestMeter implements Meter {

		@Override
		public String getName() {
			return NAME;
		}

	}

}

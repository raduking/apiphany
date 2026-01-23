package org.apiphany.lang;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Holder}.
 *
 * @author Radu Sebastian LAZIN
 */
class HolderTest {

	private static final String TEST_STRING = "mumu";

	@Test
	void shouldConstructAHolderWithoutAValue() {
		Holder<String> holder = new Holder<>();

		assertThat(holder.getValue(), nullValue());
	}

	@Test
	void shouldConstructAHolderWithAValue() {
		Holder<String> holder = new Holder<>(TEST_STRING);

		assertThat(holder.getValue(), equalTo(TEST_STRING));
	}

	@Test
	void shouldConstructAHolderWithoutAValueWithStaticMethod() {
		Holder<String> holder = Holder.noValue();

		assertThat(holder.getValue(), nullValue());
	}

	@Test
	void shouldConstructAHolderWithAValueWithStaticMethod() {
		Holder<String> holder = Holder.of(TEST_STRING);

		assertThat(holder.getValue(), equalTo(TEST_STRING));
	}

}

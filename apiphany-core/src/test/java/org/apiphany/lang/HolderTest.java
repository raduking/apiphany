package org.apiphany.lang;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Nested;
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

	@Nested
	class EqualsTests {

		@Test
		void shouldCheckValueOnEquals() {
			Holder<String> holder1 = Holder.of(TEST_STRING);
			Holder<String> holder2 = Holder.of(TEST_STRING);

			assertThat(holder1, equalTo(holder2));
		}

		@Test
		void shouldCheckNullValueOnEquals() {
			Holder<String> holder1 = Holder.noValue();
			Holder<String> holder2 = Holder.noValue();

			assertThat(holder1, equalTo(holder2));
		}

		@Test
		void shouldNotEqualIfOneValueIsNull() {
			Holder<String> holder1 = Holder.noValue();
			Holder<String> holder2 = Holder.of(TEST_STRING);

			assertThat(holder1, not(equalTo(holder2)));
		}

		@Test
		void shouldNotEqualIfValuesAreDifferent() {
			Holder<String> holder1 = Holder.of(TEST_STRING);
			Holder<String> holder2 = Holder.of("different");

			assertThat(holder1, not(equalTo(holder2)));
		}

		@Test
		void shouldNotEqualIfOtherIsNotAHolder() {
			Holder<String> holder = Holder.of(TEST_STRING);
			String other = TEST_STRING;

			assertThat(holder, not(equalTo(other)));
		}

		@Test
		void shouldEqualSameInstance() {
			Holder<String> holder = Holder.of(TEST_STRING);

			assertThat(holder, equalTo(holder));
		}
	}

	@Test
	void shouldHaveHashCodeBasedOnValue() {
		Holder<String> holder1 = Holder.of(TEST_STRING);
		Holder<String> holder2 = Holder.of(TEST_STRING);

		assertThat(holder1.hashCode(), equalTo(holder2.hashCode()));
		assertThat(holder1.hashCode(), equalTo(TEST_STRING.hashCode()));
	}
}

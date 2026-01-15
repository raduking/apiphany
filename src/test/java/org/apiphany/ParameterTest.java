package org.apiphany;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Parameter}.
 *
 * @author Radu Sebastian LAZIN
 */
class ParameterTest {

	private static final String NAME = "name";
	private static final String VALUE = "value";
	private static final String BLANK_STRING = "   ";

	@Test
	void shouldBuildParameterObjectFromStringValues() {
		Parameter parameter = Parameter.of(NAME, VALUE);

		assertThat(parameter.name(), equalTo(NAME));
		assertThat(parameter.value(), equalTo(VALUE));
	}

	@Test
	void shouldBuildParameterObjectFromObjectValues() {
		Parameter parameter = Parameter.of(NAME, 123);

		assertThat(parameter.name(), equalTo(NAME));
		assertThat(parameter.value(), equalTo("123"));
	}

	@Test
	void shouldBuildParameterObjectFromListValues() {
		Parameter parameter = Parameter.of(NAME, List.of(1, 2, 3));

		assertThat(parameter.name(), equalTo(NAME));
		assertThat(parameter.value(), equalTo("1,2,3"));
	}

	@Test
	void shouldBuildParameterObjectFromArrayValues() {
		Parameter parameter = Parameter.of(NAME, new Object[] { "a", "b", "c" });

		assertThat(parameter.name(), equalTo(NAME));
		assertThat(parameter.value(), equalTo("a,b,c"));
	}

	@Test
	void shouldBuildParameterObjectFromStringArrayValues() {
		Parameter parameter = Parameter.of(NAME, new String[] { "a", "b", "c" });

		assertThat(parameter.name(), equalTo(NAME));
		assertThat(parameter.value(), equalTo("a,b,c"));
	}

	static record A(String name) {

		@Override
		public final String toString() {
			return name;
		}
	}

	@Test
	void shouldBuildParameterObjectFromObject() {
		Parameter parameter = Parameter.of(NAME, new A("test"));

		assertThat(parameter.name(), equalTo(NAME));
		assertThat(parameter.value(), equalTo("test"));
	}

	@Test
	void shouldThrowExceptionWhenBuildingParameterWithConstructorAndNullName() {
		NullPointerException e = assertThrows(NullPointerException.class, () -> new Parameter(null, VALUE));

		assertThat(e.getMessage(), equalTo("parameter name cannot be null"));
	}

	@Test
	void shouldThrowExceptionWhenBuildingParameterWithConstructorAndBlankName() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new Parameter(BLANK_STRING, VALUE));

		assertThat(e.getMessage(), equalTo("Parameter name cannot be blank"));
	}

	@Test
	void shouldThrowExceptionWhenBuildingParameterWithNullName() {
		NullPointerException e = assertThrows(NullPointerException.class, () -> Parameter.of(null, VALUE));

		assertThat(e.getMessage(), equalTo("parameter name cannot be null"));
	}

	@Test
	void shouldThrowExceptionWhenBuildingParameterWithBlankName() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Parameter.of(BLANK_STRING, VALUE));

		assertThat(e.getMessage(), equalTo("Parameter name cannot be blank"));
	}

	@Test
	void shouldBuildParameterString() {
		Parameter parameter = Parameter.of(NAME, VALUE);

		String result = parameter.toString();

		assertThat(result, equalTo("name=value"));
	}

	@Test
	void shouldBuildStringValueFromObject() {
		String result = Parameter.value(123);

		assertThat(result, equalTo("123"));
	}

	@Test
	void shouldReturnNullWhenValueIsNull() {
		String result = Parameter.value((Object) null);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenValueArrayIsNull() {
		String result = Parameter.value((Object[]) null);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldBuildValueFromObject() {
		String parameter = Parameter.value(new A("test"));

		assertThat(parameter, equalTo("test"));
	}

	@Test
	void shouldBuildValueFromIntegerArray() {
		String parameter = Parameter.value((Object) new Integer[] { 1, 2, 3 });

		assertThat(parameter, equalTo("1,2,3"));
	}

	@Test
	void shouldBuildValueFromIntArray() {
		String parameter = Parameter.value(new int[] { 1, 2, 3 });

		assertThat(parameter, equalTo("1,2,3"));
	}

	@Test
	void shouldPutParameterIntoMap() {
		Parameter parameter = Parameter.of(NAME, VALUE);
		Map<String, String> map = new HashMap<>();

		parameter.putInto(map);

		assertThat(map.get(NAME), equalTo(VALUE));
	}
}

package org.apiphany;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link RequestParameter}.
 *
 * @author Radu Sebastian LAZIN
 */
class RequestParameterTest {

	private static final String NAME = "name";
	private static final String VALUE = "value";
	private static final String BLANK_STRING = "   ";

	@Test
	void shouldBuildParameterObjectFromStringValues() {
		RequestParameter parameter = RequestParameter.of(NAME, VALUE);

		assertThat(parameter.name(), equalTo(NAME));
		assertThat(parameter.value(), equalTo(VALUE));
	}

	@Test
	void shouldBuildParameterObjectFromObjectValues() {
		RequestParameter parameter = RequestParameter.of(NAME, 123);

		assertThat(parameter.name(), equalTo(NAME));
		assertThat(parameter.value(), equalTo("123"));
	}

	@Test
	void shouldBuildParameterObjectFromListValues() {
		RequestParameter parameter = RequestParameter.of(NAME, List.of(1, 2, 3));

		assertThat(parameter.name(), equalTo(NAME));
		assertThat(parameter.value(), equalTo("1,2,3"));
	}

	@Test
	void shouldBuildParameterObjectFromArrayValues() {
		RequestParameter parameter = RequestParameter.of(NAME, new Object[] { "a", "b", "c" });

		assertThat(parameter.name(), equalTo(NAME));
		assertThat(parameter.value(), equalTo("a,b,c"));
	}

	@Test
	void shouldBuildParameterObjectFromStringArrayValues() {
		RequestParameter parameter = RequestParameter.of(NAME, new String[] { "a", "b", "c" });

		assertThat(parameter.name(), equalTo(NAME));
		assertThat(parameter.value(), equalTo("a,b,c"));
	}

	record A(String name) {

		@Override
		public String toString() {
			return name;
		}
	}

	@Test
	void shouldBuildParameterObjectFromObject() {
		RequestParameter parameter = RequestParameter.of(NAME, new A("test"));

		assertThat(parameter.name(), equalTo(NAME));
		assertThat(parameter.value(), equalTo("test"));
	}

	@Test
	void shouldThrowExceptionWhenBuildingParameterWithConstructorAndNullName() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new RequestParameter(null, VALUE));

		assertThat(e.getMessage(), equalTo("Parameter name cannot be null"));
	}

	@Test
	void shouldThrowExceptionWhenBuildingParameterWithConstructorAndBlankName() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new RequestParameter(BLANK_STRING, VALUE));

		assertThat(e.getMessage(), equalTo("Parameter name cannot be blank"));
	}

	@Test
	void shouldThrowExceptionWhenBuildingParameterWithNullName() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> RequestParameter.of(null, VALUE));

		assertThat(e.getMessage(), equalTo("Parameter name cannot be null"));
	}

	@Test
	void shouldThrowExceptionWhenBuildingParameterWithBlankName() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> RequestParameter.of(BLANK_STRING, VALUE));

		assertThat(e.getMessage(), equalTo("Parameter name cannot be blank"));
	}

	@Test
	void shouldBuildParameterString() {
		RequestParameter parameter = RequestParameter.of(NAME, VALUE);

		String result = parameter.toString();

		assertThat(result, equalTo("name=value"));
	}

	@Test
	void shouldBuildStringValueFromObject() {
		String result = RequestParameter.value(123);

		assertThat(result, equalTo("123"));
	}

	@Test
	void shouldReturnNullWhenValueIsNull() {
		String result = RequestParameter.value((Object) null);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenValueArrayIsNull() {
		String result = RequestParameter.value(null);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldBuildValueFromObject() {
		String parameter = RequestParameter.value(new A("test"));

		assertThat(parameter, equalTo("test"));
	}

	@Test
	void shouldBuildValueFromIntegerArray() {
		String parameter = RequestParameter.value((Object) new Integer[] { 1, 2, 3 });

		assertThat(parameter, equalTo("1,2,3"));
	}

	@Test
	void shouldBuildValueFromIntArray() {
		String parameter = RequestParameter.value(new int[] { 1, 2, 3 });

		assertThat(parameter, equalTo("1,2,3"));
	}

	@Test
	void shouldPutParameterIntoMap() {
		RequestParameter parameter = RequestParameter.of(NAME, VALUE);
		Map<String, String> map = new HashMap<>();

		parameter.putInto(map);

		assertThat(map.get(NAME), equalTo(VALUE));
	}
}

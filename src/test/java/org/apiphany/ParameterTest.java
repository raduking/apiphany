package org.apiphany;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Parameter}.
 *
 * @author Radu Sebastian LAZIN
 */
class ParameterTest {

	@Test
	void shouldBuildParameterObjectFromStringValues() {
		Parameter parameter = Parameter.of("name", "value");

		assertThat(parameter.name(), equalTo("name"));
		assertThat(parameter.value(), equalTo("value"));
	}

	@Test
	void shouldBuildParameterObjectFromObjectValues() {
		Parameter parameter = Parameter.of("name", 123);

		assertThat(parameter.name(), equalTo("name"));
		assertThat(parameter.value(), equalTo("123"));
	}

	@Test
	void shouldBuildParameterObjectFromListValues() {
		Parameter parameter = Parameter.of("name", List.of(1, 2, 3));

		assertThat(parameter.name(), equalTo("name"));
		assertThat(parameter.value(), equalTo("1,2,3"));
	}

	@Test
	void shouldBuildParameterObjectFromArrayValues() {
		Parameter parameter = Parameter.of("name", new Object[] {"a", "b", "c"});

		assertThat(parameter.name(), equalTo("name"));
		assertThat(parameter.value(), equalTo("a,b,c"));
	}

	@Test
	void shouldThrowExceptionWhenBuildingParameterWithNullName() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Parameter.of(null, "value"));

		assertThat(e.getMessage(), equalTo("Parameter name cannot be null or blank"));
	}

	@Test
	void shouldThrowExceptionWhenBuildingParameterWithBlankName() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Parameter.of("   ", "value"));

		assertThat(e.getMessage(), equalTo("Parameter name cannot be null or blank"));
	}

	@Test
	void shouldBuildParameterString() {
		Parameter parameter = Parameter.of("name", "value");

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
}

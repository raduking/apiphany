package org.apiphany;

import static org.apiphany.ParameterFunction.parameter;
import static org.apiphany.ParameterFunction.withCondition;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashMap;
import java.util.Map;

import org.apiphany.utils.Tests;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link RequestParameters}.
 *
 * @author Radu Sebastian LAZIN
 */
class RequestParametersTest {

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(RequestParameters.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldCreateMapWithAllParameters() {
		Map<String, String> requestParameters = RequestParameters.of(
				parameter("x1", "y1"),
				withCondition(true,
						parameter("x2", "y2")));

		Map<String, String> expected = new HashMap<>();
		expected.put("x1", "y1");
		expected.put("x2", "y2");

		assertThat(requestParameters, equalTo(expected));
	}

	@Test
	void shouldReturnEmptyMapIfNoParametersWereSupplied() {
		Map<String, String> params = RequestParameters.of(new ParameterFunction[0]);

		assertThat(params.entrySet(), hasSize(0));
	}

	@Test
	void shouldReturnEmptyMapIfParametersIsNullSupplied() {
		Map<String, String> params = RequestParameters.of((ParameterFunction[]) null);

		assertThat(params.entrySet(), hasSize(0));
	}

	@Test
	void shouldReturnEmptyMapIfParametersStringIsEmpty() {
		Map<String, String> params = RequestParameters.from("");

		assertThat(params.entrySet(), hasSize(0));
	}

	@Test
	void shouldReturnEmptyMapIfParametersStringIsNull() {
		Map<String, String> params = RequestParameters.from(null);

		assertThat(params.entrySet(), hasSize(0));
	}

	@Test
	void shouldDecodeParametersCorrectly() {
		Map<String, String> params = RequestParameters.from("user%20name=John%20Doe");

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get("user name"), equalTo("John Doe"));
	}

	@Test
	void shouldReadParametersWithoutEquals() {
		Map<String, String> params = RequestParameters.from("user");

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get("user"), equalTo(""));
	}

	@Test
	void shouldConvertFromObject() {
		TestParams testParams = new TestParams();
		testParams.setParam1("value1");
		testParams.setParam2("value2");

		Map<String, String> params = RequestParameters.from(testParams);

		assertThat(params.entrySet(), hasSize(2));
		assertThat(params.get("param1"), equalTo("value1"));
		assertThat(params.get("param2"), equalTo("value2"));
	}

	static class TestParams {

		private String param1;
		private String param2;

		public String getParam1() {
			return param1;
		}

		public void setParam1(final String param1) {
			this.param1 = param1;
		}

		public String getParam2() {
			return param2;
		}

		public void setParam2(final String param2) {
			this.param2 = param2;
		}
	}

	static class B {

		private Integer key1;
		private Long key2;

		public Integer getKey1() {
			return key1;
		}

		public void setKey1(final Integer param1) {
			this.key1 = param1;
		}

		public Long getKey2() {
			return key2;
		}

		public void setKey2(final Long param2) {
			this.key2 = param2;
		}
	}

	@Test
	void shouldConvertFromObjectWithNonStringFields() {
		B b = new B();
		b.setKey1(100);
		b.setKey2(200L);

		Map<String, String> params = RequestParameters.from(b);

		assertThat(params.entrySet(), hasSize(2));
		assertThat(params.get("key1"), equalTo("100"));
		assertThat(params.get("key2"), equalTo("200"));
	}

	@Test
	void shouldConvertFromEmptyObject() {
		B b = new B();

		Map<String, String> params = RequestParameters.from(b);

		assertThat(params.entrySet(), hasSize(2));
		assertThat(params.get("key1"), equalTo("null"));
		assertThat(params.get("key2"), equalTo("null"));
	}

	@Test
	void shouldConvertFromNullObject() {
		Map<String, String> params = RequestParameters.from(null);

		assertThat(params.entrySet(), hasSize(0));
	}
}

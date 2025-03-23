package org.apiphany;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.List;

import org.apiphany.Filter.Operator;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Filter}.
 *
 * @author Radu Sebastian LAZIN
 */
class FilterTest {

	@Test
	void shouldBuildTheFilter() {
		Filter filter = Filter.of("songCount", Filter.Operator.EQ, "1234");

		String result = filter.toString();

		assertThat(result, equalTo("filter=songCount==1234"));
	}

	@Test
	void shouldBuildAndOrFilter() {
		List<String> fields = List.of("1", "2", "3");

		Filter filter =
				Filter.allOf(
						Filter.of("cover", Operator.EQ, "HARD"),
						Filter.anyOf("field", Operator.EQ, fields)
				);

		String result = filter.toString();

		assertThat(result, equalTo("filter=cover==HARD;(field==1,field==2,field==3)"));
	}

	@Test
	void shouldBuildOrAndFilter() {
		List<String> fields = List.of("1", "2", "3");

		Filter filter =
				Filter.anyOf(
						Filter.of("cover", Operator.EQ, "HARD"),
						Filter.allOf("field", Operator.NE, fields)
				);

		String result = filter.toString();

		assertThat(result, equalTo("filter=cover==HARD,field!=1;field!=2;field!=3"));
	}

	@Test
	void shouldBuildAndOrFilterWithStringOperators() {
		List<String> fields = List.of("1", "2", "3");

		Filter filter =
				Filter.allOf(
						Filter.of("cover", "=eq=", "HARD"),
						Filter.anyOf("field", "=eq=", fields)
				);

		String result = filter.toString();

		assertThat(result, equalTo("filter=cover=eq=HARD;(field=eq=1,field=eq=2,field=eq=3)"));
	}

	@Test
	void shouldBuildOrAndFilterWithStringOperators() {
		List<String> fields = List.of("1", "2", "3");

		Filter filter =
				Filter.anyOf(
						Filter.of("cover", "=eq=", "HARD"),
						Filter.allOf("ext", "=ne=", fields)
				);

		String result = filter.toString();

		assertThat(result, equalTo("filter=cover=eq=HARD,ext=ne=1;ext=ne=2;ext=ne=3"));
	}

	@Test
	void shouldReturnEmptyStringOnNullValue() {
		Filter filter = Filter.of(null);

		String result = filter.toString();

		assertThat(result, equalTo(""));
	}

	@Test
	void shouldReturnEmptyStringOnEmptyValue() {
		Filter filter = Filter.of("");

		String result = filter.toString();

		assertThat(result, equalTo(""));
	}

	@Test
	void shouldReturnEmptyStringAnyOfWithNullValue() {
		Filter filter = Filter.anyOf((Filter[]) null);

		String result = filter.toString();

		assertThat(result, equalTo(""));
	}

	@Test
	void shouldReturnEmptyStringAnyOfWithEmptyList() {
		Filter filter = Filter.anyOf(Collections.emptyList());

		String result = filter.toString();

		assertThat(result, equalTo(""));
	}

	@Test
	void shouldReturnEmptyStringAllOfWithNullValue() {
		Filter filter = Filter.allOf((Filter[]) null);

		String result = filter.toString();

		assertThat(result, equalTo(""));
	}

	@Test
	void shouldReturnEmptyStringAllOfWithEmptyList() {
		Filter filter = Filter.allOf(Collections.emptyList());

		String result = filter.toString();

		assertThat(result, equalTo(""));
	}

	@Test
	void shouldBuildComplexFilter() {
		Filter filter = Filter.anyOf(
				Filter.allOf(
						Filter.of("type", Operator.EQ, "song"),
						Filter.of("mimeType", Operator.EQ, "audio/mpeg"),
						Filter.anyOf(
								Filter.of("artist", Operator.EQ, "ssc"),
								Filter.of("songId", Operator.EQ, "iv")
						)
				),
				Filter.of("type", Operator.EQ, "idea")
		);

		String result = filter.toString();

		assertThat(result, equalTo("filter=type==song;mimeType==audio/mpeg;(artist==ssc,songId==iv),type==idea"));
	}

	@Test
	void shouldBuildComplexFilterWithParenthesesOnAnd() {
		Filter filter = Filter.anyOf(
				Filter.allOf(true,
						Filter.of("type", Operator.EQ, "song"),
						Filter.of("mimeType", Operator.EQ, "audio/mpeg"),
						Filter.anyOf(
								Filter.of("artist", Operator.EQ, "ssc"),
								Filter.of("songId", Operator.EQ, "iv")
						)
				),
				Filter.of("type", Operator.EQ, "idea")
		);

		String result = filter.toString();

		assertThat(result, equalTo("filter=(type==song;mimeType==audio/mpeg;(artist==ssc,songId==iv)),type==idea"));
	}

	enum E {
		TYPE;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	@Test
	void shouldBuildAnyOfFilterWithTheSameOperatorForGivenValuesForEnum() {
		Filter filter = Filter.anyOf(E.TYPE, Operator.EQ, "live", "song");

		String result = filter.toString();

		assertThat(result, equalTo("filter=type==live,type==song"));
	}

	@Test
	void shouldBuildAnyOfFilterWithTheSameOperatorForGivenValuesListForEnum() {
		Filter filter = Filter.anyOf(E.TYPE, Operator.EQ, List.of("live", "song"));

		String result = filter.toString();

		assertThat(result, equalTo("filter=type==live,type==song"));
	}

	@Test
	void shouldBuildAllOfFilterWithTheSameOperatorForGivenValuesListForEnum() {
		Filter filter = Filter.allOf(E.TYPE, Operator.EQ, List.of("live", "song"));

		String result = filter.toString();

		assertThat(result, equalTo("filter=type==live;type==song"));
	}
}

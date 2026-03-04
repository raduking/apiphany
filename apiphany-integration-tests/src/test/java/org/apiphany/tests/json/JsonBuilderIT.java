package org.apiphany.tests.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericClass;

/**
 * Integration tests for {@link JsonBuilder}.
 * <p>
 * These tests must pass for every JSON library implementation, as they verify the basic contract of transforming
 * objects to JSON and back.
 *
 * @author Radu Sebastian LAZIN
 */
class JsonBuilderIT {

	private static final String ID = "id1";
	private static final String NAME = "name1";

	static class A {

		private String id;

		private String name;

		public A() {
			// empty
		}

		public A(final String id, final String name) {
			this.id = id;
			this.name = name;
		}

		public String getId() {
			return id;
		}

		public void setId(final String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}
	}

	@Test
	void shouldTransformObjectToJsonAndReadItBack() {
		A a1 = new A(ID, NAME);

		Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

		A a2 = JsonBuilder.fromJson(json1, A.class);

		Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldTransformGenericObjectToJsonAndReadItBack() {
		List<A> list1 = List.of(new A(ID, NAME));

		Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(list1));

		List<A> list2 = JsonBuilder.fromJson(json1, new GenericClass<>() {
			// empty
		});

		Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(list2));

		assertThat(json1, equalTo(json2));
	}
}

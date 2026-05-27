package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.Map;

import org.apiphany.lang.Bytes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Multipart}.
 *
 * @author Radu Sebastian LAZIN
 */
class MultipartTest {

	@Nested
	class BuilderTest {

		@Test
		void shouldBuildMultipartBody() {
			Multipart.Body multipart = Multipart.Body.builder()
					.field("field1", "value1")
					.field("field2", "value2")
					.build();

			assertThat(multipart.getParts(), hasSize(2));
			assertThat(multipart.getParts().get(0).getName(), equalTo("field1"));
			assertThat(multipart.getParts().get(1).getName(), equalTo("field2"));
			assertThat(multipart.getParts().get(0).getValue(), equalTo("value1"));
			assertThat(multipart.getParts().get(1).getValue(), equalTo("value2"));
		}

		@Test
		void shouldBuildEmptyMultipartBody() {
			Multipart.Body multipart = Multipart.Body.builder()
					.build();

			assertThat(multipart.getParts(), hasSize(0));
		}
	}

	@Nested
	class PartTest {

		@Test
		void shouldCreatePartWithNoHeadersAndEmptyBody() {
			Multipart.Part part = new Multipart.Part(Map.of(), Bytes.EMPTY);

			assertThat(part.getName(), equalTo(null));
			assertThat(part.getValue(), equalTo(""));
		}
	}
}

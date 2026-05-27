package org.apiphany.multipart;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link MultipartMessage}.
 *
 * @author Radu Sebastian LAZIN
 */
class MultipartMessageTest {

	@Nested
	class BuilderTest {

		@Test
		void shouldBuildMultipartBody() {
			MultipartMessage multipart = MultipartMessage.builder()
					.randomBoundary()
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
			MultipartMessage multipart = MultipartMessage.builder()
					.randomBoundary()
					.build();

			assertThat(multipart.getParts(), hasSize(0));
		}
	}
}

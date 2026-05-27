package org.apiphany.multipart;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.apiphany.lang.Bytes;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link MultipartPart}.
 *
 * @author Radu Sebastian LAZIN
 */
class MultipartPartTest {

	@Test
	void shouldCreatePartWithNoHeadersAndEmptyBody() {
		MultipartPart<byte[]> part = new MultipartPart<>(Map.of(), Bytes.EMPTY);

		assertThat(part.getName(), equalTo(null));
		assertThat(part.getValue(), equalTo(""));
	}
}

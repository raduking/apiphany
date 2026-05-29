package org.apiphany.multipart;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apiphany.test.Assertions;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link MultipartEncoder}.
 *
 * @author Radu Sebastian LAZIN
 */
class MultipartEncoderTest {

	@Test
	void shouldThrowExceptionOnInstantiating() {
		UnsupportedOperationException exception = Assertions.assertDefaultConstructorThrows(MultipartEncoder.class);

		assertThat(exception.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}

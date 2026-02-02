package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ServerHelloDone}.
 *
 * @author Radu Sebastian LAZIN
 */
class ServerHelloDoneTest {

	private static final int BYTES = 0;

	@Test
	void shouldBuildWithCorrectSize() {
		ServerHelloDone shd = new ServerHelloDone();

		assertEquals(BYTES, shd.toByteArray().length);
		assertEquals(BYTES, shd.sizeOf());
	}

	@Test
	void shouldReadFromInputStream() throws Exception {
		ServerHelloDone shd = ServerHelloDone.from(new ByteArrayInputStream(new byte[0]));

		assertNotNull(shd);
		assertEquals(BYTES, shd.sizeOf());
	}

	@Test
	void shouldSerializeToString() {
		ServerHelloDone shd = new ServerHelloDone();

		String result = shd.toString();

		assertNotNull(result);
	}
}

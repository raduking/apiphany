package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apiphany.io.BytesWrapper;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link RawHandshakeBody}.
 *
 * @author Radu Sebastian LAZIN
 */
class RawHandshakeBodyTest {

	private static final int SIZE = 13;

	@Test
	void shouldTransformToByteArrayAndReadItBack() throws IOException {
		byte[] bytes = new byte[SIZE];
		Arrays.fill(bytes, (byte) 0x17);

		RawHandshakeBody body = new RawHandshakeBody(HandshakeType.CERTIFICATE, new BytesWrapper(bytes));
		byte[] bodyBytes1 = body.toByteArray();

		body = RawHandshakeBody.from(new ByteArrayInputStream(bodyBytes1), HandshakeType.CERTIFICATE, SIZE);
		assertThat(body.getType(), equalTo(HandshakeType.CERTIFICATE));
		assertThat(body.getBytes().toByteArray(), equalTo(bodyBytes1));

		byte[] bodyBytes2 = body.toByteArray();

		assertThat(bodyBytes1, equalTo(bodyBytes2));
	}

}

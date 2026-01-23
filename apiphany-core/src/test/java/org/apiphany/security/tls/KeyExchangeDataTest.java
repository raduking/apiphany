package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apiphany.io.BytesWrapper;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link KeyExchangeData}.
 *
 * @author Radu Sebastian LAZIN
 */
class KeyExchangeDataTest {

	private static final int SIZE = 13;

	@Test
	void shouldTransformToByteArrayAndReadItBack() throws IOException {
		byte[] bytes = new byte[SIZE];
		Arrays.fill(bytes, (byte) 0x17);

		KeyExchangeData body = new KeyExchangeData(new BytesWrapper(bytes));
		byte[] bodyBytes1 = body.toByteArray();

		body = KeyExchangeData.from(new ByteArrayInputStream(bodyBytes1), SIZE);
		assertThat(body.getBytes().toByteArray(), equalTo(bodyBytes1));

		byte[] bodyBytes2 = body.toByteArray();

		assertThat(bodyBytes1, equalTo(bodyBytes2));
	}

}

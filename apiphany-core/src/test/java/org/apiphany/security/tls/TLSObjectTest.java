package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.lang.LoggingFormat;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link TLSObject}.
 *
 * @author Radu Sebastian LAZIN
 */
class TLSObjectTest {

	@Test
	void shouldFormatWithHexLoggingFormat() {
		HandshakeType type = HandshakeType.CERTIFICATE;

		String expected = Strings.EOL + "0B" + Strings.EOL;
		String result = TLSObject.serialize(type, LoggingFormat.HEX);

		assertThat(result, equalTo(expected));
	}
}

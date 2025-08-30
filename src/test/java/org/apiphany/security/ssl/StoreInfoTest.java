package org.apiphany.security.ssl;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link StoreInfo}.
 *
 * @author Radu Sebastian LAZIN
 */
class StoreInfoTest {

	@Test
	void shouldReadButNotWritePassword() {
		String jsonRead = Strings.fromFile("/security/ssl/store-info.json");
		StoreInfo keystoreRead = JsonBuilder.fromJson(jsonRead, StoreInfo.class);

		String jsonWrite = keystoreRead.toString();
		StoreInfo keystoreWrite = JsonBuilder.fromJson(jsonWrite, StoreInfo.class);

		assertThat(keystoreRead.getPassword(), notNullValue());
		assertThat(keystoreWrite.getPassword(), nullValue());
	}

}

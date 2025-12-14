package org.apiphany.security.ssl;

import static org.hamcrest.CoreMatchers.equalTo;
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

	private static final String LOCATION = "keystore.jks";

	@Test
	void shouldReadButNotWritePassword() {
		String jsonRead = Strings.fromFile("/security/ssl/store-info.json");
		StoreInfo keystoreRead = JsonBuilder.fromJson(jsonRead, StoreInfo.class);

		String jsonWrite = keystoreRead.toString();
		StoreInfo keystoreWrite = JsonBuilder.fromJson(jsonWrite, StoreInfo.class);

		assertThat(keystoreRead.getPassword(), notNullValue());
		assertThat(keystoreWrite.getPassword(), nullValue());
	}

	@Test
	void shouldReturnDisplayLocation() {
		StoreInfo storeInfo = new StoreInfo();
		storeInfo.setLocation(LOCATION);

		String result = storeInfo.getDisplayLocation();

		assertThat(result, equalTo(LOCATION));
	}

	@Test
	void shouldReturnUnknownDisplayLocationWhenLocationIsMissing() {
		StoreInfo storeInfo = new StoreInfo();

		String result = storeInfo.getDisplayLocation();

		assertThat(result, equalTo(StoreInfo.UNKNOWN_LOCATION));
	}

	@Test
	void shouldSerializeToJson() {
		String json = Strings.fromFile("/security/ssl/store-info.json");

		StoreInfo result1 = JsonBuilder.fromJson(json, StoreInfo.class);

		json = result1.toString();

		StoreInfo result2 = JsonBuilder.fromJson(json, StoreInfo.class);

		assertThat(result1.toString(), equalTo(result2.toString()));
	}
}

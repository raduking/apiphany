package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;

import org.apiphany.ApiMessage;
import org.apiphany.ApiMimeType;
import org.apiphany.header.HeaderValues;
import org.apiphany.header.MapHeaderValues;
import org.apiphany.http.HttpHeader;
import org.apiphany.io.ContentType;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericClass;

/**
 * Test class for {@link HttpContentConverter}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpContentConverterTest {

	@Test
	void shouldReturnContentTypes() {
		Map<String, List<String>> headers = Map.of(HttpHeader.CONTENT_TYPE.value(), List.of(ContentType.Value.APPLICATION_JSON));

		StringHttpContentConverter converter = new StringHttpContentConverter();

		List<String> result = converter.getContentTypes(headers, new MapHeaderValues());

		assertThat(result, equalTo(List.of(ContentType.Value.APPLICATION_JSON)));
	}

	static class StringHttpContentConverter implements HttpContentConverter<String> {

		@Override
		public String from(final Object obj, final ApiMimeType contentType, final Class<String> targetClass) {
			return null;
		}

		@Override
		public String from(final Object obj, final ApiMimeType contentType, final GenericClass<String> targetGenericClass) {
			return null;
		}

		@Override
		public <U, H> boolean isConvertible(final ApiMessage<U> message, final ApiMimeType contentType, final H headers, final HeaderValues chain) {
			return false;
		}
	}
}

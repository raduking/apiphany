package org.apiphany.json.jackson2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.annotation.Creator;
import org.apiphany.lang.annotation.FieldName;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ApiphanyAnnotationIntrospector}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiphanyAnnotationIntrospectorTest {

	private static final String ERROR = "some error";
	private static final String ERROR_DESCRIPTION = "some error description";

	static class ErrorResponse {

		private final String error;

		private final String errorDescription;

		@Creator
		public ErrorResponse(
				@FieldName("error") final String error,
				@FieldName("error_description") final String description) {
			this.error = error;
			this.errorDescription = description;
		}

		public String getError() {
			return error;
		}

		public String getErrorDescription() {
			return errorDescription;
		}
	}

	@Test
	void shouldCreateObjectWithCreator() {
		String json = "{\"error\":\"" + ERROR + "\",\"error_description\":\"" + ERROR_DESCRIPTION + "\"}";

		ErrorResponse response = JsonBuilder.fromJson(json, ErrorResponse.class);

		assertThat(response.getError(), equalTo(ERROR));
		assertThat(response.getErrorDescription(), equalTo(ERROR_DESCRIPTION));
	}

}

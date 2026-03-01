package org.apiphany;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Status}.
 *
 * @author Radu Sebastian LAZIN
 */
class StatusTest {

	@Test
	void shouldReturnUnknownStatusForNullStatusOnMessage() {
		String result = Status.message(null);

		assertEquals("[unknown status]", result);
	}

	@Test
	void shouldReturnFormattedMessageForNonNullStatusOnMessage() {
		Status status = new TestStatus();
		String result = Status.message(status);

		assertEquals("[500 Internal Server Error]", result);
	}

	static class TestStatus implements Status {

		@Override
		public boolean isSuccess() {
			return false;
		}

		@Override
		public boolean isError() {
			return true;
		}

		@Override
		public int getCode() {
			return 500;
		}

		@Override
		public String getMessage() {
			return "Internal Server Error";
		}
	}
}

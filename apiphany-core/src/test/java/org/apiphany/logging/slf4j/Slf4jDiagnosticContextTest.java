package org.apiphany.logging.slf4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * Test class for {@link Slf4jDiagnosticContext}.
 *
 * @author Radu Sebastian LAZIN
 */
class Slf4jDiagnosticContextTest {

	private static final String TRACE_ID = "traceId";
	private static final String TRACE_VALUE = "some-trace-id";

	private final Slf4jDiagnosticContext diagnosticContext = new Slf4jDiagnosticContext();

	@AfterEach
	void tearDown() {
		MDC.clear();
	}

	@Test
	void shouldDelegateGetAndPutToSlf4j() {
		diagnosticContext.put(TRACE_ID, TRACE_VALUE);

		assertThat(diagnosticContext.get(TRACE_ID), equalTo(TRACE_VALUE));
		assertThat(MDC.get(TRACE_ID), equalTo(TRACE_VALUE));
	}

	@Test
	void shouldClearTheSlf4jContext() {
		diagnosticContext.put(TRACE_ID, TRACE_VALUE);

		diagnosticContext.clear();

		assertThat(diagnosticContext.get(TRACE_ID), nullValue());
		assertThat(MDC.get(TRACE_ID), nullValue());
	}

	@Test
	void shouldCopyTheContextMapFromSlf4j() {
		diagnosticContext.put(TRACE_ID, TRACE_VALUE);

		Map<String, String> copy = diagnosticContext.getCopyOfContextMap();

		assertThat(copy.get(TRACE_ID), equalTo(TRACE_VALUE));
	}

	@Test
	void shouldSetTheContextMapOnSlf4j() {
		diagnosticContext.setContextMap(Map.of(TRACE_ID, TRACE_VALUE));

		assertThat(MDC.get(TRACE_ID), equalTo(TRACE_VALUE));
	}

	@Test
	void shouldClearSlf4jWhenSetContextReceivesNull() {
		diagnosticContext.put(TRACE_ID, TRACE_VALUE);

		diagnosticContext.setContext(null);

		assertThat(MDC.get(TRACE_ID), nullValue());
	}
}

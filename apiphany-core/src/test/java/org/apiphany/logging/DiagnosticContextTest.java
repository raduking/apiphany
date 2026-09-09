package org.apiphany.logging;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apiphany.logging.slf4j.Slf4jDiagnosticContext;
import org.junit.jupiter.api.Test;
import org.morphix.runtime.OptionalLibrary;

/**
 * Test class for {@link DiagnosticContext}.
 *
 * @author Radu Sebastian LAZIN
 */
class DiagnosticContextTest {

	@Test
	void shouldReturnSlf4jDiagnosticContextWhenSlf4jIsPresent() {
		assertThat(DiagnosticContext.instance().getClass(), equalTo(Slf4jDiagnosticContext.class));
	}

	@Test
	void shouldReturnNoOpDiagnosticContextOnInitialize() {
		DiagnosticContext diagnosticContext = DiagnosticContext.initializeInstance();

		assertThat(diagnosticContext.getClass(), equalTo(DiagnosticContext.class));
	}

	@Test
	void shouldReturnNoOpDiagnosticContextOnInitializeWithNull() {
		DiagnosticContext diagnosticContext = DiagnosticContext.initializeInstance((OptionalLibrary<? extends DiagnosticContext>[]) null);

		assertThat(diagnosticContext.getClass(), equalTo(DiagnosticContext.class));
	}

	@Test
	void shouldReturnNoOpDiagnosticContextOnInitializeWhenLibraryIsNotAvailable() {
		DiagnosticContext diagnosticContext = DiagnosticContext.initializeInstance(OptionalLibrary.notPresent(DummyDiagnosticContext.class));

		assertThat(diagnosticContext.getClass(), equalTo(DiagnosticContext.class));
	}

	@Test
	void shouldReturnNullFromNoOpGet() {
		DiagnosticContext diagnosticContext = new DiagnosticContext();

		assertThat(diagnosticContext.get("traceId"), nullValue());
	}

	@Test
	void shouldIgnoreNoOpPut() {
		DiagnosticContext diagnosticContext = new DiagnosticContext();

		diagnosticContext.put("traceId", "value");

		assertThat(diagnosticContext.get("traceId"), nullValue());
	}

	@Test
	void shouldReturnNullFromNoOpGetCopyOfContextMap() {
		DiagnosticContext diagnosticContext = new DiagnosticContext();

		assertThat(diagnosticContext.getCopyOfContextMap(), nullValue());
	}

	@Test
	void shouldClearContextWhenSetContextReceivesNull() {
		AtomicBoolean cleared = new AtomicBoolean();
		DiagnosticContext diagnosticContext = new DiagnosticContext() {
			@Override
			public void clear() {
				cleared.set(true);
			}
		};

		diagnosticContext.setContext(null);

		assertTrue(cleared.get());
	}

	@Test
	void shouldSetContextMapWhenSetContextReceivesAMap() {
		AtomicBoolean contextMapSet = new AtomicBoolean();
		DiagnosticContext diagnosticContext = new DiagnosticContext() {
			@Override
			public void setContextMap(final Map<String, String> contextMap) {
				contextMapSet.set(true);
			}
		};

		diagnosticContext.setContext(Collections.emptyMap());

		assertTrue(contextMapSet.get());
	}

	static class DummyDiagnosticContext extends DiagnosticContext {
		// empty
	}
}

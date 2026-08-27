package org.apiphany.http;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.hc.client5.http.CircularRedirectException;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SpringRedirectFailureDetector}.
 *
 * @author Radu Sebastian LAZIN
 */
class SpringRedirectFailureDetectorTest {

	@Test
	void shouldReturnTrueForDirectCircularRedirectException() {
		assertTrue(SpringRedirectFailureDetector.isRedirectFailure(new CircularRedirectException("circular redirect")));
	}

	@Test
	void shouldReturnTrueForWrappedCircularRedirectException() {
		CircularRedirectException circular = new CircularRedirectException("circular redirect");
		RuntimeException wrapped = new RuntimeException("wrapped", circular);

		assertTrue(SpringRedirectFailureDetector.isRedirectFailure(wrapped));
	}

	@Test
	void shouldReturnTrueForCircularRedirectInDeepCauseChain() {
		CircularRedirectException circular = new CircularRedirectException("circular redirect");
		RuntimeException inner = new RuntimeException("inner", circular);
		RuntimeException outer = new RuntimeException("outer", inner);

		assertTrue(SpringRedirectFailureDetector.isRedirectFailure(outer));
	}

	@Test
	void shouldNotThrowOnNull() {
		SpringRedirectFailureDetector.isRedirectFailure(null);
	}

	@Test
	void shouldPreventInstantiation() {
		assertDefaultConstructorThrows(SpringRedirectFailureDetector.class);
	}
}

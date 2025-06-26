package org.apiphany;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.ReflectionException;

/**
 * Test class for {@link ApiPredicates}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiPredicatesTest {

	private static final String SOME_VALUE = "someValue";

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		ReflectionException reflectionException = assertThrows(ReflectionException.class, () -> Constructors.IgnoreAccess.newInstance(ApiPredicates.class));
		InvocationTargetException invocationTargetException = JavaObjects.cast(reflectionException.getCause());
		UnsupportedOperationException unsupportedOperationException = JavaObjects.cast(invocationTargetException.getCause());
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldValidateNonEmptyResponse() {
		List<String> list = List.of(SOME_VALUE);

		boolean result = ApiPredicates.responseListIsNotEmpty().test(list);

		assertTrue(result);
	}

	@Test
	void shouldNotValidateEmptyResponse() {
		boolean result = ApiPredicates.responseListIsNotEmpty().test(Collections.emptyList());

		assertFalse(result);
	}

	@Test
	void shouldValidateResponseSize() {
		List<String> list = List.of(SOME_VALUE);

		boolean result = ApiPredicates.responseListHasSize(1).test(list);

		assertTrue(result);
	}

	@Test
	void shouldNotValidateResponseSize() {
		boolean result = ApiPredicates.responseListHasSize(1).test(Collections.emptyList());

		assertFalse(result);
	}

}

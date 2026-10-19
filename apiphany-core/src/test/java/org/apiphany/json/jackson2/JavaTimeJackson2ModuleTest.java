package org.apiphany.json.jackson2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.MemberAccessor;

/**
 * Test class for {@link JavaTimeJackson2Module}.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaTimeJackson2ModuleTest {

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateClass() throws Exception {
		Throwable targetException = null;
		Constructor<JavaTimeJackson2Module> defaultConstructor = JavaTimeJackson2Module.class.getDeclaredConstructor();
		try (MemberAccessor<Constructor<JavaTimeJackson2Module>> ignored = new MemberAccessor<>(null, defaultConstructor)) {
			defaultConstructor.newInstance();
		} catch (InvocationTargetException e) {
			assertThat(e.getTargetException().getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
			targetException = e.getTargetException();
		}
		assertInstanceOf(UnsupportedOperationException.class, targetException);
	}
}

package org.apiphany.test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;

import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.ReflectionException;

/**
 * Assertion methods for tests.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Assertions {

	/**
	 * Verifies that the default constructor of the given class throws an exception.
	 *
	 * @param <T> the type of the exception expected to be thrown by the default constructor
	 *
	 * @param cls the class whose default constructor is to be tested
	 * @return the exception thrown by the default constructor
	 */
	static <T extends Throwable> T assertDefaultConstructorThrows(final Class<?> cls) {
		ReflectionException reflectionException =
				assertThrows(ReflectionException.class, () -> Constructors.IgnoreAccess.newInstance(cls));
		InvocationTargetException invocationTargetException = JavaObjects.cast(reflectionException.getCause());
		return JavaObjects.cast(invocationTargetException.getCause());
	}
}

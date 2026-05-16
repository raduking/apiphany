package org.apiphany.test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.util.function.BooleanSupplier;

import org.morphix.lang.JavaObjects;
import org.morphix.lang.function.ThrowingRunnable;
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

	/**
	 * Verifies that the given assertion holds true and returns a BooleanSupplier that can be used in assertions.
	 *
	 * @param assertion the assertion to be verified
	 * @return a BooleanSupplier that returns true if the assertion holds, false otherwise
	 */
	static BooleanSupplier asserted(final ThrowingRunnable assertion) {
		return () -> {
			try {
				assertion.run();
				return true;
			} catch (AssertionError e) {
				return false;
			} catch (Throwable t) {
				throw new IllegalStateException("Unexpected exception thrown during assertion", t);
			}
		};
	}
}

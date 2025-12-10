package org.apiphany.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.ReflectionException;

/**
 * Utility methods for tests.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Tests {

	public static <T extends Throwable> T verifyDefaultConstructorThrows(final Class<?> cls) {
		ReflectionException reflectionException =
				assertThrows(ReflectionException.class, () -> Constructors.IgnoreAccess.newInstance(cls));
		InvocationTargetException invocationTargetException = JavaObjects.cast(reflectionException.getCause());
		return JavaObjects.cast(invocationTargetException.getCause());
	}

	static Properties loadProperties(final String filePath) {
		try (FileInputStream input = new FileInputStream(filePath)) {
			Properties properties = new Properties();
			properties.load(input);
			return properties;
		} catch (Exception e) {
			throw new RuntimeException("Failed to load project properties", e);
		}
	}
}

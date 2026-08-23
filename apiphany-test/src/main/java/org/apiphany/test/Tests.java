package org.apiphany.test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Utility methods for tests.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Tests {

	/**
	 * Loads project {@link Properties} from the specified file path.
	 *
	 * @param filePath the path to the properties file
	 * @return the loaded properties
	 */
	static Properties loadProperties(final String filePath) {
		try (FileInputStream input = new FileInputStream(filePath)) {
			Properties properties = new Properties();
			properties.load(input);
			return properties;
		} catch (Exception e) {
			throw new IllegalStateException("Failed to load project properties", e);
		}
	}

	/**
	 * Resolves a classpath resource to a real filesystem path. When the resource is packaged inside a jar it is extracted
	 * once to a temporary location since some APIs need an actual file on the default filesystem.
	 *
	 * @param context the class whose classloader resolves the resource
	 * @param name the classpath resource name, with or without a leading slash
	 * @return the filesystem path of the resource
	 */
	static Path resourcePath(final Class<?> context, final String name) {
		return TestResources.path(context, name);
	}
}

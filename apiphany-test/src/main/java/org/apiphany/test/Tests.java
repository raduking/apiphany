package org.apiphany.test;

import java.io.FileInputStream;
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
}

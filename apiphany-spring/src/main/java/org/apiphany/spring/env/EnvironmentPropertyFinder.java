package org.apiphany.spring.env;

import org.apiphany.spring.ApplicationContextCapable;
import org.morphix.lang.Nullables;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;

/**
 * Interface with default <code>getEnvironmentProperty</code> methods which is useful when in a class hierarchy
 * environment properties need to be retrieved from the environment or the application context.
 *
 * @author Radu Sebastian LAZIN
 */
public interface EnvironmentPropertyFinder extends ApplicationContextCapable, EnvironmentCapable {

	/**
	 * Default implementation which returns the {@link Environment} from the application context.
	 */
	@Override
	default Environment getEnvironment() {
		return Nullables.apply(getApplicationContext(), EnvironmentCapable::getEnvironment);
	}

	/**
	 * Returns an environment property or given default value if the property cannot be found.
	 *
	 * @param <T> property return type
	 *
	 * @param key property key
	 * @param type property class
	 * @param defaultValue default value
	 * @return an environment property
	 */
	default <T> T getEnvironmentProperty(final String key, final Class<T> type, final T defaultValue) {
		return EnvironmentProperties.get(getEnvironment(), key, type, defaultValue);
	}
}

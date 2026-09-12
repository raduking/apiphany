package org.apiphany.spring.env;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import org.morphix.lang.JavaObjects;
import org.morphix.lang.function.Predicates;
import org.morphix.reflection.Constructors;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * Utility class for handling Spring environment properties.
 *
 * @author Radu Sebastian LAZIN
 */
public class EnvironmentProperties {

	/**
	 * Sets an environment property.
	 *
	 * @param env configurable environment
	 * @param key property key
	 * @param value property value
	 */
	public static <T> void set(final ConfigurableEnvironment env, final String key, final T value) {
		MutablePropertySources propertySources = env.getPropertySources();
		MapPropertySource propertySource = null;
		for (PropertySource<?> ps : propertySources) {
			if (Objects.equals(ps.getName(), "property.source." + key)) {
				propertySource = JavaObjects.cast(ps);
				break;
			}
		}
		if (null == propertySource) {
			Map<String, Object> properties = new HashMap<>();
			properties.put(key, value);
			propertySource = new MapPropertySource("property.source." + key, properties);
			propertySources.addFirst(propertySource);
		} else {
			propertySource.getSource().put(key, value);
		}
	}

	/**
	 * Returns a property from the Spring environment.
	 *
	 * @param <T> property value type
	 *
	 * @param env environment
	 * @param key property key
	 * @param type property class
	 * @param defaultValue default value if the property is not found
	 * @return environment property
	 */
	public static <T> T get(final Environment env, final String key, final Class<T> type, final T defaultValue) {
		return env.getProperty(key, type, defaultValue);
	}

	/**
	 * Returns all Spring environment properties as a list of strings in the following format:
	 * <code>propertyname=propertyvalue</code>
	 *
	 * @param env environment
	 * @return all Spring environment properties
	 */
	public static List<String> getAll(final ConfigurableEnvironment env) {
		return getAll(env, Predicates.acceptAll());
	}

	/**
	 * Returns all Spring environment properties as a list of strings in the following format:
	 * <code>propertyname=propertyvalue</code>
	 * <p>
	 * TODO: consider implementing with Map instead of list.
	 *
	 * @param env environment
	 * @param predicate property name predicate
	 * @return all Spring environment properties
	 */
	public static List<String> getAll(final ConfigurableEnvironment env, final Predicate<String> predicate) {
		final MutablePropertySources propertySources = env.getPropertySources();
		return StreamSupport.stream(propertySources.spliterator(), false)
				.filter(EnumerablePropertySource.class::isInstance)
				.map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
				.flatMap(Arrays::stream)
				.distinct()
				.filter(predicate)
				.map(prop -> String.join("=", prop, env.getProperty(prop)))
				.toList();
	}

	/**
	 * Private constructor which throws exception if called.
	 */
	private EnvironmentProperties() {
		throw Constructors.unsupportedOperationException();
	}
}

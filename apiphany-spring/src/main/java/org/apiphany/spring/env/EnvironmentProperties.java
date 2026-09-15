package org.apiphany.spring.env;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;
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
	 * Prefix for property source names.
	 */
	private static final String PROPERTY_SOURCE_PREFIX = "property.source.";

	/**
	 * Sets an environment property.
	 *
	 * @param <T> property value type
	 *
	 * @param env configurable environment
	 * @param key property key
	 * @param value property value
	 */
	public static <T> void set(final ConfigurableEnvironment env, final String key, final T value) {
		MutablePropertySources propertySources = env.getPropertySources();
		MapPropertySource propertySource = null;
		for (PropertySource<?> ps : propertySources) {
			if (Objects.equals(ps.getName(), PROPERTY_SOURCE_PREFIX + key)) {
				propertySource = JavaObjects.cast(ps);
				break;
			}
		}
		if (null == propertySource) {
			Map<String, Object> properties = new HashMap<>();
			properties.put(key, value);
			propertySource = new MapPropertySource(PROPERTY_SOURCE_PREFIX + key, properties);
		} else {
			propertySource.getSource().put(key, value);
		}
		propertySources.addFirst(propertySource);
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
	 * Returns a property from the Spring environment or the value supplied by the default value supplier if the property is
	 * missing.
	 *
	 * @param <T> property value type
	 *
	 * @param env environment
	 * @param key property key
	 * @param type property class
	 * @param defaultValueSupplier default value supplier
	 * @return environment property
	 */
	public static <T> T get(final Environment env, final String key, final Class<T> type, final Supplier<T> defaultValueSupplier) {
		return Nullables.nonNullOrDefault(env.getProperty(key, type), defaultValueSupplier);
	}

	/**
	 * Returns all Spring environment properties as a map with property names as keys and property values as values.
	 * Properties with null values are skipped.
	 *
	 * @param env environment
	 * @return all Spring environment properties
	 */
	public static Map<String, String> getMap(final ConfigurableEnvironment env) {
		return getMap(env, Predicates.acceptAll());
	}

	/**
	 * Returns all Spring environment properties as a map with property names as keys and property values as values.
	 * Properties with null values are skipped.
	 *
	 * @param env environment
	 * @param predicate property name predicate
	 * @return all Spring environment properties
	 */
	public static Map<String, String> getMap(final ConfigurableEnvironment env, final Predicate<String> predicate) {
		final MutablePropertySources propertySources = env.getPropertySources();
		final Map<String, String> properties = new LinkedHashMap<>();
		StreamSupport.stream(propertySources.spliterator(), false)
				.filter(EnumerablePropertySource.class::isInstance)
				.map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
				.flatMap(Arrays::stream)
				.filter(predicate)
				.forEach(prop -> {
					String value = env.getProperty(prop);
					if (Objects.nonNull(value)) {
						properties.put(prop, value);
					}
				});
		return properties;
	}

	/**
	 * Returns all Spring environment properties as a list of strings in the following format:
	 * <code>propertyname=propertyvalue</code>
	 *
	 * @param env environment
	 * @return all Spring environment properties
	 */
	public static List<String> getList(final ConfigurableEnvironment env) {
		return getList(env, Predicates.acceptAll());
	}

	/**
	 * Returns all Spring environment properties as a list of strings in the following format:
	 * <code>propertyname=propertyvalue</code>
	 *
	 * @param env environment
	 * @param predicate property name predicate
	 * @return all Spring environment properties
	 */
	public static List<String> getList(final ConfigurableEnvironment env, final Predicate<String> predicate) {
		return getMap(env, predicate).entrySet().stream()
				.map(entry -> String.join("=", entry.getKey(), entry.getValue()))
				.toList();
	}

	/**
	 * Private constructor which throws exception if called.
	 */
	private EnvironmentProperties() {
		throw Constructors.unsupportedOperationException();
	}
}

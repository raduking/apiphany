package org.apiphany.lang.collections;

import static java.util.Collections.emptyMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;

/**
 * Utility methods for maps.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Maps {

	/**
	 * Variation of the above method for Maps.
	 *
	 * @param <K> The map's key type
	 * @param <V> The map's value type
	 *
	 * @param map a provided map
	 * @return the given map if not null, empty map otherwise
	 */
	static <K, V> Map<K, V> safe(final Map<K, V> map) {
		return map == null ? emptyMap() : map;
	}

	/**
	 * Returns the value for the key from the given map and applies the converter on the value if the value is not null
	 * otherwise returns the default value given. Use this method only if the default value is a constant otherwise use the
	 * one with the default value supplier for lazy default value initialization.
	 *
	 * @param <T> The return value type
	 * @param <K> The map's key type
	 * @param <V> The map's value type
	 *
	 * @param map map to get the value from
	 * @param key key for the value
	 * @param converter value converter function
	 * @param defaultValue default value
	 * @return the value from the map
	 */
	static <T, K, V> T getOrDefault(final Map<K, V> map, final K key, final Function<V, T> converter, final T defaultValue) {
		return getOrDefault(map, key, converter, (Supplier<T>) () -> defaultValue);
	}

	/**
	 * Returns the value for the key from the given map and applies the converter on the value if the value is not null
	 * otherwise returns the default value given by the default value supplier.
	 *
	 * @param <T> The return value type
	 * @param <K> The map's key type
	 * @param <V> The map's value type
	 *
	 * @param map map to get the value from
	 * @param key key for the value
	 * @param converter value converter function
	 * @param defaultValueSupplier default value supplier
	 * @return the value from the map
	 */
	static <T, K, V> T getOrDefault(final Map<K, V> map, final K key, final Function<V, T> converter, final Supplier<T> defaultValueSupplier) {
		V value = safe(map).get(key);
		return Nullables.apply(value, converter, defaultValueSupplier);
	}

	/**
	 * Converts all keys for a {@link Map} with the given conversion function.
	 * <p>
	 * If one of the values in the map is also a {@link Map} that map will also be converted.
	 *
	 * @param <K> The map's key type
	 * @param <V> The map's value type
	 *
	 * @param map map to convert keys
	 * @param keyConversionFunction key conversion function
	 * @return map with converted keys
	 */
	static <K, V> Map<K, V> convertKeys(final Map<K, V> map, final UnaryOperator<K> keyConversionFunction) {
		return safe(map).entrySet().stream()
				.collect(Collectors.toMap(entry -> keyConversionFunction.apply(entry.getKey()), entry -> {
					Object value = entry.getValue();
					if (value instanceof Map<?, ?> mapValue
							&& isNotEmpty(mapValue)
							&& mapValue.keySet().stream().allMatch(key -> entry.getKey().getClass().isAssignableFrom(key.getClass()))) {
						value = convertKeys(JavaObjects.cast(mapValue), keyConversionFunction);
					}
					return JavaObjects.cast(value);
				}));
	}

	/**
	 * Returns a new multi value map from a {@link Map}.
	 *
	 * @param <K> The map's key type
	 * @param <V> The map's value type
	 *
	 * @param map entry map
	 * @return a new multi value map
	 */
	static <K, V> Map<K, List<V>> multiValueMap(final Map<K, V> map) {
		if (map == null) {
			return new HashMap<>();
		}
		Map<K, List<V>> multiValueMap = new HashMap<>();
		for (Map.Entry<K, V> entry : map.entrySet()) {
			List<V> value = new ArrayList<>();
			value.add(entry.getValue());
			multiValueMap.put(entry.getKey(), value);
		}
		return multiValueMap;
	}

	/**
	 * Null-safe check if the specified map is empty.
	 * <p>
	 * Null returns true.
	 *
	 * @param map the map to check, may be null
	 * @return true if empty or null
	 */
	static boolean isEmpty(final Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * Null-safe check if the specified map is not empty.
	 * <p>
	 * Null returns false.
	 *
	 * @param map the map to check, may be null
	 * @return true if non-null and non-empty
	 */
	static boolean isNotEmpty(final Map<?, ?> map) {
		return !isEmpty(map);
	}

}

package org.apiphany.spring.collections;

import java.util.List;
import java.util.Map;

import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Constructors;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Utility methods for non standard maps. This class is only here because Spring adds some new multi-value maps.
 * <p>
 * Use {@link org.morphix.lang.collections.Maps} for any other purposes.
 *
 * @author Radu Sebastian LAZIN
 */
public class ExtendedMaps {

	/**
	 * Empty multi value map instance.
	 */
	private static final MultiValueMap<?, ?> EMPTY_MULTI_VALUE_MAP = new LinkedMultiValueMap<>(0);

	/**
	 * Private constructor.
	 */
	private ExtendedMaps() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns an empty multi value map.
	 *
	 * @param <K> key type
	 * @param <V> value type
	 * @return an empty multi value map
	 */
	public static <K, V> MultiValueMap<K, V> emptyMultiValueMap() {
		return JavaObjects.cast(EMPTY_MULTI_VALUE_MAP);
	}

	/**
	 * Returns a new {@link MultiValueMap} from a {@link Map}.
	 *
	 * @param <K> key type
	 * @param <V> value type
	 *
	 * @param map entry map
	 * @return a new multi value map
	 */
	public static <K, V> MultiValueMap<K, V> multiValueMap(final Map<K, List<V>> map) {
		if (null == map || map.isEmpty()) {
			return emptyMultiValueMap();
		}
		MultiValueMap<K, V> multiValueMap = new LinkedMultiValueMap<>();
		for (Map.Entry<K, List<V>> entry : map.entrySet()) {
			multiValueMap.addAll(entry.getKey(), entry.getValue());
		}
		return multiValueMap;
	}

	/**
	 * Returns a new {@link MultiValueMap} from a key and value.
	 *
	 * @param <K> key type
	 * @param <V> value type
	 *
	 * @param key map entry key
	 * @param value map entry value
	 * @return a new multi value map
	 */
	public static <K, V> MultiValueMap<K, V> multiValueMap(final K key, final V value) {
		MultiValueMap<K, V> multiValueMap = new LinkedMultiValueMap<>();
		multiValueMap.add(key, value);
		return multiValueMap;
	}
}

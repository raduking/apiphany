package org.apiphany.lang.collections;

import java.util.Collection;
import java.util.Collections;

/**
 * Utility methods for iterables. This class is intended for {@link Iterable} and {@link Collection} types that can be
 * {@code null} and we want to avoid null checks when iterating on them. For example, if we have a method that returns a
 * list of elements but it can return {@code null} if there are no elements, we can use the {@link #safe(Iterable)}
 * method to avoid null checks when iterating on the list. This way, we can iterate on the list without worrying about
 * null values and we can also avoid potential {@link NullPointerException}s.
 * <p>
 * For more specialized methods for lists and maps, see {@link Lists} and {@link Maps}.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Iterables {

	/**
	 * Returns the iterable given as parameter or an empty iterable if the parameter is {@code null}.
	 *
	 * @param <T> element type
	 *
	 * @param iterable an iterable
	 * @return the iterable given as parameter or an empty iterable if the parameter is null
	 */
	static <T> Iterable<T> safe(final Iterable<T> iterable) {
		return null == iterable ? Collections.emptyList() : iterable;
	}

	/**
	 * Returns the collection given as parameter or an empty collection if the parameter is {@code null}.
	 *
	 * @param <T> element type
	 *
	 * @param collection a collection
	 * @return the collection given as parameter or an empty collection if the parameter is null
	 */
	static <T> Collection<T> safe(final Collection<T> collection) {
		return null == collection ? Collections.emptyList() : collection;
	}
}

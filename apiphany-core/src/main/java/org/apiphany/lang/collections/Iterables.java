package org.apiphany.lang.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.apiphany.lang.Require;

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

	/**
	 * Partitions the given iterable into sublists of the given size. The last sublist may be smaller than the given size if
	 * the total number of elements in the iterable is not a multiple of the size.
	 * <p>
	 * Like all other utility methods of this kind it is not thread safe for speed and brevity.
	 *
	 * @param <T> element type
	 *
	 * @param src an iterable to partition
	 * @param size the size of each partition
	 * @return an iterable of sublists of the given size
	 */
	static <T> Iterable<List<T>> partition(final Iterable<T> src, final int size) {
		Objects.requireNonNull(src, "Source iterable must not be null");
		Require.that(size > 0, "Size must be greater than 0");
		return () -> new Iterator<>() {
			final Iterator<T> it = src.iterator();

			public boolean hasNext() {
				return it.hasNext();
			}

			public List<T> next() {
				if (!it.hasNext()) {
					throw new NoSuchElementException();
				}
				List<T> out = new ArrayList<>(size);
				for (int i = 0; i < size && it.hasNext(); ++i) {
					out.add(it.next());
				}
				return out;
			}
		};
	}
}

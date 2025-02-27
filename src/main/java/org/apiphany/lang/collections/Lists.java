package org.apiphany.lang.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

/**
 * Utility methods for lists which are missing from JDK.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Lists {

	/**
	 * Returns the list given as parameter or an empty list if the parameter is {@code null}.
	 * <p>
	 * Usable for iterating on lists that can be {@code null} without null checks.
	 * <p>
	 * Example:
	 * <pre>
	 * List&lt;String&gt; list;
	 * for (String element : safe(list)) {
	 * 	// ...
	 * }
	 * </pre>
	 * <p>
	 * will not throw a {@link NullPointerException} even if the list is {@code null}.
	 *
	 * @param <T> element type
	 *
	 * @param list a list
	 * @return the list given as parameter or an empty list if the parameter is null
	 */
	public static <T> List<T> safe(final List<T> list) {
		return null == list ? Collections.emptyList() : list;
	}

	/**
	 * Transforms an array to an immutable list. This method returns an immutable list and does the necessary null checks
	 * which {@link List#of(Object...)} does not do. When calling:
	 *
	 * <pre>
	 * var list = List.of(null);
	 * </pre>
	 *
	 * will throw an exception.
	 *
	 * @param <T> element type
	 *
	 * @param a array
	 * @return list with the given arrays elements
	 */
	@SafeVarargs
	static <T> List<T> asList(final T... a) {
		return null != a ? List.of(a) : Collections.emptyList();
	}

	/**
	 * Returns the first element from the given collection, or {@code null} if the list is null or empty.
	 * <p>
	 * Will not throw a {@link NullPointerException} even if the collection is {@code null}.
	 *
	 * @param <T> element type
	 *
	 * @param list a list
	 * @return The first element from the given list, if the list is not null or empty, or null otherwise.
	 */
	static <T> T first(final List<T> list) {
		return CollectionUtils.isNotEmpty(list) ? list.getFirst() : null;
	}

	/**
	 * Returns the first element from the given list, or defaultValue if the list is null or empty or the value returned is
	 * null so that the value returned can only be {@code null} if the default value given is {@code null}.
	 * <p>
	 * Will not throw a {@link NullPointerException} even if the list is {@code null}.
	 *
	 * @param <T> element type
	 *
	 * @param list a list
	 * @param defaultValue default value list doesn't have a first value
	 * @return The first element from the given list, if the list is not null or empty, defaultValue otherwise.
	 */
	static <T> T first(final List<T> list, final T defaultValue) {
		T first = first(list);
		return null != first ? first : defaultValue;
	}

	/**
	 * Returns the last element from the given list, or {@code null} if the list is null or empty.
	 * <p>
	 * Will not throw a {@link NullPointerException} even if the list is {@code null}.
	 *
	 * @param <T> element type
	 *
	 * @param list a list
	 * @return The last element from the given list, if the list is not null or empty, or null otherwise.
	 */
	static <T> T last(final List<T> list) {
		return CollectionUtils.isNotEmpty(list) ? list.getLast() : null;
	}

	/**
	 * Merges two sorted lists. The result list is also sorted. The algorithm has O(n + m) complexity.
	 *
	 * @param <T> element type
	 *
	 * @param sortedList1 first sorted list
	 * @param sortedList2 second sorted list
	 * @return merged sorted list
	 */
	static <T extends Comparable<? super T>> List<T> merge(final List<T> sortedList1, final List<T> sortedList2) {
		if (CollectionUtils.isEmpty(sortedList1)) {
			return sortedList2;
		}
		if (CollectionUtils.isEmpty(sortedList2)) {
			return sortedList1;
		}
		int resultSize = sortedList1.size() + sortedList2.size();
		var result = new ArrayList<T>(resultSize);

		int list1Index = 0;
		int list2Index = 0;
		var list1Iterator = sortedList1.iterator();
		var list2Iterator = sortedList2.iterator();

		for (int i = 0; i < resultSize; ++i) {
			boolean advanceSecond = true;
			if (list1Index == sortedList1.size()) {
				// advance second
			} else if (list2Index == sortedList2.size() || sortedList1.get(list1Index).compareTo(sortedList2.get(list2Index)) < 0) {
				result.add(list1Iterator.next());
				list1Index++;
				advanceSecond = false;
			}
			if (advanceSecond) {
				result.add(list2Iterator.next());
				list2Index++;
			}
		}
		return result;
	}
}

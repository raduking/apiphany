package org.apiphany;

import java.lang.reflect.Constructor;
import java.util.List;

import org.apiphany.lang.collections.Lists;
import org.morphix.reflection.Constructors;

/**
 * API Page class with <code>content</code> list.
 *
 * @param <T> page elements type
 *
 * @author Radu Sebastian LAZIN
 */
public interface ApiPage<T> {

	/**
	 * Returns the content list.
	 *
	 * @return the content list
	 */
	List<T> getContent();

	/**
	 * Creates an {@link ApiPage} object from an API page derived class that has a constructor with content.
	 *
	 * @param <T> content element type
	 * @param <U> API page type
	 *
	 * @param apiPageClass API page class
	 * @param content content list
	 * @return API page object
	 */
	static <T, U extends ApiPage<T>> U of(final Class<U> apiPageClass, final List<T> content) {
		Constructor<U> constructor = Constructors.getDeclaredConstructor(apiPageClass, List.class);
		return Constructors.IgnoreAccess.newInstance(constructor, content);
	}

	/**
	 * Returns the first element of the page.
	 *
	 * @param <T> page element type
	 *
	 * @param page the API page object
	 * @return the first element of the page
	 */
	static <T> T first(final ApiPage<T> page) {
		if (null == page) {
			return null;
		}
		List<T> content = page.getContent();
		return Lists.first(content);
	}

	/**
	 * Returns the last element of the page.
	 *
	 * @param <T> page element type
	 *
	 * @param page the API page object
	 * @return the last element of the page
	 */
	static <T> T last(final ApiPage<T> page) {
		if (null == page) {
			return null;
		}
		List<T> content = page.getContent();
		return Lists.last(content);
	}
}

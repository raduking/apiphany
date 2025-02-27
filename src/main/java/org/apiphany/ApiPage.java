package org.apiphany;

import java.util.List;

import org.apiphany.lang.collections.Lists;

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

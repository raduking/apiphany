package org.apiphany.lang;

/**
 * Generic pair record.
 *
 * @param <L> left value type
 * @param <R> right value type
 * @param left left value
 * @param right right value
 *
 * @author Radu Sebastian LAZIN
 */
public record Pair<L, R>(L left, R right) {

	/**
	 * Factory method to create a new pair with the given left and right values.
	 *
	 * @param <L> left value type
	 * @param <R> right value type
	 *
	 * @param left left value
	 * @param right right value
	 * @return a new pair with the given left and right values
	 */
	public static <L, R> Pair<L, R> of(final L left, final R right) {
		return new Pair<>(left, right);
	}

}
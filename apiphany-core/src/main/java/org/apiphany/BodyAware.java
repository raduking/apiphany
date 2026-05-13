package org.apiphany;

/**
 * Interface for objects that have a body of a specific type. This is typically used in API contexts where messages or
 * responses contain a body that can be accessed through a standardized method.
 *
 * @param <T> the type of the body contained in this object
 *
 * @author Radu Sebastian LAZIN
 */
public interface BodyAware<T> {

	/**
	 * Returns the body of this object.
	 *
	 * @return the body of this object
	 */
	T getBody();
}

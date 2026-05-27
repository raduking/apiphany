package org.apiphany.multipart;

import org.morphix.lang.Ids;
import org.morphix.lang.Ids.UUIDStyle;

/**
 * Represents a boundary string used to separate parts in a multipart message. The boundary is a unique string that is
 * defined in the MIME specification for multipart messages. This class provides a way to generate random boundary
 * strings that can be used when constructing multipart messages.
 *
 * @author Radu Sebastian LAZIN
 */
public class MultipartBoundary {

	/**
	 * The value of the boundary string. This is a unique string that is used to separate parts in a multipart message. The
	 * boundary string must not be {@code null} and should be unique enough to avoid collisions with the content of the
	 * parts.
	 */
	private final String value;

	/**
	 * Constructs a new {@code MultipartBoundary} with the specified value. The value must not be {@code null}.
	 *
	 * @param value the boundary string to use for separating parts in a multipart message
	 * @throws NullPointerException if {@code value} is {@code null}
	 */
	protected MultipartBoundary(final String value) {
		this.value = value;
	}

	/**
	 * Generates a random {@code MultipartBoundary} string. The generated boundary string is a unique string that is
	 * prefixed with "----" followed by a randomly generated UUID without dashes. This method can be used to create boundary
	 * strings that are unlikely to collide with the content of the parts in a multipart message.
	 *
	 * @return a new {@code MultipartBoundary} with a random value
	 */
	public static MultipartBoundary random() {
		return new MultipartBoundary("----" + Ids.generateUUIDString(UUIDStyle.NO_DASHES));
	}

	/**
	 * Retrieves the value of this boundary string. The value is a unique string that is used to separate parts in a
	 * multipart message. This method returns the boundary string that can be used when constructing multipart messages.
	 *
	 * @return the boundary string value
	 */
	public String value() {
		return value;
	}

	/**
	 * Returns a string representation of this {@code MultipartBoundary}. The string representation is the same as the
	 * boundary string value. This method is useful for debugging and logging purposes, as it allows you to easily see the
	 * boundary string when printing the object.
	 *
	 * @return a string representation of this {@code MultipartBoundary}
	 */
	@Override
	public String toString() {
		return value();
	}
}

package org.apiphany;

/**
 * Represents a generic operation status with success/error state and status code.
 * <p>
 * Implementations of this interface provide standardized ways to check operation outcomes and retrieve associated
 * status codes. The interface defines common status checking methods and a constant for unknown status codes.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Status {

	/**
	 * Constant representing an unknown status code. Value: {@value}
	 */
	int UNKNOWN = -1;

	/**
	 * Determines if the status represents a successful operation.
	 *
	 * @return {@code true} if the operation was successful, {@code false} otherwise
	 */
	boolean isSuccess();

	/**
	 * Determines if the status represents an error condition.
	 *
	 * @return {@code true} if the operation resulted in an error, {@code false} otherwise
	 */
	boolean isError();

	/**
	 * Gets the numeric status code associated with this status.
	 * <p>
	 * The meaning of status codes is implementation-dependent. If the status code is not known or applicable,
	 * implementations should return {@link #UNKNOWN}.
	 *
	 * @return the numeric status code, or {@link #UNKNOWN} (-1) if not applicable
	 */
	int getCode();

	/**
	 * Gets a human-readable message associated with this status.
	 * <p>
	 * The content and format of the message are implementation-dependent. It may provide additional details about the
	 * status, such as error descriptions or success confirmations.
	 *
	 * @return a message describing the status, or {@code null} if no message is available
	 */
	String getMessage();

	/**
	 * Return the status message string.
	 *
	 * @param status the status
	 * @return the status message string
	 */
	static String message(final Status status) {
		return null != status
				? "[" + status.getCode() + " " + status.getMessage() + "]"
				: "[unknown status]";
	}

	/**
	 * Return the message string based on the status message and the given message.
	 *
	 * @param status the status
	 * @param message the human-readable message
	 * @return the status message string
	 */
	static String message(final Status status, final String message) {
		return null != message
				? String.join(" ", message(status), message)
				: message(status);
	}
}

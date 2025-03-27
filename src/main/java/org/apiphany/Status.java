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
	static final int UNKNOWN = -1;

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
}

package org.apiphany.lang.accumulator;

import java.io.Serial;

import org.apiphany.lang.Strings;
import org.morphix.lang.JavaObjects;

/**
 * Exception that indicates an error in the accumulator.
 *
 * @author Radu Sebastian LAZIN
 */
public class AccumulatorException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -1040952495776314643L;

	/**
	 * The accumulator that caused the exception.
	 */
	private final transient Accumulator<?> accumulator;

	/**
	 * Constructor.
	 *
	 * @param cause exception cause
	 * @param accumulator accumulator which caused the exception
	 */
	public AccumulatorException(final Throwable cause, final Accumulator<?> accumulator) {
		super(cause);
		this.accumulator = accumulator;
	}

	/**
	 * Constructor.
	 *
	 * @param message exception message
	 * @param cause exception cause
	 * @param accumulator accumulator which caused the exception
	 */
	public AccumulatorException(final String message, final Throwable cause, final Accumulator<?> accumulator) {
		super(message, cause);
		this.accumulator = accumulator;
	}

	/**
	 * Returns the accumulator that caused the exception.
	 *
	 * @param <T> accumulated element type
	 * @return the accumulator that caused the exception
	 */
	public <T> Accumulator<T> getAccumulator() {
		return JavaObjects.cast(accumulator);
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		if (accumulator.isEmpty()) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		for (Object object : accumulator.getInformationList()) {
			result.append("Accumulated exception: ")
					.append(Strings.safeToString(object))
					.append(System.lineSeparator());
		}
		return result.toString();
	}
}

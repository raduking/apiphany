package org.apiphany.test;

/**
 * ANSI colors for console output.
 *
 * @author Radu Sebastian LAZIN
 */
public enum ANSIColor {

	/**
	 * Reset color.
	 */
	RESET("\u001B[0m"),

	/**
	 * Black color.
	 */
	BLACK("\u001B[30m"),

	/**
	 * Red color.
	 */
	RED("\u001B[31m"),

	/**
	 * Green color.
	 */
	GREEN("\u001B[32m"),

	/**
	 * Yellow color.
	 */
	YELLOW("\u001B[33m"),

	/**
	 * Blue color.
	 */
	BLUE("\u001B[34m"),

	/**
	 * Purple color.
	 */
	PURPLE("\u001B[35m"),

	/**
	 * Cyan color.
	 */
	CYAN("\u001B[36m"),

	/**
	 * White color.
	 */
	WHITE("\u001B[37m");

	/**
	 * ANSI code.
	 */
	private final String code;

	/**
	 * Constructor.
	 *
	 * @param code ANSI code
	 */
	ANSIColor(final String code) {
		this.code = code;
	}

	/**
	 * Returns the ANSI code.
	 *
	 * @return ANSI code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Returns the ANSI code as string.
	 *
	 * @return ANSI code as string
	 */
	@Override
	public String toString() {
		return getCode();
	}
}

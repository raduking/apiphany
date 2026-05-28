package org.apiphany.lang;

/**
 * Enum representing the parsing mode for API definitions.
 *
 * @author Radu Sebastian LAZIN
 */
public enum ParsingMode {

	/**
	 * Strict parsing mode, where the parser will throw an exception if it encounters any parsing error.
	 */
	STRICT,

	/**
	 * Lenient parsing mode, where the parser will ignore any parsing error and try to parse as much as possible.
	 */
	LENIENT;

	/**
	 * The default parsing mode (STRICT).
	 */
	public static final ParsingMode DEFAULT = STRICT;
}

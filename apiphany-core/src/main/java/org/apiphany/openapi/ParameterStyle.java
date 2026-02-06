package org.apiphany.openapi;

/**
 * Enumeration representing the different styles of parameter serialization as defined in the OpenAPI Specification.
 *
 * @author Radu Sebastian LAZIN
 */
public enum ParameterStyle {

	/**
	 * The default style for query parameters. This style serializes multiple values as a comma-separated list. For example,
	 * {@code "color=red,green,blue"}.
	 */
	FORM,

	/**
	 * The default style for path parameters. This style does not use any delimiters and concatenates multiple values
	 * directly. For example, {@code "color=redgreenblue"}.
	 */
	SIMPLE,

	/**
	 * The default style for header parameters. This style does not use any delimiters and concatenates multiple values
	 * directly. For example, {@code "color=redgreenblue"}.
	 */
	MATRIX,

	/**
	 * The default style for cookie parameters. This style does not use any delimiters and concatenates multiple values
	 * directly. For example, {@code "color=redgreenblue"}.
	 */
	SPACE_DELIMITED,

	/**
	 * The default style for query parameters when the {@code explode} property is set to {@code true}. This style
	 * serializes multiple values as a pipe-separated list. For example, {@code "color=red|green|blue"}.
	 */
	PIPE_DELIMITED,

	/**
	 * The default style for query parameters when the {@code explode} property is set to {@code true}. This style allows
	 * multiple parameter instances instead of a single parameter with multiple values. For example, for a query parameter
	 * named {@code "color"} with values {@code "red"}, {@code "green"}, and {@code "blue"}, this style would serialize it
	 * as {@code "color=red&color=green&color=blue"}.
	 */
	DEEP_OBJECT
}

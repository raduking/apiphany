package org.apiphany.openapi;

/**
 * Enumeration of the allowed values for the {@code style} field of a Parameter Object as defined in the OpenAPI
 * Specification (3.0.x and 3.1.x).
 * <p>
 * The {@code style} determines how array and object parameter values are serialized into the URI (path/query) or
 * headers/cookies. The actual behavior also depends on:
 * <ul>
 * <li>the parameter location ({@code in})</li>
 * <li>the {@code explode} flag (whether values are repeated or delimited)</li>
 * <li>the parameter type (primitive / array / object)</li>
 * </ul>
 * Not all styles are valid for every location.
 * <p>
 * Official OpenAPI style values (lower-case):
 * <ul>
 * <li>{@code form} — default for query and cookie</li>
 * <li>{@code simple} — default for path and header</li>
 * <li>{@code matrix}</li>
 * <li>{@code spaceDelimited}</li>
 * <li>{@code pipeDelimited}</li>
 * <li>{@code deepObject}</li>
 * </ul>
 *
 * @see <a href="https://spec.openapis.org/oas/v3.1.0#style-values">OpenAPI 3.1 – Style Values</a>
 * @see <a href="https://swagger.io/docs/specification/serialization/">Parameter Serialization – Swagger Docs</a>
 *
 * @author Radu Sebastian LAZIN (original), updated/corrected
 */
public enum ParameterStyle {

	/**
	 * Form-style query expansion — ampersand-separated (default for <b>query</b> and <b>cookie</b>).
	 * <ul>
	 * <li>explode = true (default):
	 *
	 * <pre>
	 * color=red&amp;color=green&amp;color=blue
	 * </pre>
	 *
	 * </li>
	 * <li>explode = false:
	 *
	 * <pre>
	 * color=red,green,blue
	 * </pre>
	 *
	 * </li>
	 * </ul>
	 */
	FORM,

	/**
	 * Comma-separated values without any prefix (default for <b>path</b> and <b>header</b> parameters).
	 * <p>
	 * Example (array):
	 *
	 * <pre>
	 * /users/1,2,3
	 * </pre>
	 *
	 * or
	 *
	 * <pre>
	 * Accept: text/csv
	 * </pre>
	 * <p>
	 * Also used for exploded objects in some cases.
	 */
	SIMPLE,

	/**
	 * Space-separated array values (only for <b>query</b> parameters, usually with explode = false).
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * color=red%20green%20blue
	 * </pre>
	 *
	 * (note: spaces are %-encoded)
	 */
	SPACE_DELIMITED,

	/**
	 * Pipe-separated array values (only for <b>query</b> parameters, usually with explode = false).
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * color = red | green | blue
	 * </pre>
	 */
	PIPE_DELIMITED,

	/**
	 * Path-style / matrix parameters — semicolon-prefixed (mainly for <b>path</b> parameters).
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>explode = false:
	 *
	 * <pre>
	 * /map/point;x=50;y=20
	 * </pre>
	 *
	 * </li>
	 * <li>explode = true:
	 *
	 * <pre>
	 * /map/point;x=50;y=20
	 * </pre>
	 *
	 * (same in most cases)</li>
	 * </ul>
	 */
	MATRIX,

	/**
	 * Deep object notation — renders object properties as separate parameters with property names in brackets.
	 * <p>
	 * Mainly for <b>query</b> parameters representing objects.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * point[x]=50&amp;point[y]=20
	 * </pre>
	 * <p>
	 * Note: behavior for deeply nested objects or arrays is often undefined / implementation-specific.
	 */
	DEEP_OBJECT
}

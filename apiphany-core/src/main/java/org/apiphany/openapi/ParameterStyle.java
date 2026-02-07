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
 * <li>{@code form} — default for query & cookie</li>
 * <li>{@code simple} — default for path & header</li>
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
	 * <li>explode = true (default): <code>color=red&color=green&color=blue</code></li>
	 * <li>explode = false: <code>color=red,green,blue</code></li>
	 * </ul>
	 */
	FORM,

	/**
	 * Comma-separated values without any prefix (default for <b>path</b> and <b>header</b> parameters).
	 * <p>
	 * Example (array): <code>/users/1,2,3</code> or <code>Accept: text/csv</code>
	 * <p>
	 * Also used for exploded objects in some cases.
	 */
	SIMPLE,

	/**
	 * Space-separated array values (only for <b>query</b> parameters, usually with explode = false).
	 * <p>
	 * Example: <code>color=red%20green%20blue</code> (note: spaces are %-encoded)
	 */
	SPACE_DELIMITED,

	/**
	 * Pipe-separated array values (only for <b>query</b> parameters, usually with explode = false).
	 * <p>
	 * Example: <code>color=red|green|blue</code>
	 */
	PIPE_DELIMITED,

	/**
	 * Path-style / matrix parameters — semicolon-prefixed (mainly for <b>path</b> parameters).
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>explode = false: <code>/map/point;x=50;y=20</code></li>
	 * <li>explode = true: <code>/map/point;x=50;y=20</code> (same in most cases)</li>
	 * </ul>
	 */
	MATRIX,

	/**
	 * Deep object notation — renders object properties as separate parameters with property names in brackets.
	 * <p>
	 * Mainly for <b>query</b> parameters representing objects.
	 * <p>
	 * Example: <code>point[x]=50&point[y]=20</code>
	 * <p>
	 * Note: behavior for deeply nested objects or arrays is often undefined / implementation-specific.
	 */
	DEEP_OBJECT
}

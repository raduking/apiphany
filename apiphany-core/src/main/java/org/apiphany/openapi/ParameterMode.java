package org.apiphany.openapi;

/**
 * Enumeration representing the different parameter modes as defined in the OpenAPI Specification.
 *
 * @author Radu Sebastian LAZIN
 */
public enum ParameterMode {

	/**
	 * The parameter is serialized as multiple parameters with the same name (e.g.,
	 * {@code "color=red&color=green&color=blue"}).
	 */
	EXPLODE,

	/**
	 * The parameter is serialized as a single parameter with multiple values (e.g., {@code "color=red,green,blue"} or
	 * {@code "color=red green blue"}, etc.).
	 */
	JOINED;

}

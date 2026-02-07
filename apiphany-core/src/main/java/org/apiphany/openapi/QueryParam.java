package org.apiphany.openapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify query parameter serialization style and mode as per OpenAPI Specification.
 *
 * @author Radu Sebastian LAZIN
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface QueryParam {

	/**
	 * The serialization style of the query parameter.
	 *
	 * @return the parameter style
	 */
	ParameterStyle style() default ParameterStyle.FORM;

	/**
	 * The serialization mode of the query parameter.
	 *
	 * @return the parameter mode
	 */
	ParameterMode mode() default ParameterMode.EXPLODE;
}

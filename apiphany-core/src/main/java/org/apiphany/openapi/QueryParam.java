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
	 * The strategy for serializing multiple values of the query parameter. If this is not specified, the serialization
	 * strategy will be determined based on the parameter {@link #style()} and {@link #mode()}. If the strategy is
	 * specified, it will take precedence over the style and mode settings.
	 *
	 * @return the multi-value serialization strategy, default is an empty string which indicates that the strategy should
	 * be determined based on style and mode
	 */
	String strategy() default "";

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

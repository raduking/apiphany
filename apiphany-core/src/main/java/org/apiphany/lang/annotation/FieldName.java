package org.apiphany.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies an alternative name to use for a field or method when it is serialized, deserialized, or otherwise
 * processed.
 * <p>
 * This can be used to align Java naming conventions with external formats (e.g., snake_case, kebab-case, or legacy
 * field names).
 * </p>
 *
 * <pre>
 * public class User {
 *
 *     {@code @FieldName("user_name")}
 *     private String username;
 * }
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
public @interface FieldName {

	/**
	 * Returns the field name.
	 *
	 * @return the field name
	 */
	String value();

}

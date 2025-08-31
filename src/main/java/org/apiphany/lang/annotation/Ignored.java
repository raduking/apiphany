package org.apiphany.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field or method to be excluded from serialization, deserialization, or other reflection-based processing
 * performed by this library.
 * <p>
 * This annotation is useful when certain members should not be exposed to external representations (such as JSON,
 * properties maps, or logs), or when they are only used internally.
 * </p>
 *
 * <pre>
 * public class User {
 *
 *     private String username;
 *
 *     {@code @Ignored}
 *     private String password;
 * }
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Ignored {

	// empty
}

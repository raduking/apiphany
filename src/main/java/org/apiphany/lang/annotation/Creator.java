package org.apiphany.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a constructor or static factory method as the preferred way to create an instance of a class during automated
 * instantiation or deserialization.
 *
 * <p>
 * This allows frameworks or libraries that support annotation-driven object creation to identify which constructor or
 * factory method to use.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * public class ErrorResponse {
 *
 *     {@code @Creator}
 *     public ErrorResponse(
 *         {@code @FieldName("error")} String error,
 *         {@code @FieldName("error_description")} String description
 *     ) {
 *         this.error = error;
 *         this.errorDescription = description;
 *     }
 * }
 * </pre>
 *
 * or
 *
 * <pre>
 *  {@code @Creator}
 *  public static OAuth2ErrorCode fromString(final String value) {
 *      // ...
 *  }
 * </pre>
 *
 * <p>
 * This annotation is a marker and does not contain any fields or values.
 * </p>
 *
 * @author Radu Sebastian LAZIN
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD })
public @interface Creator {

	// marker annotation, no fields needed

}

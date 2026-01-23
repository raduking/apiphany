package org.apiphany.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the order of fields in a class when processed for serialization, deserialization, or other reflection-based
 * operations.
 * <p>
 * The value should list field names in the desired order. Fields not listed may be processed in any order.
 * </p>
 *
 * <pre>
 * {@code @FieldOrder}({
 *     "id",
 *     "username",
 *     "email"
 * })
 * public class User {
 *     private String id;
 *     private String email;
 *     private String username;
 * }
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FieldOrder {

	/**
	 * Returns an array with the field order.
	 *
	 * @return an array with the field order
	 */
	String[] value();

}

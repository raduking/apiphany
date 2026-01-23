package org.apiphany.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a single method or field as the value representation of an object during serialization. When present, the
 * annotated member will be used to produce the object's serialized form instead of its fields or properties.
 *
 * <p>
 * This is similar in intent to Jackson's {@code @JsonValue}, but is defined in a library-agnostic way so that it can be
 * interpreted by any serialization framework or custom mapper that supports it.
 * </p>
 *
 * <h2>Usage example</h2>
 *
 * <pre>
 * public enum Status {
 *
 *     OK("ok"),
 *     ERROR("error");
 *
 *     private final String code;
 *
 *     Status(String code) {
 *         this.code = code;
 *     }
 *
 *     {@code @AsValue}
 *     public String getCode() {
 *         return code;
 *     }
 * }
 *
 * // serializes to "ok" or "error" instead of a full object
 * </pre>
 *
 * <p>
 * Rules:
 * </p>
 * <ul>
 * <li>Only one member per type should be annotated with {@code @AsValue}.</li>
 * <li>Can be placed on a method or field.</li>
 * <li>If multiple members are annotated, the behavior is undefined.</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AsValue {

	// empty
}

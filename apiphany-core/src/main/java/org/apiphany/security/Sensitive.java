package org.apiphany.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.morphix.reflection.Constructors;

/**
 * Annotation to mark sensitive fields.
 *
 * @author Radu Sebastian LAZIN
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Sensitive {

	/**
	 * Defines the strategy to be used for handling sensitive data. The default strategy is {@link Visibility#HIDDEN}.
	 *
	 * @return the strategy to be used for handling sensitive data
	 */
	Visibility visibility() default Visibility.HIDDEN;

	/**
	 * Defines value constants for handling sensitive data.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Value {

		/**
		 * Constant representing redacted sensitive data. This value can be used in outputs, logs, or any other context where
		 * the actual value of sensitive data should not be exposed.
		 */
		public static final String REDACTED = "-REDACTED-";

		/**
		 * Private constructor to prevent instantiation of this utility class.
		 */
		private Value() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * Enum representing the visibility strategies for handling sensitive data. The {@link Visibility#HIDDEN} strategy hides
	 * the sensitive data by not including it in the output, while the {@link Visibility#REDACTED} strategy redacts the
	 * sensitive data by replacing it with the string defined in {@link Value#REDACTED}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public enum Visibility {

		/**
		 * Hides the sensitive data by not including it in the output.
		 */
		HIDDEN,

		/**
		 * Redacts the sensitive data by replacing it with the string defined in {@link Value#REDACTED}.
		 */
		REDACTED
	}
}

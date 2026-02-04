package org.apiphany.json.jackson2;

import java.io.Serial;

import org.apiphany.security.Sensitive;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

/**
 * Custom Jackson {@link com.fasterxml.jackson.databind.AnnotationIntrospector} to handle fields annotated with
 * {@link Sensitive}.
 * <p>
 * When enabled, any field marked with {@link Sensitive} will be serialized as write-only, meaning it will be ignored
 * during JSON serialization but can still be populated during deserialization. This allows sensitive information such
 * as passwords or tokens to be hidden in serialized output.
 * <p>
 * Use {@link #hideSensitive()} to get an instance that hides sensitive fields, or {@link #allowSensitive()} to get an
 * instance that allows all fields to be serialized normally.
 *
 * @author Radu Sebastian LAZIN
 */
public class SensitiveAnnotationIntrospector extends NopAnnotationIntrospector {

	/**
	 * The serial version UID required by serializable classes.
	 */
	@Serial
	private static final long serialVersionUID = -1476598423097529275L;

	/**
	 * Flag indicating whether sensitive fields should be hidden (write-only).
	 */
	private final boolean enabled;

	/**
	 * Constructs a new {@link SensitiveAnnotationIntrospector}.
	 *
	 * @param enabled flag indicating what to do with {@link Sensitive} fields.
	 */
	public SensitiveAnnotationIntrospector(final boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns a singleton instance that hides sensitive fields.
	 *
	 * @return an introspector configured to hide sensitive fields
	 */
	public static SensitiveAnnotationIntrospector hideSensitive() {
		return InstanceHolder.HIDE;
	}

	/**
	 * Returns a singleton instance that allows sensitive fields to be serialized normally.
	 *
	 * @return an introspector configured to allow all fields
	 */
	public static SensitiveAnnotationIntrospector allowSensitive() {
		return InstanceHolder.ALLOW;
	}

	/**
	 * @see AnnotationIntrospector#findPropertyAccess(Annotated)
	 */
	@Override
	public Access findPropertyAccess(final Annotated ann) {
		if (enabled && ann.hasAnnotation(Sensitive.class)) {
			return JsonProperty.Access.WRITE_ONLY;
		}
		return super.findPropertyAccess(ann);
	}

	/**
	 * Holder for singleton instances to avoid unnecessary object creation.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * Singleton instance configured to hide sensitive fields.
		 */
		private static final SensitiveAnnotationIntrospector HIDE = new SensitiveAnnotationIntrospector(true);

		/**
		 * Singleton instance configured to allow all fields.
		 */
		private static final SensitiveAnnotationIntrospector ALLOW = new SensitiveAnnotationIntrospector(false);
	}
}

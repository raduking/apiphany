package org.apiphany.json.jackson3;

import java.io.Serial;

import org.apiphany.security.Sensitive;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.NopAnnotationIntrospector;

/**
 * Custom Jackson 3 {@link com.fasterxml.jackson.databind.AnnotationIntrospector} to handle fields annotated with
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
public class SensitiveJackson3AnnotationIntrospector extends NopAnnotationIntrospector {

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
	 * Constructs a new {@link SensitiveJackson3AnnotationIntrospector}.
	 *
	 * @param enabled flag indicating what to do with {@link Sensitive} fields.
	 */
	protected SensitiveJackson3AnnotationIntrospector(final boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns a singleton instance that hides sensitive fields.
	 *
	 * @return an introspector configured to hide sensitive fields
	 */
	public static SensitiveJackson3AnnotationIntrospector hideSensitive() {
		return InstanceHolder.HIDE;
	}

	/**
	 * Returns a singleton instance that allows sensitive fields to be serialized normally.
	 *
	 * @return an introspector configured to allow all fields
	 */
	public static SensitiveJackson3AnnotationIntrospector allowSensitive() {
		return InstanceHolder.ALLOW;
	}

	/**
	 * @see AnnotationIntrospector#findPropertyAccess(MapperConfig, Annotated)
	 */
	@Override
	public Access findPropertyAccess(final MapperConfig<?> config, final Annotated ann) {
		if (enabled && ann.hasAnnotation(Sensitive.class)) {
			return JsonProperty.Access.WRITE_ONLY;
		}
		return super.findPropertyAccess(config, ann);
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
		private static final SensitiveJackson3AnnotationIntrospector HIDE = new SensitiveJackson3AnnotationIntrospector(true);

		/**
		 * Singleton instance configured to allow all fields.
		 */
		private static final SensitiveJackson3AnnotationIntrospector ALLOW = new SensitiveJackson3AnnotationIntrospector(false);
	}
}

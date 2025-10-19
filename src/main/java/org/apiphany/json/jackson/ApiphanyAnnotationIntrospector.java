package org.apiphany.json.jackson;

import java.io.Serializable;

import org.apiphany.lang.annotation.AsValue;
import org.apiphany.lang.annotation.FieldName;
import org.apiphany.lang.annotation.FieldOrder;
import org.apiphany.lang.annotation.Ignored;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

/**
 * A Jackson {@link AnnotationIntrospector} that adapts neutral field annotations ({@link FieldName}, {@link Ignored},
 * {@link FieldOrder}) into Jackson's property model.
 *
 * This allows your domain classes to remain independent of Jackson while still benefiting from annotation-driven
 * serialization.
 */
public class ApiphanyAnnotationIntrospector extends NopAnnotationIntrospector { // NOSONAR - singleton needed

	/**
	 * Serial version UID needed for {@link Serializable} objects.
	 */
	private static final long serialVersionUID = -7229112614741239569L;

	/**
	 * Holder for singleton instances to avoid unnecessary object creation.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * Singleton instance configured to hide sensitive fields.
		 */
		private static final ApiphanyAnnotationIntrospector INSTANCE = new ApiphanyAnnotationIntrospector();
	}

	/**
	 * Hide constructor.
	 */
	protected ApiphanyAnnotationIntrospector() {
		// empty
	}

	/**
	 * Returns the singleton instance.
	 *
	 * @return the singleton instance
	 */
	public static ApiphanyAnnotationIntrospector getInstance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * @see #findNameForSerialization(Annotated)
	 */
	@Override
	public PropertyName findNameForSerialization(final Annotated a) {
		FieldName ann = _findAnnotation(a, FieldName.class);
		if (ann != null) {
			return PropertyName.construct(ann.value());
		}
		return super.findNameForSerialization(a);
	}

	/**
	 * @see #findNameForDeserialization(Annotated)
	 */
	@Override
	public PropertyName findNameForDeserialization(final Annotated a) {
		FieldName ann = _findAnnotation(a, FieldName.class);
		if (ann != null) {
			return PropertyName.construct(ann.value());
		}
		return super.findNameForDeserialization(a);
	}

	/**
	 * @see #findPropertyAccess(Annotated)
	 */
	@Override
	public JsonProperty.Access findPropertyAccess(final Annotated a) {
		if (_hasAnnotation(a, Ignored.class)) {
			// exclude from serialization but allow deserialization
			return JsonProperty.Access.WRITE_ONLY;
		}
		return super.findPropertyAccess(a);
	}

	/**
	 * @see #findSerializationPropertyOrder(AnnotatedClass)
	 */
	@Override
	public String[] findSerializationPropertyOrder(final AnnotatedClass ac) {
		FieldOrder ann = _findAnnotation(ac, FieldOrder.class);
		if (ann != null) {
			return ann.value();
		}
		return super.findSerializationPropertyOrder(ac);
	}

	/**
	 * @see #hasAsValue(Annotated)
	 */
	@Override
	public Boolean hasAsValue(final Annotated a) {
		// tells Jackson that this method/field should be used as the "value" during serialization
		if (a.hasAnnotation(AsValue.class)) {
			return Boolean.TRUE;
		}
		return super.hasAsValue(a);
	}
}

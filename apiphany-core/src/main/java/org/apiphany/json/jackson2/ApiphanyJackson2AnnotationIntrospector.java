package org.apiphany.json.jackson2;

import java.io.Serial;
import java.io.Serializable;

import org.apiphany.lang.annotation.AsValue;
import org.apiphany.lang.annotation.Creator;
import org.apiphany.lang.annotation.FieldName;
import org.apiphany.lang.annotation.FieldOrder;
import org.apiphany.lang.annotation.Ignored;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

/**
 * A Jackson 2 {@link AnnotationIntrospector} that adapts neutral field annotations ({@link FieldName}, {@link Ignored},
 * {@link FieldOrder}) into Jackson's property model.
 * <p>
 * This allows your domain classes to remain independent of Jackson 2 while still benefiting from annotation-driven
 * serialization.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiphanyJackson2AnnotationIntrospector extends NopAnnotationIntrospector { // NOSONAR - singleton needed

	/**
	 * Serial version UID needed for {@link Serializable} objects.
	 */
	@Serial
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
		private static final ApiphanyJackson2AnnotationIntrospector INSTANCE = new ApiphanyJackson2AnnotationIntrospector();
	}

	/**
	 * Hide constructor.
	 */
	protected ApiphanyJackson2AnnotationIntrospector() {
		// empty
	}

	/**
	 * Returns the singleton instance.
	 *
	 * @return the singleton instance
	 */
	public static ApiphanyJackson2AnnotationIntrospector getInstance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * @see AnnotationIntrospector#findNameForSerialization(Annotated)
	 */
	@Override
	public PropertyName findNameForSerialization(final Annotated annotated) {
		FieldName fieldName = _findAnnotation(annotated, FieldName.class);
		if (null != fieldName) {
			return PropertyName.construct(fieldName.value());
		}
		return super.findNameForSerialization(annotated);
	}

	/**
	 * @see AnnotationIntrospector#findNameForDeserialization(Annotated)
	 */
	@Override
	public PropertyName findNameForDeserialization(final Annotated annotated) {
		FieldName fieldName = _findAnnotation(annotated, FieldName.class);
		if (null != fieldName) {
			return PropertyName.construct(fieldName.value());
		}
		return super.findNameForDeserialization(annotated);
	}

	/**
	 * @see AnnotationIntrospector#findPropertyAccess(Annotated)
	 */
	@Override
	public JsonProperty.Access findPropertyAccess(final Annotated annotated) {
		if (_hasAnnotation(annotated, Ignored.class)) {
			// exclude from serialization but allow deserialization
			return JsonProperty.Access.WRITE_ONLY;
		}
		return super.findPropertyAccess(annotated);
	}

	/**
	 * @see AnnotationIntrospector#findSerializationPropertyOrder(AnnotatedClass)
	 */
	@Override
	public String[] findSerializationPropertyOrder(final AnnotatedClass annotatedClass) {
		FieldOrder fieldOrder = _findAnnotation(annotatedClass, FieldOrder.class);
		if (null != fieldOrder) {
			return fieldOrder.value();
		}
		return super.findSerializationPropertyOrder(annotatedClass);
	}

	/**
	 * @see AnnotationIntrospector#hasAsValue(Annotated)
	 */
	@Override
	public Boolean hasAsValue(final Annotated annotated) {
		// tells Jackson that this method/field should be used as the "value" during serialization
		if (annotated.hasAnnotation(AsValue.class)) {
			return Boolean.TRUE;
		}
		return super.hasAsValue(annotated);
	}

	/**
	 * @see AnnotationIntrospector#findCreatorAnnotation(MapperConfig, Annotated)
	 */
	@Override
	public JsonCreator.Mode findCreatorAnnotation(final MapperConfig<?> config, final Annotated annotated) {
		if (annotated.hasAnnotation(Creator.class)) {
			return JsonCreator.Mode.DEFAULT;
		}
		return super.findCreatorAnnotation(config, annotated);
	}
}

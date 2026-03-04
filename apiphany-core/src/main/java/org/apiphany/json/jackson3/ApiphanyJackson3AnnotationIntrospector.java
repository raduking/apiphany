package org.apiphany.json.jackson3;

import java.io.Serial;
import java.io.Serializable;

import org.apiphany.lang.annotation.AsValue;
import org.apiphany.lang.annotation.Creator;
import org.apiphany.lang.annotation.FieldName;
import org.apiphany.lang.annotation.FieldOrder;
import org.apiphany.lang.annotation.Ignored;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.PropertyName;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.introspect.NopAnnotationIntrospector;

/**
 * A Jackson 3 {@link AnnotationIntrospector} that adapts neutral field annotations ({@link FieldName}, {@link Ignored},
 * {@link FieldOrder}) into Jackson's property model.
 * <p>
 * This allows your domain classes to remain independent of Jackson 3 while still benefiting from annotation-driven
 * serialization.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiphanyJackson3AnnotationIntrospector extends NopAnnotationIntrospector { // NOSONAR - singleton needed

	/**
	 * Serial version UID needed for {@link Serializable} objects.
	 */
	@Serial
	private static final long serialVersionUID = -5405566553991118163L;

	/**
	 * Holder for singleton instances to avoid unnecessary object creation.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * Singleton instance configured to hide sensitive fields.
		 */
		private static final ApiphanyJackson3AnnotationIntrospector INSTANCE = new ApiphanyJackson3AnnotationIntrospector();
	}

	/**
	 * Hide constructor.
	 */
	protected ApiphanyJackson3AnnotationIntrospector() {
		// empty
	}

	/**
	 * Returns the singleton instance.
	 *
	 * @return the singleton instance
	 */
	public static ApiphanyJackson3AnnotationIntrospector getInstance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * @see AnnotationIntrospector#findNameForSerialization(MapperConfig, Annotated)
	 */
	@Override
	public PropertyName findNameForSerialization(final MapperConfig<?> config, final Annotated annotated) {
		FieldName fieldName = _findAnnotation(annotated, FieldName.class);
		if (null != fieldName) {
			return PropertyName.construct(fieldName.value());
		}
		return super.findNameForSerialization(config, annotated);
	}

	/**
	 * @see AnnotationIntrospector#findNameForDeserialization(MapperConfig, Annotated)
	 */
	@Override
	public PropertyName findNameForDeserialization(final MapperConfig<?> config, final Annotated annotated) {
		FieldName fieldName = _findAnnotation(annotated, FieldName.class);
		if (null != fieldName) {
			return PropertyName.construct(fieldName.value());
		}
		return super.findNameForDeserialization(config, annotated);
	}

	/**
	 * @see AnnotationIntrospector#findPropertyAccess(MapperConfig, Annotated)
	 */
	@Override
	public Access findPropertyAccess(final MapperConfig<?> config, final Annotated annotated) {
		if (_hasAnnotation(annotated, Ignored.class)) {
			// exclude from serialization but allow deserialization
			return JsonProperty.Access.WRITE_ONLY;
		}
		return super.findPropertyAccess(config, annotated);
	}

	/**
	 * @see AnnotationIntrospector#findSerializationPropertyOrder(MapperConfig, AnnotatedClass)
	 */
	@Override
	public String[] findSerializationPropertyOrder(final MapperConfig<?> config, final AnnotatedClass annotatedClass) {
		FieldOrder fieldOrder = _findAnnotation(annotatedClass, FieldOrder.class);
		if (null != fieldOrder) {
			return fieldOrder.value();
		}
		return super.findSerializationPropertyOrder(config, annotatedClass);
	}

	/**
	 * @see AnnotationIntrospector#hasAsValue(MapperConfig, Annotated)
	 */
	@Override
	public Boolean hasAsValue(final MapperConfig<?> config, final Annotated annotated) {
		// tells Jackson that this method/field should be used as the "value" during serialization
		if (annotated.hasAnnotation(AsValue.class)) {
			return Boolean.TRUE;
		}
		return super.hasAsValue(config, annotated);
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

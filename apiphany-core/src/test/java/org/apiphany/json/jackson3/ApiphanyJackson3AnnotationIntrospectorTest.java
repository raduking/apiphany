package org.apiphany.json.jackson3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apiphany.lang.annotation.AsValue;
import org.apiphany.lang.annotation.Creator;
import org.apiphany.lang.annotation.FieldName;
import org.apiphany.lang.annotation.FieldOrder;
import org.apiphany.lang.annotation.Ignored;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import tools.jackson.databind.PropertyName;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.AnnotatedClass;

/**
 * Test class for {@link ApiphanyJackson3AnnotationIntrospector}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiphanyJackson3AnnotationIntrospectorTest {

	private static final String FIELD_NAME = "fieldName";

	private ApiphanyJackson3AnnotationIntrospector annotationIntrospector = ApiphanyJackson3AnnotationIntrospector.getInstance();

	@Test
	void shouldReturnPropertyNameForFieldNameAnnotationOnSerialization() {
		Annotated annotated = mock(Annotated.class);
		FieldName fieldNameAnnotation = mock(FieldName.class);
		doReturn(FIELD_NAME).when(fieldNameAnnotation).value();
		doReturn(fieldNameAnnotation).when(annotated).getAnnotation(FieldName.class);
		MapperConfig<?> mapperConfig = mock(MapperConfig.class);

		PropertyName propertyName = annotationIntrospector.findNameForSerialization(mapperConfig, annotated);

		assertThat(propertyName, equalTo(new PropertyName(FIELD_NAME)));
	}

	@Test
	void shouldReturnNullPropertyNameForMissingFieldNameAnnotationOnSerialization() {
		Annotated annotated = mock(Annotated.class);
		doReturn(null).when(annotated).getAnnotation(FieldName.class);
		MapperConfig<?> mapperConfig = mock(MapperConfig.class);

		PropertyName propertyName = annotationIntrospector.findNameForSerialization(mapperConfig, annotated);

		assertThat(propertyName, nullValue());
	}

	@Test
	void shouldReturnPropertyNameForFieldNameAnnotationOnDeserialization() {
		Annotated annotated = mock(Annotated.class);
		FieldName fieldNameAnnotation = mock(FieldName.class);
		doReturn(FIELD_NAME).when(fieldNameAnnotation).value();
		doReturn(fieldNameAnnotation).when(annotated).getAnnotation(FieldName.class);
		MapperConfig<?> mapperConfig = mock(MapperConfig.class);

		PropertyName propertyName = annotationIntrospector.findNameForDeserialization(mapperConfig, annotated);

		assertThat(propertyName, equalTo(new PropertyName(FIELD_NAME)));
	}

	@Test
	void shouldReturnNullPropertyNameForMissingFieldNameAnnotationOnDeserialization() {
		Annotated annotated = mock(Annotated.class);
		doReturn(null).when(annotated).getAnnotation(FieldName.class);
		MapperConfig<?> mapperConfig = mock(MapperConfig.class);

		PropertyName propertyName = annotationIntrospector.findNameForDeserialization(mapperConfig, annotated);

		assertThat(propertyName, nullValue());
	}

	@Test
	void shouldReturnWriteAccessForIgnoredAnnotation() {
		Annotated annotated = mock(Annotated.class);
		doReturn(true).when(annotated).hasAnnotation(Ignored.class);
		MapperConfig<?> mapperConfig = mock(MapperConfig.class);

		Access access = annotationIntrospector.findPropertyAccess(mapperConfig, annotated);

		assertThat(access, equalTo(Access.WRITE_ONLY));
	}

	@Test
	void shouldReturnNullAccessForMissingIgnoredAnnotation() {
		Annotated annotated = mock(Annotated.class);
		doReturn(false).when(annotated).hasAnnotation(Ignored.class);
		MapperConfig<?> mapperConfig = mock(MapperConfig.class);

		Access access = annotationIntrospector.findPropertyAccess(mapperConfig, annotated);

		assertThat(access, nullValue());
	}

	@Test
	void shouldReturnFieldOrderForFieldOrderAnnotation() {
		AnnotatedClass annotatedClass = mock(AnnotatedClass.class);
		FieldOrder fieldOrderAnnotation = mock(FieldOrder.class);
		String[] fieldOrder = { "field1", "field2" };
		doReturn(fieldOrder).when(fieldOrderAnnotation).value();
		doReturn(fieldOrderAnnotation).when(annotatedClass).getAnnotation(FieldOrder.class);
		MapperConfig<?> mapperConfig = mock(MapperConfig.class);

		String[] result = annotationIntrospector.findSerializationPropertyOrder(mapperConfig, annotatedClass);

		assertThat(result, equalTo(fieldOrder));
	}

	@Test
	void shouldReturnNullFieldOrderForMissingFieldOrderAnnotation() {
		AnnotatedClass annotatedClass = mock(AnnotatedClass.class);
		doReturn(null).when(annotatedClass).getAnnotation(FieldOrder.class);
		MapperConfig<?> mapperConfig = mock(MapperConfig.class);

		String[] result = annotationIntrospector.findSerializationPropertyOrder(mapperConfig, annotatedClass);

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnTrueForAsValueAnnotation() {
		Annotated annotated = mock(Annotated.class);
		doReturn(true).when(annotated).hasAnnotation(AsValue.class);
		MapperConfig<?> mapperConfig = mock(MapperConfig.class);

		boolean isAsValue = annotationIntrospector.hasAsValue(mapperConfig, annotated);

		assertThat(isAsValue, equalTo(true));
	}

	@Test
	void shouldReturnNullForMissingAsValueAnnotation() {
		Annotated annotated = mock(Annotated.class);
		doReturn(false).when(annotated).hasAnnotation(AsValue.class);
		MapperConfig<?> mapperConfig = mock(MapperConfig.class);

		Boolean isAsValue = annotationIntrospector.hasAsValue(mapperConfig, annotated);

		assertThat(isAsValue, nullValue());
	}

	@Test
	void shouldReturnModeDefaultForCreatorAnnotation() {
		Annotated annotated = mock(Annotated.class);
		doReturn(true).when(annotated).hasAnnotation(Creator.class);
		MapperConfig<?> mapperConfig = mock(MapperConfig.class);

		Mode mode = annotationIntrospector.findCreatorAnnotation(mapperConfig, annotated);

		assertThat(mode, equalTo(Mode.DEFAULT));
	}

	@Test
	void shouldReturnNullForMissingCreatorAnnotation() {
		Annotated annotated = mock(Annotated.class);
		doReturn(false).when(annotated).hasAnnotation(Creator.class);
		MapperConfig<?> mapperConfig = mock(MapperConfig.class);

		Mode mode = annotationIntrospector.findCreatorAnnotation(mapperConfig, annotated);

		assertThat(mode, nullValue());
	}
}

package org.apiphany.json.jackson3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apiphany.lang.annotation.FieldName;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.PropertyName;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;

/**
 * Test class for {@link ApiphanyJackson3AnnotationIntrospector}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiphanyJackson3AnnotationIntrospectorTest {

	private static final String FIELD_NAME = "fieldName";

	private ApiphanyJackson3AnnotationIntrospector annotationIntrospector = ApiphanyJackson3AnnotationIntrospector.getInstance();

	@Test
	void shouldHandleFieldNameAnnotation() {
		Annotated annotated = mock(Annotated.class);
		FieldName fieldNameAnnotation = mock(FieldName.class);
		doReturn(FIELD_NAME).when(fieldNameAnnotation).value();
		doReturn(fieldNameAnnotation).when(annotated).getAnnotation(FieldName.class);
		MapperConfig<?> mapperConfig = mock(MapperConfig.class);

		PropertyName propertyName = annotationIntrospector.findNameForSerialization(mapperConfig, annotated);

		assertThat(propertyName, equalTo(new PropertyName(FIELD_NAME)));
	}
}

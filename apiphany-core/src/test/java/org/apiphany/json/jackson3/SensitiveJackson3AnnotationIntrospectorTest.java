package org.apiphany.json.jackson3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apiphany.json.jackson3.serializers.RedactedValueSerializer;
import org.apiphany.security.Sensitive;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty.Access;

import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;

/**
 * Test class for {@link SensitiveJackson3AnnotationIntrospector}.
 *
 * @author Radu Sebastian LAZIN
 */
class SensitiveJackson3AnnotationIntrospectorTest {

	@Nested
	class FindPropertyAccessTests {

		@Test
		void shouldReturnAccessWriteOnlyWhenSensitivityEnabled() {
			SensitiveJackson3AnnotationIntrospector introspector = SensitiveJackson3AnnotationIntrospector.hideSensitive();

			Sensitive sensitive = mock(Sensitive.class);
			doReturn(Sensitive.Visibility.HIDDEN).when(sensitive).visibility();

			Annotated annotated = mock(Annotated.class);
			doReturn(true).when(annotated).hasAnnotation(Sensitive.class);
			doReturn(sensitive).when(annotated).getAnnotation(Sensitive.class);
			MapperConfig<?> mapperConfig = mock(MapperConfig.class);

			Access access = introspector.findPropertyAccess(mapperConfig, annotated);

			assertThat(access, equalTo(Access.WRITE_ONLY));
		}

		@Test
		void shouldReturnAccessWriteOnlyWhenSensitivityEnabledButVisibilityIsRedacted() {
			SensitiveJackson3AnnotationIntrospector introspector = SensitiveJackson3AnnotationIntrospector.hideSensitive();

			Sensitive sensitive = mock(Sensitive.class);
			doReturn(Sensitive.Visibility.REDACTED).when(sensitive).visibility();

			Annotated annotated = mock(Annotated.class);
			doReturn(true).when(annotated).hasAnnotation(Sensitive.class);
			doReturn(sensitive).when(annotated).getAnnotation(Sensitive.class);
			MapperConfig<?> mapperConfig = mock(MapperConfig.class);

			Access access = introspector.findPropertyAccess(mapperConfig, annotated);

			assertThat(access, equalTo(null));
		}

		@Test
		void shouldReturnNullAccessWhenNoSensitiveAnnotationPresent() {
			SensitiveJackson3AnnotationIntrospector introspector = SensitiveJackson3AnnotationIntrospector.hideSensitive();

			Annotated annotated = mock(Annotated.class);
			doReturn(false).when(annotated).hasAnnotation(Sensitive.class);
			MapperConfig<?> mapperConfig = mock(MapperConfig.class);

			Access access = introspector.findPropertyAccess(mapperConfig, annotated);

			assertThat(access, equalTo(null));
		}

		@Test
		void shouldReturnNullAccessWhenSensitivityDisabled() {
			SensitiveJackson3AnnotationIntrospector introspector = SensitiveJackson3AnnotationIntrospector.allowSensitive();

			Annotated annotated = mock(Annotated.class);
			doReturn(true).when(annotated).hasAnnotation(Sensitive.class);
			MapperConfig<?> mapperConfig = mock(MapperConfig.class);

			Access access = introspector.findPropertyAccess(mapperConfig, annotated);

			assertThat(access, equalTo(null));
		}
	}

	@Nested
	class FindSerializerTests {

		@Test
		void shouldReturnNullSerializerWhenSensitivityEnabled() {
			SensitiveJackson3AnnotationIntrospector introspector = SensitiveJackson3AnnotationIntrospector.hideSensitive();

			Sensitive sensitive = mock(Sensitive.class);
			doReturn(Sensitive.Visibility.HIDDEN).when(sensitive).visibility();

			Annotated annotated = mock(Annotated.class);
			doReturn(true).when(annotated).hasAnnotation(Sensitive.class);
			doReturn(sensitive).when(annotated).getAnnotation(Sensitive.class);
			MapperConfig<?> mapperConfig = mock(MapperConfig.class);

			Object serializer = introspector.findSerializer(mapperConfig, annotated);

			assertThat(serializer, equalTo(null));
		}

		@Test
		void shouldReturnRedactedValueSerializerWhenSensitivityEnabledButVisibilityIsRedacted() {
			SensitiveJackson3AnnotationIntrospector introspector = SensitiveJackson3AnnotationIntrospector.hideSensitive();

			Sensitive sensitive = mock(Sensitive.class);
			doReturn(Sensitive.Visibility.REDACTED).when(sensitive).visibility();

			Annotated annotated = mock(Annotated.class);
			doReturn(true).when(annotated).hasAnnotation(Sensitive.class);
			doReturn(sensitive).when(annotated).getAnnotation(Sensitive.class);
			MapperConfig<?> mapperConfig = mock(MapperConfig.class);

			Object serializer = introspector.findSerializer(mapperConfig, annotated);

			assertThat(serializer.getClass(), equalTo(RedactedValueSerializer.class));
		}

		@Test
		void shouldReturnNullAccessWhenNoSensitiveAnnotationPresent() {
			SensitiveJackson3AnnotationIntrospector introspector = SensitiveJackson3AnnotationIntrospector.hideSensitive();

			Annotated annotated = mock(Annotated.class);
			doReturn(false).when(annotated).hasAnnotation(Sensitive.class);
			MapperConfig<?> mapperConfig = mock(MapperConfig.class);

			Object serializer = introspector.findSerializer(mapperConfig, annotated);

			assertThat(serializer, equalTo(null));
		}

		@Test
		void shouldReturnNullAccessWhenSensitivityDisabled() {
			SensitiveJackson3AnnotationIntrospector introspector = SensitiveJackson3AnnotationIntrospector.allowSensitive();

			Annotated annotated = mock(Annotated.class);
			doReturn(true).when(annotated).hasAnnotation(Sensitive.class);
			MapperConfig<?> mapperConfig = mock(MapperConfig.class);

			Object serializer = introspector.findSerializer(mapperConfig, annotated);

			assertThat(serializer, equalTo(null));
		}
	}
}

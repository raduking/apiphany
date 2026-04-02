package org.apiphany.json.jackson2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apiphany.json.jackson2.serializers.RedactedValueSerializer;
import org.apiphany.security.Sensitive;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.introspect.Annotated;

/**
 * Test class for {@link SensitiveJackson2AnnotationIntrospector}.
 *
 * @author Radu Sebastian LAZIN
 */
class SensitiveJackson2AnnotationIntrospectorTest {

	@Nested
	class FindPropertyAccessTests {

		@Test
		void shouldReturnAccessWriteOnlyWhenSensitivityEnabled() {
			SensitiveJackson2AnnotationIntrospector introspector = SensitiveJackson2AnnotationIntrospector.hideSensitive();

			Sensitive sensitive = mock(Sensitive.class);
			doReturn(Sensitive.Visibility.HIDDEN).when(sensitive).visibility();

			Annotated annotated = mock(Annotated.class);
			doReturn(true).when(annotated).hasAnnotation(Sensitive.class);
			doReturn(sensitive).when(annotated).getAnnotation(Sensitive.class);

			Access access = introspector.findPropertyAccess(annotated);

			assertThat(access, equalTo(Access.WRITE_ONLY));
		}

		@Test
		void shouldReturnNullAccessWhenSensitivityEnabledButVisibilityIsRedacted() {
			SensitiveJackson2AnnotationIntrospector introspector = SensitiveJackson2AnnotationIntrospector.hideSensitive();

			Sensitive sensitive = mock(Sensitive.class);
			doReturn(Sensitive.Visibility.REDACTED).when(sensitive).visibility();

			Annotated annotated = mock(Annotated.class);
			doReturn(true).when(annotated).hasAnnotation(Sensitive.class);
			doReturn(sensitive).when(annotated).getAnnotation(Sensitive.class);

			Access access = introspector.findPropertyAccess(annotated);

			assertThat(access, equalTo(null));
		}

		@Test
		void shouldReturnNullAccessWhenNoSensitiveAnnotationPresent() {
			SensitiveJackson2AnnotationIntrospector introspector = SensitiveJackson2AnnotationIntrospector.hideSensitive();

			Annotated annotated = mock(Annotated.class);
			doReturn(false).when(annotated).hasAnnotation(Sensitive.class);

			Access access = introspector.findPropertyAccess(annotated);

			assertThat(access, equalTo(null));
		}

		@Test
		void shouldReturnNullAccessWhenSensitivityDisabled() {
			SensitiveJackson2AnnotationIntrospector introspector = SensitiveJackson2AnnotationIntrospector.allowSensitive();

			Annotated annotated = mock(Annotated.class);
			doReturn(true).when(annotated).hasAnnotation(Sensitive.class);

			Access access = introspector.findPropertyAccess(annotated);

			assertThat(access, equalTo(null));
		}
	}

	@Nested
	class FindSerializerTests {

		@Test
		void shouldReturnNullSerializerWhenSensitivityEnabled() {
			SensitiveJackson2AnnotationIntrospector introspector = SensitiveJackson2AnnotationIntrospector.hideSensitive();

			Sensitive sensitive = mock(Sensitive.class);
			doReturn(Sensitive.Visibility.HIDDEN).when(sensitive).visibility();

			Annotated annotated = mock(Annotated.class);
			doReturn(true).when(annotated).hasAnnotation(Sensitive.class);
			doReturn(sensitive).when(annotated).getAnnotation(Sensitive.class);

			Object serializer = introspector.findSerializer(annotated);

			assertThat(serializer, equalTo(null));
		}

		@Test
		void shouldReturnRedactedValueSerializerWhenSensitivityEnabledButVisibilityIsRedacted() {
			SensitiveJackson2AnnotationIntrospector introspector = SensitiveJackson2AnnotationIntrospector.hideSensitive();

			Sensitive sensitive = mock(Sensitive.class);
			doReturn(Sensitive.Visibility.REDACTED).when(sensitive).visibility();

			Annotated annotated = mock(Annotated.class);
			doReturn(true).when(annotated).hasAnnotation(Sensitive.class);
			doReturn(sensitive).when(annotated).getAnnotation(Sensitive.class);

			Object serializer = introspector.findSerializer(annotated);

			assertThat(serializer.getClass(), equalTo(RedactedValueSerializer.class));
		}

		@Test
		void shouldReturnNullAccessWhenNoSensitiveAnnotationPresent() {
			SensitiveJackson2AnnotationIntrospector introspector = SensitiveJackson2AnnotationIntrospector.hideSensitive();

			Annotated annotated = mock(Annotated.class);
			doReturn(false).when(annotated).hasAnnotation(Sensitive.class);

			Object serializer = introspector.findSerializer(annotated);

			assertThat(serializer, equalTo(null));
		}

		@Test
		void shouldReturnNullAccessWhenSensitivityDisabled() {
			SensitiveJackson2AnnotationIntrospector introspector = SensitiveJackson2AnnotationIntrospector.allowSensitive();

			Annotated annotated = mock(Annotated.class);
			doReturn(true).when(annotated).hasAnnotation(Sensitive.class);

			Object serializer = introspector.findSerializer(annotated);

			assertThat(serializer, equalTo(null));
		}
	}
}

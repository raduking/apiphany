package org.apiphany.json.jackson3;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JacksonAnnotation;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationValue.ForTypeDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription.ForLoadedType;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

/**
 * Base test class for Jackson 3 compatibility.
 * <p>
 * This test ensures that the necessary annotations are present for Jackson 3 to function correctly.
 *
 * @author Radu Sebastian LAZIN
 */
class Jackson3Test {

	protected static final Class<?> jsonSerializeAs = getSerializeAs();
	protected static final Class<?> jsonDeserializeAs = getDeserializeAs();

	private static Class<?> getSerializeAs() {
		try {
			return Class.forName("com.fasterxml.jackson.databind.annotation.JsonSerializeAs");
		} catch (ClassNotFoundException e) {
			return createJsonSerializeAsAnnotation();
		}
	}

	private static Class<?> getDeserializeAs() {
		try {
			return Class.forName("com.fasterxml.jackson.databind.annotation.JsonDeserializeAs");
		} catch (ClassNotFoundException e) {
			return createJsonDeserializeAsAnnotation();
		}
	}

	@SuppressWarnings("resource")
	static Class<?> createJsonSerializeAsAnnotation() {
		AnnotationDescription retentionAnnotation = AnnotationDescription.Builder
				.ofType(Retention.class)
				.define("value", RetentionPolicy.RUNTIME)
				.build();

		AnnotationDescription targetAnnotation = AnnotationDescription.Builder
				.ofType(Target.class)
				.defineEnumerationArray("value", ElementType.class,
						ElementType.FIELD,
						ElementType.METHOD,
						ElementType.PARAMETER,
						ElementType.ANNOTATION_TYPE)
				.build();

		AnnotationDescription jacksonAnnotation = AnnotationDescription.Builder
				.ofType(JacksonAnnotation.class)
				.build();

		var voidDefault = ForTypeDescription.of(ForLoadedType.of(void.class));

		DynamicType.Unloaded<?> dynamicAnnotation = new ByteBuddy()
				.makeAnnotation()
				.name("com.fasterxml.jackson.annotation.JsonSerializeAs")
				.annotateType(retentionAnnotation, targetAnnotation, jacksonAnnotation)
				.defineMethod("value", Class.class, Visibility.PUBLIC)
				.defaultValue(voidDefault)
				.defineMethod("content", Class.class, Visibility.PUBLIC)
				.defaultValue(voidDefault)
				.defineMethod("key", Class.class, Visibility.PUBLIC)
				.defaultValue(voidDefault)
				.make();

		Class<?> annotationClass = dynamicAnnotation
				.load(Jackson3Test.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
				.getLoaded();

		System.out.println("Created @JsonSerializeAs annotation: " + annotationClass);

		return annotationClass;
	}

	@SuppressWarnings("resource")
	static Class<?> createJsonDeserializeAsAnnotation() {
		AnnotationDescription retentionAnnotation = AnnotationDescription.Builder
				.ofType(Retention.class)
				.define("value", RetentionPolicy.RUNTIME)
				.build();

		AnnotationDescription targetAnnotation = AnnotationDescription.Builder
				.ofType(Target.class)
				.defineEnumerationArray("value", ElementType.class,
						ElementType.FIELD,
						ElementType.METHOD,
						ElementType.PARAMETER,
						ElementType.ANNOTATION_TYPE)
				.build();

		AnnotationDescription jacksonAnnotation = AnnotationDescription.Builder
				.ofType(JacksonAnnotation.class)
				.build();

		var voidDefault = ForTypeDescription.of(ForLoadedType.of(void.class));

		DynamicType.Unloaded<?> dynamicAnnotation = new ByteBuddy()
				.makeAnnotation()
				.name("com.fasterxml.jackson.annotation.JsonDeserializeAs")
				.annotateType(retentionAnnotation, targetAnnotation, jacksonAnnotation)
				.defineMethod("value", Class.class, Visibility.PUBLIC)
				.defaultValue(voidDefault)
				.defineMethod("content", Class.class, Visibility.PUBLIC)
				.defaultValue(voidDefault)
				.defineMethod("keys", Class.class, Visibility.PUBLIC)
				.defaultValue(voidDefault)
				.make();

		Class<?> annotationClass = dynamicAnnotation
				.load(Jackson3Test.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
				.getLoaded();

		System.out.println("Created @JsonDeserializeAs annotation: " + annotationClass);

		return annotationClass;
	}

	@Test
	void testAnnotationCreation() {
		assertNotNull(jsonSerializeAs);
		assertNotNull(jsonDeserializeAs);
	}
}

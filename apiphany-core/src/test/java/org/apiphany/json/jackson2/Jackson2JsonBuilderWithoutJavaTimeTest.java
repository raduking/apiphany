package org.apiphany.json.jackson2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;

import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.ClassHidingClassLoader;

/**
 * Test class for {@link Jackson2JsonBuilder} when Jackson's Java time module is not present in the classpath.
 * <p>
 * The Java time module is an optional dependency, so the absence of the module cannot be simulated with the classpath
 * of this module, which always contains it. Instead, the Jackson 2 and the Morphix reflection classes are loaded by a
 * class loader that refuses to load the Java time module, which is exactly what the runtime does when the optional
 * dependency is missing.
 *
 * @author Radu Sebastian LAZIN
 */
class Jackson2JsonBuilderWithoutJavaTimeTest {

	/**
	 * Jackson's Java time module classes used by {@link JavaTimeJackson2Module}.
	 */
	private static final String[] JAVA_TIME_CLASS_NAMES = {
			"com.fasterxml.jackson.datatype.jsr310.JavaTimeModule",
			"com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer",
			"com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer",
			"com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer",
			"com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer"
	};

	/**
	 * JSON body used to check that the builder works when the Java time module is not present.
	 */
	private static final Map<String, String> BODY = Map.of("name", "value");

	@Test
	void shouldNotSeeJavaTimeModuleFromTheSimulatingClassLoader() throws Exception {
		ClassLoader classLoader = newJavaTimeModuleHidingClassLoader();

		Class<?> libraryClass = Class.forName(Jackson2Library.class.getName(), true, classLoader);

		boolean javaTimeModulePresent = (Boolean) libraryClass.getMethod("isJavaTimeModulePresent").invoke(null);

		assertThat(libraryClass.getClassLoader(), equalTo(classLoader));
		assertThat(javaTimeModulePresent, equalTo(false));
		assertThat(Jackson2Library.isJavaTimeModulePresent(), equalTo(true));
	}

	@Test
	void shouldCreateSingletonWhenJavaTimeModuleIsNotPresent() throws Exception {
		ClassLoader classLoader = newJavaTimeModuleHidingClassLoader();
		Thread thread = Thread.currentThread();
		ClassLoader previousContextClassLoader = thread.getContextClassLoader();

		Object instance;
		String json;
		try {
			thread.setContextClassLoader(classLoader);

			Class<?> builderClass = Class.forName(Jackson2JsonBuilder.class.getName(), true, classLoader);

			instance = builderClass.getMethod("runtime").invoke(null);
			json = (String) builderClass.getMethod("toJson", Object.class).invoke(null, BODY);
		} finally {
			thread.setContextClassLoader(previousContextClassLoader);
		}

		assertThat(instance, notNullValue());
		assertThat(Strings.removeAllWhitespace(json), equalTo("{\"name\":\"value\"}"));
	}

	/**
	 * Creates a class loader that hides Jackson's Java time module.
	 *
	 * @return a class loader that hides Jackson's Java time module
	 */
	private static ClassLoader newJavaTimeModuleHidingClassLoader() {
		ClassHidingClassLoader.Builder builder = ClassHidingClassLoader.builder()
				.parent(Jackson2JsonBuilderWithoutJavaTimeTest.class.getClassLoader())
				.loadLocally(Jackson2JsonBuilder.class)
				.loadLocally(Jackson2Library.class)
				.loadLocally(JavaTimeJackson2Module.class)
				.loadLocally("org.morphix.reflection.Classes")
				.loadLocally("org.morphix.reflection.Reflection");
		for (String javaTimeClassName : JAVA_TIME_CLASS_NAMES) {
			builder.hide(javaTimeClassName);
		}
		return builder.build();
	}
}

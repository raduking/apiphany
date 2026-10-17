package org.apiphany.json.jackson2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;

import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.ClassHidingClassLoader;

/**
 * Test class for {@link Jackson2JsonBuilder} when Jackson's JDK 8 module is not present in the classpath.
 * <p>
 * The JDK 8 module is an optional dependency, so the absence of the module cannot be simulated with the classpath of
 * this module, which always contains it. Instead, the Jackson 2 and the Morphix reflection classes are loaded by a
 * class loader that refuses to load the JDK 8 module, which is exactly what the runtime does when the optional
 * dependency is missing.
 *
 * @author Radu Sebastian LAZIN
 */
class Jackson2JsonBuilderWithoutJdk8Test {

	/**
	 * Jackson's JDK 8 module class name.
	 */
	private static final String JDK8_MODULE_CLASS_NAME = "com.fasterxml.jackson.datatype.jdk8.Jdk8Module";

	/**
	 * JSON body used to check that the builder works when the JDK 8 module is not present.
	 */
	private static final Map<String, String> BODY = Map.of("name", "value");

	@Test
	void shouldNotSeeJdk8ModuleFromTheSimulatingClassLoader() throws Exception {
		ClassLoader classLoader = newJdk8ModuleHidingClassLoader();

		Class<?> libraryClass = Class.forName(Jackson2Library.class.getName(), true, classLoader);

		boolean jdk8ModulePresent = (Boolean) libraryClass.getMethod("isJdk8ModulePresent").invoke(null);

		assertThat(libraryClass.getClassLoader(), equalTo(classLoader));
		assertThat(jdk8ModulePresent, equalTo(false));
		assertThat(Jackson2Library.isJdk8ModulePresent(), equalTo(true));
	}

	@Test
	void shouldCreateSingletonWhenJdk8ModuleIsNotPresent() throws Exception {
		ClassLoader classLoader = newJdk8ModuleHidingClassLoader();
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
	 * Creates a class loader that hides Jackson's JDK 8 module.
	 *
	 * @return a class loader that hides Jackson's JDK 8 module
	 */
	private static ClassLoader newJdk8ModuleHidingClassLoader() {
		return ClassHidingClassLoader.builder()
				.parent(Jackson2JsonBuilderWithoutJdk8Test.class.getClassLoader())
				.hide(JDK8_MODULE_CLASS_NAME)
				.loadLocally(Jackson2JsonBuilder.class)
				.loadLocally(Jackson2Library.class)
				.loadLocally("org.morphix.reflection.Classes")
				.loadLocally("org.morphix.reflection.Reflection")
				.build();
	}
}

package org.apiphany.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.ClassHidingClassLoader;

/**
 * Test class for {@link JsonBuilder} when no JSON library is present in the classpath.
 * <p>
 * The JSON libraries are detected by loading their object mapper class with {@code Class.forName}, so the absence of a
 * library cannot be simulated with the classpath of this module, which always contains Jackson 2 and Jackson 3.
 * Instead, the {@link JsonBuilder} and the Morphix classes that detect the libraries are loaded by a class loader that
 * refuses to load both object mappers, which is exactly what the runtime does when no JSON library is present.
 *
 * @author Radu Sebastian LAZIN
 */
class JsonBuilderWithoutJsonLibraryTest {

	/**
	 * Jackson 2 object mapper class name, Jackson 2 is detected by loading this class.
	 */
	private static final String JACKSON_2_OBJECT_MAPPER_CLASS_NAME = "com.fasterxml.jackson.databind.ObjectMapper";

	/**
	 * Jackson 3 object mapper class name, Jackson 3 is detected by loading this class.
	 */
	private static final String JACKSON_3_OBJECT_MAPPER_CLASS_NAME = "tools.jackson.databind.ObjectMapper";

	@Test
	void shouldNotFindALibraryWhenNoObjectMapperIsVisible() throws Exception {
		ClassLoader classLoader = newNoJsonLibraryClassLoader();
		Thread thread = Thread.currentThread();
		ClassLoader previousContextClassLoader = thread.getContextClassLoader();

		Class<?> builderClass;
		boolean hasLibrary;
		Class<?> runtimeClass;
		try {
			thread.setContextClassLoader(classLoader);

			builderClass = Class.forName(JsonBuilder.class.getName(), true, classLoader);
			hasLibrary = (Boolean) builderClass.getMethod("hasLibrary").invoke(null);
			runtimeClass = builderClass.getMethod("runtime").invoke(null).getClass();
		} finally {
			thread.setContextClassLoader(previousContextClassLoader);
		}

		assertThat(builderClass.getClassLoader(), equalTo(classLoader));
		assertThat(hasLibrary, equalTo(false));
		assertThat(runtimeClass, equalTo(builderClass));
		assertThat(JsonBuilder.hasLibrary(), equalTo(true));
	}

	/**
	 * Creates a class loader that hides the Jackson object mappers.
	 *
	 * @return a class loader that hides the Jackson object mappers
	 */
	private static ClassLoader newNoJsonLibraryClassLoader() {
		return ClassHidingClassLoader.builder()
				.parent(JsonBuilderWithoutJsonLibraryTest.class.getClassLoader())
				.hide(JACKSON_2_OBJECT_MAPPER_CLASS_NAME)
				.hide(JACKSON_3_OBJECT_MAPPER_CLASS_NAME)
				.loadLocally(JsonBuilder.class)
				.loadLocally(JsonObservability.class)
				.loadLocally("org.apiphany.json.jackson2.Jackson2Library")
				.loadLocally("org.apiphany.json.jackson3.Jackson3Library")
				.loadLocally("org.morphix.runtime.OptionalLibrary")
				.loadLocally("org.morphix.runtime.Libraries")
				.loadLocally("org.morphix.reflection.Classes")
				.build();
	}
}

package org.apiphany.json.jackson2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.http.ApacheHC5Library;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.ClassHidingClassLoader;
import org.morphix.reflection.Fields;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Test class for {@link ApiphanyHC5Jackson2Module} when Apache HttpClient 5 is not present in the classpath.
 * <p>
 * The Apache HTTP Client 5 dependency cannot be removed from the classpath of this module, so its absence is simulated
 * with a class loader that refuses to load it, which is exactly what the runtime does when the dependency is missing.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiphanyHC5Jackson2ModuleWithoutHC5Test {

	/**
	 * The Apache HttpClient 5 class used to detect whether the library is present.
	 */
	private static final String CLOSEABLE_HTTP_CLIENT_CLASS_NAME =
			"org.apache.hc.client5.http.impl.classic.CloseableHttpClient";

	@Test
	void shouldNotSeeTheApacheHC5LibraryFromTheSimulatingClassLoader() throws Exception {
		ClassLoader classLoader = newHC5HidingClassLoader();

		Class<?> libraryClass = Class.forName(ApacheHC5Library.class.getName(), true, classLoader);

		boolean libraryPresent = (Boolean) libraryClass.getMethod("isPresent").invoke(null);

		assertThat(libraryClass.getClassLoader(), equalTo(classLoader));
		assertThat(libraryPresent, equalTo(false));
		assertThat(ApacheHC5Library.isPresent(), equalTo(true));
	}

	@Test
	void shouldReturnAModuleWithoutTimeoutDeserializerWhenTheLibraryIsNotPresent() throws Exception {
		ClassLoader classLoader = newHC5HidingClassLoader();
		Thread thread = Thread.currentThread();
		ClassLoader previousContextClassLoader = thread.getContextClassLoader();

		Object module;
		try {
			thread.setContextClassLoader(classLoader);

			Class<?> moduleClass = Class.forName(ApiphanyHC5Jackson2Module.class.getName(), true, classLoader);
			module = moduleClass.getMethod("instance").invoke(null);
		} finally {
			thread.setContextClassLoader(previousContextClassLoader);
		}

		assertThat(module, notNullValue());
		assertThat(module, instanceOf(SimpleModule.class));
		assertThat(Fields.IgnoreAccess.get(module, "_deserializers"), nullValue());
	}

	/**
	 * Creates a class loader that hides Apache HttpClient 5.
	 *
	 * @return a class loader that hides Apache HttpClient 5
	 */
	private static ClassLoader newHC5HidingClassLoader() {
		return ClassHidingClassLoader.builder()
				.parent(ApiphanyHC5Jackson2ModuleWithoutHC5Test.class.getClassLoader())
				.hide(CLOSEABLE_HTTP_CLIENT_CLASS_NAME)
				.loadLocally(ApiphanyHC5Jackson2Module.class)
				.loadLocally(ApacheHC5Library.class)
				.loadLocally("org.morphix.reflection.Classes")
				.loadLocally("org.morphix.reflection.Reflection")
				.build();
	}
}

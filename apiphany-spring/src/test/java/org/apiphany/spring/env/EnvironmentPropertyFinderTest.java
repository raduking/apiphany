package org.apiphany.spring.env;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Tests for {@link EnvironmentPropertyFinder}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class EnvironmentPropertyFinderTest {

	private static final String KEY = "test.key";
	private static final String VALUE = "test-value";
	private static final String DEFAULT_STRING = "default";

	@Nested
	class GetEnvironmentTests {

		@Test
		void shouldReturnEnvironmentFromApplicationContext() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			Environment env = mock(Environment.class);
			when(ctx.getEnvironment()).thenReturn(env);
			TestEnvironmentPropertyFinder finder = createFinder(ctx);

			Environment result = finder.getEnvironment();

			assertThat(result, equalTo(env));
		}

		@Test
		void shouldReturnNullWhenApplicationContextIsNull() {
			TestEnvironmentPropertyFinder finder = createFinder(null);

			Environment result = finder.getEnvironment();

			assertThat(result, nullValue());
		}
	}

	@Nested
	class GetEnvironmentPropertyTests {

		@Test
		void shouldReturnEnvironmentProperty() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			Environment env = mock(Environment.class);
			when(ctx.getEnvironment()).thenReturn(env);
			doReturn(VALUE).when(env).getProperty(KEY, String.class, DEFAULT_STRING);
			TestEnvironmentPropertyFinder finder = createFinder(ctx);

			String result = finder.getEnvironmentProperty(KEY, String.class, DEFAULT_STRING);

			assertThat(result, equalTo(VALUE));
		}

		@Test
		void shouldReturnDefaultValueWhenPropertyIsMissing() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			Environment env = mock(Environment.class);
			when(ctx.getEnvironment()).thenReturn(env);
			doReturn(DEFAULT_STRING).when(env).getProperty(KEY, String.class, DEFAULT_STRING);
			TestEnvironmentPropertyFinder finder = createFinder(ctx);

			String result = finder.getEnvironmentProperty(KEY, String.class, DEFAULT_STRING);

			assertThat(result, equalTo(DEFAULT_STRING));
		}

		@Test
		void shouldThrowWhenEnvironmentIsMissing() {
			TestEnvironmentPropertyFinder finder = createFinder(null);

			assertThrows(NullPointerException.class, () -> finder.getEnvironmentProperty(KEY, String.class, DEFAULT_STRING));
		}

		@Test
		void shouldGetPropertyFromEnvironmentOfApplicationContext() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			Environment env = mock(Environment.class);
			when(ctx.getEnvironment()).thenReturn(env);
			TestEnvironmentPropertyFinder finder = createFinder(ctx);

			finder.getEnvironmentProperty(KEY, String.class, DEFAULT_STRING);

			verify(env).getProperty(KEY, String.class, DEFAULT_STRING);
		}
	}

	static class TestEnvironmentPropertyFinder implements EnvironmentPropertyFinder {

		private final ApplicationContext applicationContext;

		TestEnvironmentPropertyFinder(final ApplicationContext applicationContext) {
			this.applicationContext = applicationContext;
		}

		@Override
		public ApplicationContext getApplicationContext() {
			return applicationContext;
		}
	}

	private static TestEnvironmentPropertyFinder createFinder(final ApplicationContext ctx) {
		return new TestEnvironmentPropertyFinder(ctx);
	}
}

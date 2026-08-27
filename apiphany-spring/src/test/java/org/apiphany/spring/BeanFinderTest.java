package org.apiphany.spring;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.function.Consumer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

/**
 * Tests for {@link BeanFinder}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class BeanFinderTest {

	@Nested
	class GetBeanByNameTests {

		@Test
		void shouldReturnBeanByName() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			doReturn("value").when(ctx).getBean("myBean");
			TestBeanFinder finder = createFinder(ctx);

			String result = finder.getBean("myBean");

			assertThat(result, equalTo("value"));
		}

		@Test
		void shouldReturnNullWhenBeanNotFoundByName() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			doReturn(null).when(ctx).getBean("missing");
			TestBeanFinder finder = createFinder(ctx);

			String result = finder.getBean("missing");

			assertThat(result, nullValue());
		}

		@Test
		@SuppressWarnings("unchecked")
		void shouldCallOnErrorWhenBeanNotFoundByName() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			doReturn(null).when(ctx).getBean("missing");
			Consumer<Exception> onError = mock(Consumer.class);
			TestBeanFinder finder = createFinder(ctx);

			String result = finder.getBean("missing", onError);

			assertThat(result, nullValue());
		}
	}

	@Nested
	class GetBeanByClassTests {

		@Test
		void shouldReturnBeanByClass() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			doReturn("value").when(ctx).getBean(String.class);
			TestBeanFinder finder = createFinder(ctx);

			String result = finder.getBean(String.class);

			assertThat(result, equalTo("value"));
		}

		@Test
		void shouldReturnNullWhenBeanNotFoundByClass() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			doReturn(null).when(ctx).getBean(String.class);
			TestBeanFinder finder = createFinder(ctx);

			String result = finder.getBean(String.class);

			assertThat(result, nullValue());
		}

		@Test
		@SuppressWarnings("unchecked")
		void shouldCallOnErrorWhenBeanNotFoundByClass() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			doReturn(null).when(ctx).getBean(String.class);
			Consumer<Exception> onError = mock(Consumer.class);
			TestBeanFinder finder = createFinder(ctx);

			String result = finder.getBean(String.class, onError);

			assertThat(result, nullValue());
		}
	}

	static class TestBeanFinder implements BeanFinder {

		private final ApplicationContext applicationContext;

		TestBeanFinder(final ApplicationContext applicationContext) {
			this.applicationContext = applicationContext;
		}

		@Override
		public ApplicationContext getApplicationContext() {
			return applicationContext;
		}
	}

	private static TestBeanFinder createFinder(final ApplicationContext ctx) {
		return new TestBeanFinder(ctx);
	}
}

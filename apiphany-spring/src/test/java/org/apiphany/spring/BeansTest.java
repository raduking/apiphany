package org.apiphany.spring;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apiphany.test.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.reflection.Constructors;
import org.springframework.context.ApplicationContext;

/**
 * Tests for {@link Beans}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class BeansTest {

	@Nested
	class ConstructorsTests {

		@Test
		void shouldHaveMessageConstructor() {
			UnsupportedOperationException e = Assertions.assertDefaultConstructorThrows(Beans.class);
			assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
		}

		@Test
		void shouldHavePrivateMessageConstructor() {
			UnsupportedOperationException e = Assertions.assertDefaultConstructorThrows(Beans.Message.class);
			assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
		}
	}

	@Nested
	class GetBeanByNameTests {

		@Test
		void shouldReturnBeanByName() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			when(ctx.getBean("myBean")).thenReturn("value");

			String result = Beans.getBean("myBean", ctx);

			assertThat(result, equalTo("value"));
		}

		@Test
		void shouldReturnNullWhenBeanNotFoundByName() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			doThrow(new RuntimeException("not found")).when(ctx).getBean("missing");

			String result = Beans.getBean("missing", ctx);

			assertThat(result, nullValue());
		}

		@Test
		void shouldCallOnErrorWhenBeanNotFoundByName() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			doThrow(new RuntimeException("not found")).when(ctx).getBean("missing");

			AtomicReference<Exception> captured = new AtomicReference<>();
			Beans.getBean("missing", ctx, captured::set);

			assertThat(captured.get(), notNullValue());
		}

		@Test
		void shouldReturnNullWhenBeanNotFoundByNameWithNeededIn() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			doThrow(new RuntimeException("not found")).when(ctx).getBean("missing");

			String result = Beans.getBean("missing", String.class, ctx);

			assertThat(result, nullValue());
		}
	}

	@Nested
	class GetBeanByClassTests {

		@Test
		void shouldReturnBeanByClass() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			when(ctx.getBean(String.class)).thenReturn("value");

			String result = Beans.getBean(String.class, ctx);

			assertThat(result, equalTo("value"));
		}

		@Test
		void shouldReturnNullWhenBeanNotFoundByClass() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			doThrow(new RuntimeException("not found")).when(ctx).getBean(String.class);

			String result = Beans.getBean(String.class, ctx);

			assertThat(result, nullValue());
		}

		@Test
		void shouldCallOnErrorWhenBeanNotFoundByClass() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			doThrow(new RuntimeException("not found")).when(ctx).getBean(String.class);

			AtomicReference<Exception> captured = new AtomicReference<>();
			Beans.getBean(String.class, ctx, captured::set);

			assertThat(captured.get(), notNullValue());
		}

		@Test
		void shouldReturnNullWhenBeanNotFoundByClassWithNeededIn() {
			ApplicationContext ctx = mock(ApplicationContext.class);
			doThrow(new RuntimeException("not found")).when(ctx).getBean(String.class);

			String result = Beans.getBean(String.class, String.class, ctx);

			assertThat(result, nullValue());
		}
	}

	@Nested
	class GetBeanWithSupplierTests {

		@Test
		void shouldReturnBeanFromSupplier() {
			String result = Beans.getBean(() -> "value", "beanId", e -> {
				// empty
			});

			assertThat(result, equalTo("value"));
		}

		@Test
		void shouldReturnNullWhenSupplierThrows() {
			String result = Beans.getBean(() -> {
				throw new RuntimeException("error");
			}, "beanId", e -> {
				// empty
			});

			assertThat(result, nullValue());
		}

		@Test
		void shouldCallOnErrorWhenSupplierThrows() {
			AtomicReference<Exception> captured = new AtomicReference<>();
			Beans.getBean(() -> {
				throw new RuntimeException("error");
			}, "beanId", captured::set);

			assertThat(captured.get(), notNullValue());
		}
	}

	@Nested
	class NullOnErrorTests {

		@Test
		void shouldReturnConsumer() {
			Consumer<Exception> consumer = Beans.nullOnError();

			assertThat(consumer, notNullValue());
		}
	}
}

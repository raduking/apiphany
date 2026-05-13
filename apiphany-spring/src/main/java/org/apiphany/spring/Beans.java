package org.apiphany.spring;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.morphix.lang.JavaObjects;
import org.morphix.lang.function.Consumers;
import org.morphix.reflection.Constructors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Utility methods for spring beans.
 *
 * @author Radu Sebastian LAZIN
 */
public class Beans {

	/**
	 * Class logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Beans.class);

	/**
	 * Messages used for beans.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Message {

		/**
		 * Message when a bean is not found.
		 */
		public static final String BEAN_NOT_FOUND = "Bean not found: {}";

		/**
		 * Message when a bean is not found and needed in another bean.
		 */
		public static final String BEAN_NOT_FOUND_NEEDED_IN = BEAN_NOT_FOUND + ", needed in: {}";

		/**
		 * Hide default constructor.
		 */
		private Message() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * Private constructor.
	 */
	private Beans() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns a bean by name.
	 *
	 * @param <T> bean type
	 * @param beanName bean name
	 * @param ctx application context
	 * @param onError exception consumer
	 * @return a bean
	 */
	public static <T> T getBean(final String beanName, final ApplicationContext ctx, final Consumer<Exception> onError) {
		return getBean(() -> ctx.getBean(beanName), beanName, onError);
	}

	/**
	 * Returns a bean by name.
	 *
	 * @param <T> bean type
	 * @param beanName bean name
	 * @param ctx application context
	 * @return a bean
	 */
	public static <T> T getBean(final String beanName, final ApplicationContext ctx) {
		return getBean(beanName, ctx, e -> LOGGER.error(Message.BEAN_NOT_FOUND, beanName));
	}

	/**
	 * Returns a bean by name.
	 *
	 * @param <T> bean type
	 * @param beanName bean name
	 * @param neededInClass needed in the given class
	 * @param ctx application context
	 * @return a bean
	 */
	public static <T> T getBean(final String beanName, final Class<?> neededInClass, final ApplicationContext ctx) {
		return getBean(beanName, ctx, e -> LOGGER.error(Message.BEAN_NOT_FOUND_NEEDED_IN, beanName, neededInClass));
	}

	/**
	 * Returns a bean by name.
	 *
	 * @param <T> bean type
	 * @param beanClass bean class
	 * @param ctx application context
	 * @param onError exception consumer
	 * @return a bean
	 */
	public static <T> T getBean(final Class<?> beanClass, final ApplicationContext ctx, final Consumer<Exception> onError) {
		return getBean(() -> ctx.getBean(beanClass), beanClass, onError);
	}

	/**
	 * Returns a bean by name.
	 *
	 * @param <T> bean type
	 * @param beanClass bean class
	 * @param ctx application context
	 * @return a bean
	 */
	public static <T> T getBean(final Class<?> beanClass, final ApplicationContext ctx) {
		return getBean(beanClass, ctx, e -> LOGGER.error(Message.BEAN_NOT_FOUND, beanClass, e));
	}

	/**
	 * Returns a bean by name.
	 *
	 * @param <T> bean type
	 * @param beanClass bean class
	 * @param neededInClass needed in the given class
	 * @param ctx application context
	 * @return a bean
	 */
	public static <T> T getBean(final Class<?> beanClass, final Class<?> neededInClass, final ApplicationContext ctx) {
		return getBean(beanClass, ctx, e -> LOGGER.error(Message.BEAN_NOT_FOUND_NEEDED_IN, beanClass, neededInClass, e));
	}

	/**
	 * Returns a bean using the given supplier, which can be used to retrieve the bean from any source, such as an
	 * application context or a custom bean registry. The method handles any exceptions that may occur during the retrieval
	 * process and allows the caller to specify a custom error handling strategy through the {@code onError} consumer. If an
	 * exception occurs, the method logs the error and returns {@code null}.
	 *
	 * @param <T> the type of the bean to be retrieved
	 *
	 * @param beanSupplier a supplier that provides the logic to retrieve the bean
	 * @param beanId an identifier for the bean, used for logging purposes in case of an error
	 * @param onError a consumer that handles any exceptions that occur during the bean retrieval process
	 * @return the retrieved bean, or {@code null} if an error occurs
	 */
	public static <T> T getBean(final Supplier<Object> beanSupplier, final Object beanId, final Consumer<Exception> onError) {
		try {
			return JavaObjects.cast(beanSupplier.get());
		} catch (Exception e) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace(Message.BEAN_NOT_FOUND, beanId, e);
			}
			onError.accept(e);
			return null;
		}
	}

	/**
	 * Consumer to be used in conjunction with:
	 * <ul>
	 * <li>{@link #getBean(Class, ApplicationContext, Consumer)}</li>
	 * <li>{@link #getBean(String, ApplicationContext, Consumer)}</li>
	 * </ul>
	 * to automatically return {@code null} if the bean is not found.
	 *
	 * @return consumer that signifies that a null will be returned on error
	 */
	public static Consumer<Exception> nullOnError() {
		return Consumers.consumeNothing();
	}
}

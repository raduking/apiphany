package org.apiphany.spring;

import java.util.function.Consumer;

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
		try {
			return JavaObjects.cast(ctx.getBean(beanName));
		} catch (Exception e) {
			LOGGER.trace(Message.BEAN_NOT_FOUND, beanName);
			onError.accept(e);
			return null;
		}
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
		try {
			return JavaObjects.cast(ctx.getBean(beanClass));
		} catch (Exception e) {
			LOGGER.trace(Message.BEAN_NOT_FOUND, beanClass);
			onError.accept(e);
			return null;
		}
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
		return getBean(beanClass, ctx, e -> LOGGER.error(Message.BEAN_NOT_FOUND, beanClass));
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
		return getBean(beanClass, ctx, e -> LOGGER.error(Message.BEAN_NOT_FOUND_NEEDED_IN, beanClass, neededInClass));
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

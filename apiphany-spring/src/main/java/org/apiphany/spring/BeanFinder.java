package org.apiphany.spring;

import java.util.function.Consumer;

/**
 * Interface with default getBean methods which is useful when in a class hierarchy beans need to be retrieved from the
 * application context. This interface provides composition over inheritance, as it can be implemented by any class,
 * without the need to extend a specific base class.
 *
 * @author Radu Sebastian LAZIN
 */
public interface BeanFinder extends ApplicationContextCapable {

	/**
	 * Returns a bean by name.
	 *
	 * @param <T> bean type
	 *
	 * @param beanName bean name
	 * @return a bean
	 */
	default <T> T getBean(final String beanName) {
		return Beans.getBean(beanName, getClass(), getApplicationContext());
	}

	/**
	 * Returns a bean by name.
	 *
	 * @param <T> bean type
	 *
	 * @param beanName bean name
	 * @param onError exception consumer
	 * @return a bean
	 */
	default <T> T getBean(final String beanName, final Consumer<Exception> onError) {
		return Beans.getBean(beanName, getApplicationContext(), onError);
	}

	/**
	 * Returns a bean by name.
	 *
	 * @param <T> bean type
	 *
	 * @param beanClass bean class
	 * @return a bean
	 */
	default <T> T getBean(final Class<T> beanClass) {
		return Beans.getBean(beanClass, getClass(), getApplicationContext());
	}

	/**
	 * Returns a bean by name.
	 *
	 * @param <T> bean type
	 *
	 * @param beanClass bean class
	 * @param onError exception consumer
	 * @return a bean
	 */
	default <T> T getBean(final Class<T> beanClass, final Consumer<Exception> onError) {
		return Beans.getBean(beanClass, getApplicationContext(), onError);
	}
}

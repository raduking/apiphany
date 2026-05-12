package org.apiphany.spring;

import org.springframework.context.ApplicationContext;

/**
 * Interface indicating a component that contains and exposes an {@link ApplicationContext} reference.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ApplicationContextCapable {

	/**
	 * Returns the application context.
	 *
	 * @return the application context
	 */
	ApplicationContext getApplicationContext();
}

package org.apiphany.json.jackson2;

import java.util.function.Supplier;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Interface for providing Jackson 2 modules to be registered in the {@link Jackson2JsonBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Jackson2ModuleProvider {

	/**
	 * Returns the name of the module to be registered in the {@link Jackson2JsonBuilder}.
	 *
	 * @return the name of the module
	 */
	String getModuleName();

	/**
	 * Returns a supplier of the module to be registered in the {@link Jackson2JsonBuilder}. The supplier is used to create
	 * a new instance of the module each time it is registered.
	 *
	 * @return a supplier of the module
	 */
	Supplier<SimpleModule> getModuleSupplier();
}

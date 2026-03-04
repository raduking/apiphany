package org.apiphany.json.jackson3;

import java.util.function.Supplier;

import tools.jackson.databind.module.SimpleModule;

/**
 * Interface for providing Jackson 3 modules to be registered in the {@link Jackson3JsonBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Jackson3ModuleProvider {

	/**
	 * Returns the name of the module to be registered in the {@link Jackson3JsonBuilder}.
	 *
	 * @return the name of the module
	 */
	String getModuleName();

	/**
	 * Returns a supplier of the module to be registered in the {@link Jackson3JsonBuilder}. The supplier is used to create
	 * a new instance of the module each time it is registered.
	 *
	 * @return a supplier of the module
	 */
	Supplier<SimpleModule> getModuleSupplier();
}

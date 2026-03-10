package org.apiphany.json.jackson3;

import java.util.function.Supplier;

import tools.jackson.databind.module.SimpleModule;

/**
 * {@link Jackson3ModuleProvider} implementation for {@link ApiphanyHC5Jackson3Module}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiphanyHC5Jackson3ModuleProvider implements Jackson3ModuleProvider {

	/**
	 * Default constructor.
	 */
	public ApiphanyHC5Jackson3ModuleProvider() {
		// empty
	}

	/**
	 * @see Jackson3ModuleProvider#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return ApiphanyHC5Jackson3Module.NAME;
	}

	/**
	 * @see Jackson3ModuleProvider#getModuleSupplier()
	 */
	@Override
	public Supplier<SimpleModule> getModuleSupplier() {
		return ApiphanyHC5Jackson3Module::instance;
	}
}

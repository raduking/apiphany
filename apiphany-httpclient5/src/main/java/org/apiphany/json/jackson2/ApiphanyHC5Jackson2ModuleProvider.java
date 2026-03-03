package org.apiphany.json.jackson2;

import java.util.function.Supplier;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * {@link Jackson2ModuleProvider} implementation for {@link ApiphanyHC5Jackson2Module}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiphanyHC5Jackson2ModuleProvider implements Jackson2ModuleProvider {

	/**
	 * Default constructor.
	 */
	public ApiphanyHC5Jackson2ModuleProvider() {
		// empty
	}

	/**
	 * @see Jackson2ModuleProvider#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return ApiphanyHC5Jackson2Module.NAME;
	}

	/**
	 * @see Jackson2ModuleProvider#getModuleSupplier()
	 */
	@Override
	public Supplier<SimpleModule> getModuleSupplier() {
		return ApiphanyHC5Jackson2Module::instance;
	}
}

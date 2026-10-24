package org.apiphany.json.jackson2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.Module;

/**
 * Test class for {@link ApiphanyHC5Jackson2ModuleProvider}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiphanyHC5Jackson2ModuleProviderTest {

	@Test
	void shouldReturnTheApiphanyHC5ModuleName() {
		ApiphanyHC5Jackson2ModuleProvider provider = new ApiphanyHC5Jackson2ModuleProvider();

		assertThat(provider.getModuleName(), equalTo(ApiphanyHC5Jackson2Module.NAME));
	}

	@Test
	void shouldReturnASupplierOfTheApiphanyHC5Module() {
		ApiphanyHC5Jackson2ModuleProvider provider = new ApiphanyHC5Jackson2ModuleProvider();

		Supplier<Module> moduleSupplier = provider.getModuleSupplier();

		assertNotNull(moduleSupplier);
		assertThat(moduleSupplier.get(), instanceOf(ApiphanyHC5Jackson2Module.class));
	}

	@Test
	void shouldLoadThisProviderWithServiceLoader() {
		Supplier<? extends Module> moduleSupplier = Jackson2JsonBuilder.MODULES.get(ApiphanyHC5Jackson2Module.NAME);

		assertNotNull(moduleSupplier, "The ApiphanyHC5Jackson2Module provider should be loaded with ServiceLoader");
	}
}

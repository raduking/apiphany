package org.apiphany.json.jackson3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.module.SimpleModule;

/**
 * Test class for {@link ApiphanyHC5Jackson3ModuleProvider}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiphanyHC5Jackson3ModuleProviderTest {

	@Test
	void shouldReturnTheApiphanyHC5ModuleName() {
		ApiphanyHC5Jackson3ModuleProvider provider = new ApiphanyHC5Jackson3ModuleProvider();

		assertThat(provider.getModuleName(), equalTo(ApiphanyHC5Jackson3Module.NAME));
	}

	@Test
	void shouldReturnASupplierOfTheApiphanyHC5Module() {
		ApiphanyHC5Jackson3ModuleProvider provider = new ApiphanyHC5Jackson3ModuleProvider();

		Supplier<SimpleModule> moduleSupplier = provider.getModuleSupplier();

		assertNotNull(moduleSupplier);
		assertThat(moduleSupplier.get(), instanceOf(ApiphanyHC5Jackson3Module.class));
	}
}

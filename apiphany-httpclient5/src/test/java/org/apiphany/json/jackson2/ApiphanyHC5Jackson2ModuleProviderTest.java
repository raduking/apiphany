package org.apiphany.json.jackson2;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Test class for {@link ApiphanyHC5Jackson2ModuleProvider}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiphanyHC5Jackson2ModuleProviderTest {

	@Test
	void shouldLoadThisProviderWithServiceLoader() {
		Supplier<SimpleModule> moduleSupplier = Jackson2JsonBuilder.MODULES.get(ApiphanyHC5Jackson2Module.NAME);

		assertNotNull(moduleSupplier, "The ApiphanyHC5Jackson2Module provider should be loaded with ServiceLoader");
	}

}

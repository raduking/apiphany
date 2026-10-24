package org.apiphany.json.jackson3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.apache.hc.core5.util.Timeout;
import org.apiphany.json.jackson3.serializers.TimeoutDeserializer;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;

import tools.jackson.databind.JacksonModule.SetupContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.type.ClassKey;

/**
 * Test class for {@link ApiphanyHC5Jackson3Module}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiphanyHC5Jackson3ModuleTest {

	@Test
	void shouldHaveTheExpectedName() {
		assertThat(ApiphanyHC5Jackson3Module.NAME, equalTo("apiphany-hc5-jackson3"));
	}

	@Test
	void shouldSetTheModuleNameInTheConstructor() {
		SimpleModule module = new ApiphanyHC5Jackson3Module();

		assertThat(module.getModuleName(), equalTo(ApiphanyHC5Jackson3Module.NAME));
	}

	@Test
	void shouldInsertTheApiphanyAnnotationIntrospectorOnSetup() {
		ApiphanyHC5Jackson3Module module = new ApiphanyHC5Jackson3Module();
		SetupContext context = mock(SetupContext.class);

		module.setupModule(context);

		verify(context).insertAnnotationIntrospector(ApiphanyJackson3AnnotationIntrospector.getInstance());
	}

	@Test
	void shouldHaveDeserializerForTimeout() {
		SimpleModule module = ApiphanyHC5Jackson3Module.instance();

		SimpleDeserializers deserializers = Fields.IgnoreAccess.get(module, "_deserializers");
		Map<ClassKey, ValueDeserializer<?>> deserializerMap = Fields.IgnoreAccess.get(deserializers, "_classMappings");

		TimeoutDeserializer timeoutDeserializer = (TimeoutDeserializer) deserializerMap.get(new ClassKey(Timeout.class));

		assertNotNull(timeoutDeserializer);
	}

	@Test
	void shouldReturnTheSameInstance() {
		SimpleModule instance = ApiphanyHC5Jackson3Module.instance();

		assertThat(ApiphanyHC5Jackson3Module.instance(), sameInstance(instance));
	}

	@Test
	void shouldReturnASimpleModuleInstanceWhenTheLibraryIsPresent() {
		assertThat(ApiphanyHC5Jackson3Module.instance(), instanceOf(SimpleModule.class));
	}
}

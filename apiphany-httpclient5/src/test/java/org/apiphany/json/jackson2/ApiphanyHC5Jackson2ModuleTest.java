package org.apiphany.json.jackson2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.apache.hc.core5.util.Timeout;
import org.apiphany.json.jackson2.serializers.TimeoutDeserializer;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.ClassKey;

/**
 * Test class for {@link ApiphanyHC5Jackson2Module}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiphanyHC5Jackson2ModuleTest {

	@Test
	void shouldHaveTheExpectedName() {
		assertThat(ApiphanyHC5Jackson2Module.NAME, equalTo("apiphany-hc5-jackson2"));
	}

	@Test
	void shouldSetTheModuleNameInTheConstructor() {
		SimpleModule module = new ApiphanyHC5Jackson2Module();

		assertThat(module.getModuleName(), equalTo(ApiphanyHC5Jackson2Module.NAME));
	}

	@Test
	void shouldHaveDeserializerForTimeout() {
		SimpleModule module = ApiphanyHC5Jackson2Module.instance();

		SimpleDeserializers deserializers = Fields.IgnoreAccess.get(module, "_deserializers");
		Map<ClassKey, JsonDeserializer<?>> deserializerMap = Fields.IgnoreAccess.get(deserializers, "_classMappings");

		TimeoutDeserializer timeoutDeserializer = (TimeoutDeserializer) deserializerMap.get(new ClassKey(Timeout.class));

		assertNotNull(timeoutDeserializer);
	}

	@Test
	void shouldReturnTheSameInstance() {
		SimpleModule instance = ApiphanyHC5Jackson2Module.instance();

		assertThat(ApiphanyHC5Jackson2Module.instance(), sameInstance(instance));
	}

	@Test
	void shouldReturnASimpleModuleInstanceWhenTheLibraryIsPresent() {
		assertThat(ApiphanyHC5Jackson2Module.instance(), instanceOf(SimpleModule.class));
	}

	@Test
	void shouldInsertTheApiphanyAnnotationIntrospectorOnSetup() {
		ObjectMapper objectMapper = new ObjectMapper();

		objectMapper.registerModule(new ApiphanyHC5Jackson2Module());

		AnnotationIntrospector annotationIntrospector = objectMapper.getDeserializationConfig().getAnnotationIntrospector();

		assertThat(annotationIntrospector.allIntrospectors(), hasItem(ApiphanyJackson2AnnotationIntrospector.getInstance()));
	}
}

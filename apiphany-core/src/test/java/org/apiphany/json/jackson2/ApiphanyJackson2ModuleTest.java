package org.apiphany.json.jackson2;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.apiphany.RequestMethod;
import org.apiphany.json.jackson2.serializers.RequestMethodDeserializer;
import org.apiphany.json.jackson2.serializers.RequestMethodSerializer;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module.SetupContext;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.type.ClassKey;

/**
 * Test class for {@link ApiphanyJackson2Module}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiphanyJackson2ModuleTest {

	@Test
	void shouldSetUpModule() {
		ApiphanyJackson2Module module = new ApiphanyJackson2Module();
		SetupContext context = mock(SetupContext.class);

		module.setupModule(context);

		verify(context).insertAnnotationIntrospector(ApiphanyJackson2AnnotationIntrospector.getInstance());
	}

	@Test
	void shouldHaveTheProperSerializers() {
		SimpleModule module = ApiphanyJackson2Module.instance();

		SimpleSerializers serializers = Fields.IgnoreAccess.get(module, "_serializers");
		Map<ClassKey, JsonSerializer<?>> serializerMap = Fields.IgnoreAccess.get(serializers, "_classMappings");
		if (null == serializerMap) {
			serializerMap = Fields.IgnoreAccess.get(serializers, "_interfaceMappings");
		}

		RequestMethodSerializer rms = (RequestMethodSerializer) serializerMap.get(new ClassKey(RequestMethod.class));

		assertNotNull(rms);
	}

	@Test
	void shouldHaveTheProperDeserializers() {
		SimpleModule module = ApiphanyJackson2Module.instance();

		SimpleDeserializers deserializers = Fields.IgnoreAccess.get(module, "_deserializers");
		Map<ClassKey, JsonDeserializer<?>> deserializerMap = Fields.IgnoreAccess.get(deserializers, "_classMappings");

		RequestMethodDeserializer rmds = (RequestMethodDeserializer) deserializerMap.get(new ClassKey(RequestMethod.class));

		assertNotNull(rmds);
	}
}

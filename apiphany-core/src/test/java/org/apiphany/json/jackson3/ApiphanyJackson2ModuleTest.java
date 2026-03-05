package org.apiphany.json.jackson3;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.apiphany.RequestMethod;
import org.apiphany.json.jackson3.serializers.RequestMethodDeserializer;
import org.apiphany.json.jackson3.serializers.RequestMethodSerializer;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;

import tools.jackson.databind.JacksonModule.SetupContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.module.SimpleSerializers;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.databind.type.ClassKey;

/**
 * Test class for {@link ApiphanyJackson3Module}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiphanyJackson2ModuleTest {

	@Test
	void shouldSetUpModule() {
		ApiphanyJackson3Module module = new ApiphanyJackson3Module();
		SetupContext context = mock(SetupContext.class);

		module.setupModule(context);

		verify(context).insertAnnotationIntrospector(ApiphanyJackson3AnnotationIntrospector.getInstance());
	}

	@Test
	void shouldHaveTheProperSerializers() {
		SimpleModule module = ApiphanyJackson3Module.instance();

		SimpleSerializers serializers = Fields.IgnoreAccess.get(module, "_serializers");
		Map<ClassKey, StdSerializer<?>> serializerMap = Fields.IgnoreAccess.get(serializers, "_classMappings");
		if (null == serializerMap) {
			serializerMap = Fields.IgnoreAccess.get(serializers, "_interfaceMappings");
		}

		RequestMethodSerializer rms = (RequestMethodSerializer) serializerMap.get(new ClassKey(RequestMethod.class));

		assertNotNull(rms);
	}

	@Test
	void shouldHaveTheProperDeserializers() {
		SimpleModule module = ApiphanyJackson3Module.instance();

		SimpleDeserializers deserializers = Fields.IgnoreAccess.get(module, "_deserializers");
		Map<ClassKey, StdDeserializer<?>> deserializerMap = Fields.IgnoreAccess.get(deserializers, "_classMappings");

		RequestMethodDeserializer rmds = (RequestMethodDeserializer) deserializerMap.get(new ClassKey(RequestMethod.class));

		assertNotNull(rmds);
	}
}

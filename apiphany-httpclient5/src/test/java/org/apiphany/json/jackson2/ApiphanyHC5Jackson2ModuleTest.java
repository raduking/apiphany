package org.apiphany.json.jackson2;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.apache.hc.core5.util.Timeout;
import org.apiphany.json.jackson2.serializers.TimeoutDeserializer;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;

import com.fasterxml.jackson.databind.JsonDeserializer;
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
	void shouldHaveDeserializerForTimeout() {
		SimpleModule module = ApiphanyHC5Jackson2Module.instance();

		SimpleDeserializers deserializers = Fields.IgnoreAccess.get(module, "_deserializers");
		Map<ClassKey, JsonDeserializer<?>> deserializerMap = Fields.IgnoreAccess.get(deserializers, "_classMappings");

		TimeoutDeserializer timeoutDeserializer = (TimeoutDeserializer) deserializerMap.get(new ClassKey(Timeout.class));

		assertNotNull(timeoutDeserializer);
	}
}

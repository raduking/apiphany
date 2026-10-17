package org.apiphany.json.jackson3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;

import org.apiphany.RequestMethod;
import org.apiphany.json.jackson3.serializers.RequestMethodDeserializer;
import org.apiphany.json.jackson3.serializers.RequestMethodSerializer;
import org.apiphany.json.jackson3.serializers.TypeHolderDeserializer;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;
import org.morphix.reflection.TypeHolder;

import tools.jackson.databind.JacksonModule.SetupContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.module.SimpleSerializers;
import tools.jackson.databind.type.ClassKey;

/**
 * Test class for {@link ApiphanyJackson3Module}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiphanyJackson3ModuleTest {

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

		RequestMethodSerializer rms = (RequestMethodSerializer) findSerializer(serializers, RequestMethod.class);

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

	@Test
	void shouldRegisterTypeHolderDeserializer() {
		SimpleModule module = ApiphanyJackson3Module.instance();

		SimpleDeserializers deserializers = Fields.IgnoreAccess.get(module, "_deserializers");
		Map<ClassKey, StdDeserializer<?>> deserializerMap = Fields.IgnoreAccess.get(deserializers, "_classMappings");

		StdDeserializer<?> deserializer = deserializerMap.get(new ClassKey(TypeHolder.class));

		assertInstanceOf(TypeHolderDeserializer.class, deserializer);
	}

	@Test
	void shouldSerializeDatesWithTheModule() {
		JsonMapper mapper = JsonMapper.builder()
				.addModule(ApiphanyJackson3Module.instance())
				.build();

		Date date = Date.valueOf(LocalDate.of(2024, 1, 2));

		String keyJson = mapper.writeValueAsString(Map.of(date, "value"));
		String valueJson = mapper.writeValueAsString(Map.of("value", date));

		assertThat(keyJson, equalTo("{\"2024-01-02\":\"value\"}"));
		assertThat(valueJson, equalTo("{\"value\":\"2024-01-02\"}"));
	}

	/**
	 * Looks up a serializer by type in both the class and interface mappings, since interface types (like
	 * {@link RequestMethod}) are stored separately from class types.
	 *
	 * @param serializers the serializers to look into
	 * @param type the type of the serializer
	 * @return the serializer for the given type or {@code null} if none is found
	 */
	private static ValueSerializer<?> findSerializer(final SimpleSerializers serializers, final Class<?> type) {
		Map<ClassKey, ValueSerializer<?>> classMappings = Fields.IgnoreAccess.get(serializers, "_classMappings");
		if (null != classMappings) {
			ValueSerializer<?> serializer = classMappings.get(new ClassKey(type));
			if (null != serializer) {
				return serializer;
			}
		}
		Map<ClassKey, ValueSerializer<?>> interfaceMappings = Fields.IgnoreAccess.get(serializers, "_interfaceMappings");
		return null == interfaceMappings ? null : interfaceMappings.get(new ClassKey(type));
	}
}

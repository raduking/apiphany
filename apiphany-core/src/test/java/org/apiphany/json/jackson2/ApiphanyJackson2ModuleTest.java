package org.apiphany.json.jackson2;

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
import org.apiphany.json.jackson2.serializers.RequestMethodDeserializer;
import org.apiphany.json.jackson2.serializers.RequestMethodSerializer;
import org.apiphany.json.jackson2.serializers.TypeHolderDeserializer;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;
import org.morphix.reflection.TypeHolder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module.SetupContext;
import com.fasterxml.jackson.databind.ObjectMapper;
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

		RequestMethodSerializer rms = (RequestMethodSerializer) findSerializer(serializers, RequestMethod.class);

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

	@Test
	void shouldRegisterTypeHolderDeserializer() {
		SimpleModule module = ApiphanyJackson2Module.instance();

		SimpleDeserializers deserializers = Fields.IgnoreAccess.get(module, "_deserializers");
		Map<ClassKey, JsonDeserializer<?>> deserializerMap = Fields.IgnoreAccess.get(deserializers, "_classMappings");

		JsonDeserializer<?> deserializer = deserializerMap.get(new ClassKey(TypeHolder.class));

		assertInstanceOf(TypeHolderDeserializer.class, deserializer);
	}

	@Test
	void shouldSerializeDateMapKeysWithTheModule() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(ApiphanyJackson2Module.instance());

		Date date = Date.valueOf(LocalDate.of(2024, 1, 2));

		String json = mapper.writeValueAsString(Map.of(date, "value"));

		assertThat(json, equalTo("{\"2024-01-02\":\"value\"}"));
	}

	/**
	 * Looks up a serializer by type in both the class and interface mappings, since interface types (like
	 * {@link RequestMethod}) are stored separately from class types.
	 *
	 * @param serializers the serializers to look into
	 * @param type the type of the serializer
	 * @return the serializer for the given type or {@code null} if none is found
	 */
	private static JsonSerializer<?> findSerializer(final SimpleSerializers serializers, final Class<?> type) {
		Map<ClassKey, JsonSerializer<?>> classMappings = Fields.IgnoreAccess.get(serializers, "_classMappings");
		if (null != classMappings) {
			JsonSerializer<?> serializer = classMappings.get(new ClassKey(type));
			if (null != serializer) {
				return serializer;
			}
		}
		Map<ClassKey, JsonSerializer<?>> interfaceMappings = Fields.IgnoreAccess.get(serializers, "_interfaceMappings");
		return null == interfaceMappings ? null : interfaceMappings.get(new ClassKey(type));
	}
}

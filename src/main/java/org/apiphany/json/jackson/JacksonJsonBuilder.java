package org.apiphany.json.jackson;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import org.apiphany.RequestMethod;
import org.apiphany.json.JsonBuilder;
import org.apiphany.json.jackson.serializers.RequestMethodDeserializer;
import org.apiphany.json.jackson.serializers.RequestMethodSerializer;
import org.morphix.lang.JavaObjects;
import org.morphix.reflection.GenericClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

/**
 * This will serialize/de-serialize any JSON serializable/deserializable object to {@link String}/{@link Object}. This
 * class is used to uniformly serialize/de-serialize objects across the entire project.
 * <p>
 * For serialization to {@link String} use {@link #toJson(Object)}.<br/>
 * For de-serialization {@link Object} use {@link #fromJson(String, Class)} or {@link #fromJson(String, GenericClass)}
 * or {@link #fromJson(String, TypeReference)}.
 * <p>
 * To indent the JSON output, use the {@code json-builder.to-json.indent-output} property set to {@code true}.
 * <p>
 * Note: For indentation use: {@link #indentOutput(boolean)} with {@code true} to indent output and {@code false} to
 * have a single line JSON string.
 *
 * @author Radu Sebastian LAZIN
 */
public final class JacksonJsonBuilder extends JsonBuilder { // NOSONAR singleton implementation

	private static final Logger LOGGER = LoggerFactory.getLogger(JacksonJsonBuilder.class);

	/**
	 * Error message logged when an object could not be serialized.
	 */
	static final String LOG_MSG_COULD_NOT_SERIALIZE_OBJECT = "Could not serialize object: {}";

	/**
	 * Error message logged when an object could not be de-serialized.
	 */
	static final String LOG_MSG_COULD_NOT_DESERIALIZE_OBJECT = "Could not deserialize object: {}";

	/**
	 * Singleton instance holder.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * Singleton instance.
		 */
		private static final JacksonJsonBuilder INSTANCE = new JacksonJsonBuilder();
	}

	/**
	 * The underlying {@link ObjectMapper}.
	 */
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Hide constructor.
	 */
	JacksonJsonBuilder() {
		objectMapper.registerModule(newJavaTimeModule(DateTimeFormatter.ISO_DATE_TIME));
		objectMapper.registerModule(customSerializationModule());
		indentOutput(isIndentOutput());
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	/**
	 * Transforms the parameter to a JSON String.
	 *
	 * @param <T> type of the object
	 *
	 * @param obj object to transform
	 * @return JSON String if conversion is possible, <code>null</code> if parameter is <code>null</code>,
	 * {@link #toString(Object)} otherwise.
	 */
	public static <T> String toJson(final T obj) {
		return InstanceHolder.INSTANCE.toJsonString(obj);
	}

	/**
	 * Returns an object from the JSON string.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON string
	 * @param cls class of the object
	 * @return an object from the JSON string
	 */
	public static <T> T fromJson(final String json, final Class<T> cls) {
		return InstanceHolder.INSTANCE.fromJsonString(json, cls);
	}

	/**
	 * Returns an object from the JSON string.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON string
	 * @param genericClass generic class wrapper for the type of the generic object
	 * @return an object from the JSON string
	 */
	public static <T> T fromJson(final String json, final GenericClass<T> genericClass) {
		return InstanceHolder.INSTANCE.fromJsonString(json, genericClass);
	}

	/**
	 * Returns an object from the JSON string.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON string
	 * @param typeReference type of the object
	 * @return an object from the JSON string
	 */
	public static <T> T fromJson(final String json, final TypeReference<T> typeReference) {
		return InstanceHolder.INSTANCE.fromJsonString(json, typeReference);
	}

	/**
	 * Transforms the parameter to a JSON String.
	 *
	 * @param <T> type of the object
	 *
	 * @param obj object to transform
	 * @return JSON String if conversion is possible, <code>null</code> if parameter is <code>null</code>,
	 * {@link #toString(Object)} otherwise.
	 */
	@Override
	public <T> String toJsonString(final T obj) {
		if (null == obj) {
			return null;
		}
		if (isDebugString()) {
			return toDebugString(obj);
		}
		ObjectWriter objectWriter = objectMapper.writerFor(obj.getClass());
		try {
			return eol() + objectWriter.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			LOGGER.warn(LOG_MSG_COULD_NOT_SERIALIZE_OBJECT, toString(obj), e);
		}
		return toString(obj);
	}

	/**
	 * Returns an object from the JSON string.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON string
	 * @param cls class of the object
	 * @return an object from the JSON string
	 */
	@Override
	public <T> T fromJsonString(final String json, final Class<T> cls) {
		try {
			return objectMapper.readValue(json, cls);
		} catch (JsonProcessingException e) {
			LOGGER.warn(LOG_MSG_COULD_NOT_DESERIALIZE_OBJECT, json, e);
			return null;
		}
	}

	/**
	 * Returns an object from the JSON string.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON string
	 * @param genericClass generic class wrapper for the type of the generic object
	 * @return an object from the JSON string
	 */
	@Override
	public <T> T fromJsonString(final String json, final GenericClass<T> genericClass) {
		TypeReference<T> typeReference = new TypeReference<>() {
			@Override
			public Type getType() {
				return genericClass.getType();
			}
		};
		return fromJsonString(json, typeReference);
	}

	/**
	 * Returns an object from the JSON string.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON string
	 * @param typeReference type of the object
	 * @return an object from the JSON string
	 */
	public <T> T fromJsonString(final String json, final TypeReference<T> typeReference) {
		try {
			return objectMapper.readValue(json, typeReference);
		} catch (JsonProcessingException e) {
			LOGGER.warn(LOG_MSG_COULD_NOT_DESERIALIZE_OBJECT, json, e);
			return null;
		}
	}

	/**
	 * Returns an object from a properties map. The properties map should use the kebab-case naming strategy.
	 *
	 * @param <T> return type
	 *
	 * @param propertiesMap the map with the properties
	 * @param cls class of the return type
	 * @param onError on error exception consumer
	 * @return wanted object
	 */
	@Override
	public <T> T fromPropertiesMap(final Map<String, Object> propertiesMap, final Class<T> cls, final Consumer<Exception> onError) {
		final ObjectMapper propertiesObjectMapper = objectMapper.copy()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
		try {
			String json = propertiesObjectMapper.writeValueAsString(propertiesMap);
			return JavaObjects.cast(propertiesObjectMapper.readValue(json, cls));
		} catch (Exception e) {
			onError.accept(e);
			return null;
		}
	}

	/**
	 * Returns a properties map from an object. The properties map uses the kebab-case naming strategy.
	 *
	 * @param <T> properties object type
	 *
	 * @param properties properties object
	 * @param onError on error exception consumer
	 * @return properties map
	 */
	@Override
	public <T> Map<String, Object> toPropertiesMap(final T properties, final Consumer<Exception> onError) {
		final ObjectMapper propertiesObjectMapper = objectMapper.copy()
				.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
		try {
			return propertiesObjectMapper.convertValue(properties, new TypeReference<>() {
				// empty
			});
		} catch (Exception e) {
			onError.accept(e);
			return Collections.emptyMap();
		}
	}

	/**
	 * Creates a new JavaTimeModule with ISO formatters
	 *
	 * @param dateTimeFormatter the date time formatter object
	 * @return java time module
	 */
	public static SimpleModule newJavaTimeModule(final DateTimeFormatter dateTimeFormatter) {
		return new JavaTimeModule()
				.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter))
				.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
	}

	/**
	 * Creates a new SimpleModule with custom serializers/deserializers.
	 *
	 * @return simple module
	 */
	public static SimpleModule customSerializationModule() {
		return new SimpleModule("apiphany")
				.addSerializer(RequestMethod.class, new RequestMethodSerializer())
				.addDeserializer(RequestMethod.class, new RequestMethodDeserializer());
	}

	/**
	 * Enable/disable JSON indentation.
	 *
	 * @param enable true to enable, false to disable
	 */
	@Override
	public void indentOutput(final boolean enable) {
		super.indentOutput(enable);
		Consumer<SerializationFeature> indentation = enable ? objectMapper::enable : objectMapper::disable;
		indentation.accept(SerializationFeature.INDENT_OUTPUT);
	}

}

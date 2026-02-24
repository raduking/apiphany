package org.apiphany.json.jackson2;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apiphany.json.JsonBuilder;
import org.morphix.reflection.GenericClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
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
 * For de-serialization {@link Object} use {@link #fromJson(Object, Class)} or {@link #fromJson(Object, GenericClass)}
 * or {@link #fromJson(Object, TypeReference)}.
 * <p>
 * To indent the JSON output, use the {@code json-builder.to-json.indent-output} property set to {@code true}.
 * <p>
 * Note: For indentation use: {@link #indentOutput(boolean)} with {@code true} to indent output and {@code false} to
 * have a single line JSON string.
 *
 * @author Radu Sebastian LAZIN
 */
public final class Jackson2JsonBuilder extends JsonBuilder { // NOSONAR singleton implementation

	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Jackson2JsonBuilder.class);

	/**
	 * Singleton instance holder.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * Singleton instance.
		 */
		private static final Jackson2JsonBuilder INSTANCE = new Jackson2JsonBuilder();
	}

	/**
	 * Thread local override for the singleton instance.
	 * <p>
	 * The {@link ThreadLocal#remove()} is handled correctly in #with(Jackson2JsonBuilder, Supplier) which calls
	 * {@link #with(Object, ThreadLocal, Supplier)} so no memory leak problems occur.
	 */
	private static final ThreadLocal<Jackson2JsonBuilder> OVERRIDE = new ThreadLocal<>(); // NOSONAR see JavaDoc

	/**
	 * The underlying {@link ObjectMapper}.
	 */
	protected final ObjectMapper objectMapper;

	/**
	 * The default annotation introspector.
	 */
	protected final AnnotationIntrospector defaultAnnotationIntrospector;

	/**
	 * Hide constructor.
	 *
	 * @param objectMapper the object mapper to use
	 */
	Jackson2JsonBuilder(final ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.objectMapper.registerModule(newJavaTimeModule(DateTimeFormatter.ISO_DATE_TIME));
		this.objectMapper.registerModule(apiphanySerializationModule());
		indentOutput(isIndentOutput());
		this.objectMapper.setSerializationInclusion(Include.NON_NULL);
		this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		this.defaultAnnotationIntrospector = objectMapper.getSerializationConfig().getAnnotationIntrospector();
		configureSensitivity(SensitiveJackson2AnnotationIntrospector.hideSensitive());
	}

	/**
	 * Hide constructor.
	 */
	Jackson2JsonBuilder() {
		this(new ObjectMapper());
	}

	/**
	 * Since the {@link ObjectMapper} cannot be configured to use a different {@link JsonFactory} after its creation, this
	 * constructor allows creating a new {@link Jackson2JsonBuilder} with a custom {@link JsonFactory}.
	 *
	 * @param jsonFactory the JSON factory to use for the underlying {@link ObjectMapper}, e.g. for YAML support.
	 */
	public Jackson2JsonBuilder(final JsonFactory jsonFactory) {
		this(new ObjectMapper(jsonFactory));
	}

	/**
	 * Returns the runtime JSON builder instance. If a thread local override is set it returns the override, otherwise it
	 * returns the singleton instance.
	 *
	 * @return the runtime JSON builder instance
	 */
	public static Jackson2JsonBuilder runtime() {
		return runtime(OVERRIDE, InstanceHolder.INSTANCE);
	}

	/**
	 * Executes the supplier with the provided JSON builder.
	 *
	 * @param <T> return type of the supplier
	 *
	 * @param builder the JSON builder to use during the execution of the supplier
	 * @param supplier the supplier to execute with the provided JSON builder
	 * @return the result of the supplier execution
	 */
	public static <T> T with(final Jackson2JsonBuilder builder, final Supplier<T> supplier) {
		return with(builder, OVERRIDE, supplier);
	}

	/**
	 * Transforms the parameter to a JSON String.
	 *
	 * @param <T> type of the object
	 *
	 * @param obj object to transform
	 * @return JSON String if conversion is possible, <code>null</code> if parameter is <code>null</code>,
	 * {@link #toIdentityJsonString(Object)} otherwise.
	 */
	public static <T> String toJson(final T obj) {
		return runtime().toJsonString(obj);
	}

	/**
	 * Returns an object from the JSON input object.
	 *
	 * @param <O> type of the input object
	 * @param <T> type of the resulting object
	 *
	 * @param json JSON input object
	 * @param cls class of the output object
	 * @return an object from the JSON input object
	 */
	public static <O, T> T fromJson(final O json, final Class<T> cls) {
		return switch (json) {
			case String string -> runtime().fromJsonString(string, cls);
			case byte[] bytes -> runtime().fromJsonBytes(bytes, cls);
			case InputStream inputStream -> runtime().fromJsonInputStream(inputStream, cls);
			default -> throw unsupportedJsonInputType(json);
		};
	}

	/**
	 * Returns an object from the JSON input object.
	 *
	 * @param <O> type of the input object
	 * @param <T> type of the resulting object
	 *
	 * @param json JSON input object
	 * @param genericClass generic class wrapper for the type of the generic output object
	 * @return an object from the JSON input object
	 */
	public static <O, T> T fromJson(final O json, final GenericClass<T> genericClass) {
		return switch (json) {
			case String string -> runtime().fromJsonString(string, genericClass);
			case byte[] bytes -> runtime().fromJsonBytes(bytes, genericClass);
			case InputStream inputStream -> runtime().fromJsonInputStream(inputStream, genericClass);
			default -> throw unsupportedJsonInputType(json);
		};
	}

	/**
	 * Returns an object from the JSON input object.
	 *
	 * @param <O> type of the input object
	 * @param <T> type of the resulting object
	 *
	 * @param json JSON input object
	 * @param typeReference type of the output object
	 * @return an object from the JSON input object
	 */
	public static <O, T> T fromJson(final O json, final TypeReference<T> typeReference) {
		return switch (json) {
			case String string -> runtime().fromJsonString(string, typeReference);
			case byte[] bytes -> runtime().fromJsonBytes(bytes, typeReference);
			case InputStream inputStream -> runtime().fromJsonInputStream(inputStream, typeReference);
			default -> throw unsupportedJsonInputType(json);
		};
	}

	/**
	 * Transforms the parameter to a JSON String. If the object is null, returns null. If the object cannot be serialized,
	 * returns the result of {@link #toIdentityJsonString(Object)}.
	 *
	 * @param <T> type of the object
	 *
	 * @param obj object to transform
	 * @return JSON String if conversion is possible, <code>null</code> if parameter is <code>null</code>,
	 * {@link #toIdentityJsonString(Object)} otherwise.
	 */
	@Override
	public <T> String toJsonString(final T obj) {
		if (isDebugString()) {
			return toDebugString(obj);
		}
		if (null == obj) {
			return null;
		}
		ObjectWriter objectWriter = objectMapper.writerFor(obj.getClass());
		try {
			return eol() + objectWriter.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			String result = toIdentityJsonString(obj);
			LOGGER.warn(ErrorMessage.COULD_NOT_SERIALIZE_OBJECT, result, e);
			return result;
		}
	}

	/**
	 * Returns an object from the JSON string. If the JSON string cannot be de-serialized, returns null.
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
			LOGGER.warn(ErrorMessage.COULD_NOT_DESERIALIZE_OBJECT, json, e);
			return null;
		}
	}

	/**
	 * Returns an object from the JSON string. If the JSON string cannot be de-serialized, returns null.
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
	 * Returns an object from the JSON string. If the JSON string cannot be de-serialized, returns null.
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
			LOGGER.warn(ErrorMessage.COULD_NOT_DESERIALIZE_OBJECT, json, e);
			return null;
		}
	}

	/**
	 * Returns an object from the given byte array. If the byte array cannot be de-serialized, returns null.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON byte array
	 * @param cls class of the object
	 * @return an object from the JSON string
	 */
	public <T> T fromJsonBytes(final byte[] json, final Class<T> cls) {
		try {
			return objectMapper.readValue(json, cls);
		} catch (IOException e) {
			LOGGER.warn(ErrorMessage.COULD_NOT_DESERIALIZE_OBJECT, json, e);
			return null;
		}
	}

	/**
	 * Returns an object from the JSON byte array. If the byte array cannot be de-serialized, returns null.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON byte array
	 * @param genericClass generic class wrapper for the type of the generic object
	 * @return an object from the JSON string
	 */
	public <T> T fromJsonBytes(final byte[] json, final GenericClass<T> genericClass) {
		TypeReference<T> typeReference = new TypeReference<>() {
			@Override
			public Type getType() {
				return genericClass.getType();
			}
		};
		return fromJsonBytes(json, typeReference);
	}

	/**
	 * Returns an object from the JSON byte array. If the byte array cannot be de-serialized, returns null.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON byte array
	 * @param typeReference type of the object
	 * @return an object from the JSON string
	 */
	public <T> T fromJsonBytes(final byte[] json, final TypeReference<T> typeReference) {
		try {
			return objectMapper.readValue(json, typeReference);
		} catch (IOException e) {
			LOGGER.warn(ErrorMessage.COULD_NOT_DESERIALIZE_OBJECT, json, e);
			return null;
		}
	}

	/**
	 * Returns an object from the given input stream. If the byte array cannot be de-serialized, returns null.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON input stream
	 * @param cls class of the object
	 * @return an object from the JSON string
	 */
	public <T> T fromJsonInputStream(final InputStream json, final Class<T> cls) {
		try {
			return objectMapper.readValue(json, cls);
		} catch (IOException e) {
			LOGGER.warn(ErrorMessage.COULD_NOT_DESERIALIZE_OBJECT, json, e);
			return null;
		}
	}

	/**
	 * Returns an object from the given input stream. If the byte array cannot be de-serialized, returns null.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON input stream
	 * @param genericClass generic class wrapper for the type of the generic object
	 * @return an object from the JSON string
	 */
	public <T> T fromJsonInputStream(final InputStream json, final GenericClass<T> genericClass) {
		TypeReference<T> typeReference = new TypeReference<>() {
			@Override
			public Type getType() {
				return genericClass.getType();
			}
		};
		return fromJsonInputStream(json, typeReference);
	}

	/**
	 * Returns an object from the given input stream. If the byte array cannot be de-serialized, returns null.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON input stream
	 * @param typeReference type of the object
	 * @return an object from the JSON string
	 */
	public <T> T fromJsonInputStream(final InputStream json, final TypeReference<T> typeReference) {
		try {
			return objectMapper.readValue(json, typeReference);
		} catch (IOException e) {
			LOGGER.warn(ErrorMessage.COULD_NOT_DESERIALIZE_OBJECT, json, e);
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
			return propertiesObjectMapper.readValue(json, cls);
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
	@SuppressWarnings("unchecked")
	@Override
	public <T> Map<String, Object> toPropertiesMap(final T properties, final Consumer<Exception> onError) {
		final ObjectMapper propertiesObjectMapper = objectMapper.copy()
				.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
		configureSensitivity(propertiesObjectMapper,
				SensitiveJackson2AnnotationIntrospector.allowSensitive(), defaultAnnotationIntrospector);
		try {
			return propertiesObjectMapper.convertValue(properties, Map.class);
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
	 * Returns a {@link SimpleModule} with custom serializers/deserializers named {@link ApiphanyJackson2Module#NAME}.
	 *
	 * @return simple module
	 */
	public static SimpleModule apiphanySerializationModule() {
		return ApiphanyJackson2Module.instance();
	}

	/**
	 * Configures the underlying {@link ObjectMapper} with the given {@link SensitiveJackson2AnnotationIntrospector}.
	 *
	 * @param sensitiveAnnotationIntrospector the sensitive annotation introspector
	 */
	public void configureSensitivity(final SensitiveJackson2AnnotationIntrospector sensitiveAnnotationIntrospector) {
		configureSensitivity(objectMapper, sensitiveAnnotationIntrospector, defaultAnnotationIntrospector);
	}

	/**
	 * Configures the given {@link ObjectMapper} with the given {@link SensitiveJackson2AnnotationIntrospector}.
	 *
	 * @param objectMapper the object mapper to configure
	 * @param sensitiveAnnotationIntrospector the sensitive annotation introspector
	 * @return the configured object mapper
	 */
	public static ObjectMapper configureSensitivity(final ObjectMapper objectMapper,
			final SensitiveJackson2AnnotationIntrospector sensitiveAnnotationIntrospector) {
		AnnotationIntrospector baseAnnotationIntrospector = objectMapper.getSerializationConfig().getAnnotationIntrospector();
		return configureSensitivity(objectMapper, sensitiveAnnotationIntrospector, baseAnnotationIntrospector);
	}

	/**
	 * Configures the given {@link ObjectMapper} with the given {@link SensitiveJackson2AnnotationIntrospector}.
	 *
	 * @param objectMapper the object mapper to configure
	 * @param sensitiveAnnotationIntrospector the sensitive annotation introspector
	 * @param baseAnnotationIntrospector the base annotation introspector
	 * @return the configured object mapper
	 */
	public static ObjectMapper configureSensitivity(final ObjectMapper objectMapper,
			final SensitiveJackson2AnnotationIntrospector sensitiveAnnotationIntrospector, final AnnotationIntrospector baseAnnotationIntrospector) {
		return objectMapper.setAnnotationIntrospector(
				AnnotationIntrospector.pair(sensitiveAnnotationIntrospector, baseAnnotationIntrospector));
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

	/**
	 * Returns the underlying {@link ObjectMapper}.
	 *
	 * @return the underlying {@link ObjectMapper}
	 */
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}

package org.apiphany.json.jackson;

import java.io.IOException;
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
import org.apiphany.lang.Pair;
import org.morphix.lang.JavaObjects;
import org.morphix.reflection.GenericClass;
import org.morphix.reflection.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
public final class JacksonJsonBuilder extends JsonBuilder { // NOSONAR singleton implementation

	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JacksonJsonBuilder.class);

	/**
	 * The custom serialization module name.
	 */
	public static final String APIPHANY_MODULE = "apiphany";

	/**
	 * Jackson JSON library ObjectMapper class name.
	 */
	private static final String JACKSON_OBJECT_MAPPER_CLASS_NAME = "com.fasterxml.jackson.databind.ObjectMapper";

	/**
	 * Pair that shows if Jackson JSON library is present in the class path and the {@link JsonBuilder} specific class.
	 */
	public static final Pair<Boolean, Class<? extends JsonBuilder>> JACKSON_LIBRARY_INFO =
			Pair.of(Reflection.isClassPresent(JACKSON_OBJECT_MAPPER_CLASS_NAME), JacksonJsonBuilder.class);

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
	 * The default annotation introspector.
	 */
	private final AnnotationIntrospector defaultAnnotationIntrospector;

	/**
	 * Hide constructor.
	 */
	JacksonJsonBuilder() {
		this.objectMapper.registerModule(newJavaTimeModule(DateTimeFormatter.ISO_DATE_TIME));
		this.objectMapper.registerModule(apiphanySerializationModule());
		indentOutput(isIndentOutput());
		this.objectMapper.setSerializationInclusion(Include.NON_NULL);
		this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		this.defaultAnnotationIntrospector = objectMapper.getSerializationConfig().getAnnotationIntrospector();
		this.objectMapper.setAnnotationIntrospector(
				AnnotationIntrospector.pair(
						SensitiveAnnotationIntrospector.hideSensitive(),
						defaultAnnotationIntrospector));
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
			case String string -> InstanceHolder.INSTANCE.fromJsonString(string, cls);
			case byte[] bytes -> InstanceHolder.INSTANCE.fromJsonBytes(bytes, cls);
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
			case String string -> InstanceHolder.INSTANCE.fromJsonString(string, genericClass);
			case byte[] bytes -> InstanceHolder.INSTANCE.fromJsonBytes(bytes, genericClass);
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
			case String string -> InstanceHolder.INSTANCE.fromJsonString(string, typeReference);
			case byte[] bytes -> InstanceHolder.INSTANCE.fromJsonBytes(bytes, typeReference);
			default -> throw unsupportedJsonInputType(json);
		};
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
			LOGGER.warn(ErrorMessage.COULD_NOT_SERIALIZE_OBJECT, toString(obj), e);
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
			LOGGER.warn(ErrorMessage.COULD_NOT_DESERIALIZE_OBJECT, json, e);
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
			LOGGER.warn(ErrorMessage.COULD_NOT_DESERIALIZE_OBJECT, json, e);
			return null;
		}
	}

	/**
	 * Returns an object from the given byte array.
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
	 * Returns an object from the JSON byte array.
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
	 * Returns an object from the JSON byte array.
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
	@SuppressWarnings("unchecked")
	@Override
	public <T> Map<String, Object> toPropertiesMap(final T properties, final Consumer<Exception> onError) {
		final ObjectMapper propertiesObjectMapper = objectMapper.copy()
				.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
				.setAnnotationIntrospector(
						AnnotationIntrospector.pair(
								SensitiveAnnotationIntrospector.allowSensitive(),
								defaultAnnotationIntrospector));
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
	 * Creates a new {@link SimpleModule} with custom serializers/deserializers named {@link #APIPHANY_MODULE}.
	 *
	 * @return simple module
	 */
	public static SimpleModule apiphanySerializationModule() {
		SimpleModule apiphanyModule = new SimpleModule(APIPHANY_MODULE) {

			private static final long serialVersionUID = -1205949335841515195L;

			@Override
			public void setupModule(final SetupContext context) {
				super.setupModule(context);
				context.insertAnnotationIntrospector(ApiphanyAnnotationIntrospector.getInstance());
			}
		};
		return apiphanyModule
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

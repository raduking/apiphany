package org.apiphany.json.jackson3;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.Consumers;
import org.morphix.reflection.GenericClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;

import tools.jackson.core.JacksonException;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

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
public class Jackson3JsonBuilder extends JsonBuilder { // NOSONAR singleton implementation

	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Jackson3JsonBuilder.class);

	/**
	 * Singleton instance holder.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * Singleton instance.
		 */
		private static final Jackson3JsonBuilder INSTANCE = create();

		/**
		 * Creates the singleton instance.
		 *
		 * @return the singleton instance
		 */
		private static Jackson3JsonBuilder create() {
			Jackson3JsonBuilder instance = new Jackson3JsonBuilder();
			Jackson3JsonBuilder.singletonMaterialized = true;
			return instance;
		}
	}

	/**
	 * Thread local override for the singleton instance.
	 * <p>
	 * The {@link ThreadLocal#remove()} is handled correctly in #with(Jackson2JsonBuilder, Supplier) which calls
	 * {@link JsonBuilder#with(JsonBuilder, ThreadLocal, Supplier)} so no memory leak problems occur.
	 */
	private static final ThreadLocal<Jackson3JsonBuilder> OVERRIDE = new ThreadLocal<>(); // NOSONAR see JavaDoc

	/**
	 * Map of modules to register to the underlying {@link JsonMapper}. The key is the module class name. This allows
	 * registering modules to the underlying {@link JsonMapper} without creating a dependency on the module's library.
	 */
	protected static final Map<String, Supplier<SimpleModule>> MODULES = new ConcurrentHashMap<>();
	static {
		registerModule(ApiphanyJackson3Module.NAME, () -> apiphanySerializationModule());
		registerModules(ServiceLoader.load(Jackson3ModuleProvider.class));
	}

	/**
	 * Flag to indicate if the singleton instance has been created and initialized. This is used to log a warning if a
	 * module is registered after the singleton instance has been initialized, as the module will not be registered to the
	 * singleton instance's underlying {@link JsonMapper}.
	 */
	private static volatile boolean singletonMaterialized;

	/**
	 * The underlying {@link JsonMapper}.
	 */
	protected final JsonMapper jsonMapper;

	/**
	 * The default annotation introspector.
	 */
	protected final AnnotationIntrospector defaultAnnotationIntrospector;

	/**
	 * The JSON mapper builder used to create the underlying {@link JsonMapper}. This is used to create new
	 * {@link JsonMapper} instances with the registered modules when needed, e.g. for the {@link #custom(JsonFactory)}
	 * method.
	 */
	protected final JsonMapper.Builder jsonMapperBuilder;

	/**
	 * Hide constructor.
	 *
	 * @param jsonMapper the object mapper to use
	 */
	Jackson3JsonBuilder(final JsonMapper.Builder builder, final Consumer<JsonMapper.Builder> builderCustomizer) {
		this.jsonMapperBuilder = builder;

		for (Supplier<SimpleModule> moduleSupplier : MODULES.values()) {
			builder.addModule(moduleSupplier.get());
		}
		builder.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		builder.changeDefaultPropertyInclusion(inclusion -> inclusion
				.withContentInclusion(Include.NON_NULL)
				.withValueInclusion(Include.NON_NULL));

		Consumer<SerializationFeature> indentation = isIndentOutput() ? builder::enable : builder::disable;
		indentation.accept(SerializationFeature.INDENT_OUTPUT);

		this.defaultAnnotationIntrospector = builder.annotationIntrospector();
		configureSensitivity(SensitiveJackson3AnnotationIntrospector.hideSensitive());

		builderCustomizer.accept(builder);
		indentOutput(builder.isEnabled(SerializationFeature.INDENT_OUTPUT));

		this.jsonMapper = builder.build();
	}

	/**
	 * Hide constructor.
	 */
	Jackson3JsonBuilder() {
		this(JsonMapper.builder(), Consumers.noConsumer());
	}

	/**
	 * Returns the runtime JSON builder instance. If a thread local override is set it returns the override, otherwise it
	 * returns the singleton instance.
	 *
	 * @return the runtime JSON builder instance
	 */
	public static Jackson3JsonBuilder runtime() {
		return runtime(OVERRIDE, InstanceHolder.INSTANCE);
	}

	/**
	 * Creates a new JSON builder with the given {@link JsonFactory}.
	 *
	 * @param jsonFactory the JSON factory to use for the underlying {@link JsonMapper}, e.g. for YAML support.
	 * @param builderCustomizer the customizer to apply to the underlying {@link JsonMapper.Builder}
	 * @return a new JSON builder with the given {@link JsonFactory}
	 */
	public static Jackson3JsonBuilder custom(final JsonFactory jsonFactory, final Consumer<JsonMapper.Builder> builderCustomizer) {
		return new Jackson3JsonBuilder(JsonMapper.builder(jsonFactory), builderCustomizer);
	}

	/**
	 * Creates a new JSON builder with the given {@link JsonFactory}.
	 *
	 * @param jsonFactory the JSON factory to use for the underlying {@link JsonMapper}, e.g. for YAML support.
	 * @return a new JSON builder with the given {@link JsonFactory}
	 */
	public static Jackson3JsonBuilder custom(final JsonFactory jsonFactory) {
		return custom(jsonFactory, Consumers.noConsumer());
	}

	/**
	 * Creates a new JSON builder with the given customizer applied to the underlying {@link JsonMapper.Builder}.
	 *
	 * @param builderCustomizer the customizer to apply to the underlying {@link JsonMapper.Builder}
	 * @return a new JSON builder with the given customizer applied to the underlying {@link JsonMapper.Builder}
	 */
	public static Jackson3JsonBuilder custom(final Consumer<JsonMapper.Builder> builderCustomizer) {
		return new Jackson3JsonBuilder(JsonMapper.builder(), builderCustomizer);
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
	public static <T> T with(final Jackson3JsonBuilder builder, final Supplier<T> supplier) {
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
		ObjectWriter objectWriter = jsonMapper.writerFor(obj.getClass());
		try {
			return eol() + objectWriter.writeValueAsString(obj);
		} catch (JacksonException e) {
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
			return jsonMapper.readValue(json, cls);
		} catch (JacksonException e) {
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
			return jsonMapper.readValue(json, typeReference);
		} catch (JacksonException e) {
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
			return jsonMapper.readValue(json, cls);
		} catch (JacksonException e) {
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
			return jsonMapper.readValue(json, typeReference);
		} catch (JacksonException e) {
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
			return jsonMapper.readValue(json, cls);
		} catch (JacksonException e) {
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
			return jsonMapper.readValue(json, typeReference);
		} catch (JacksonException e) {
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
		JsonMapper.Builder propertiesJsonMapperBuilder = JsonMapper.builder()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
				.propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
		final JsonMapper propertiesJsonMapper = propertiesJsonMapperBuilder.build();
		try {
			String json = propertiesJsonMapper.writeValueAsString(propertiesMap);
			return propertiesJsonMapper.readValue(json, cls);
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
		JsonMapper.Builder propertiesJsonMapperBuilder = JsonMapper.builder()
				.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
				.propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
		configureSensitivity(propertiesJsonMapperBuilder,
				SensitiveJackson3AnnotationIntrospector.allowSensitive(), defaultAnnotationIntrospector);
		final JsonMapper propertiesJsonMapper = propertiesJsonMapperBuilder.build();
		try {
			return propertiesJsonMapper.convertValue(properties, Map.class);
		} catch (Exception e) {
			onError.accept(e);
			return Collections.emptyMap();
		}
	}

	/**
	 * Returns the singleton instance of the JSON builder.
	 *
	 * @return the singleton instance of the JSON builder
	 */
	protected static Jackson3JsonBuilder instance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Registers a module to be added to the underlying {@link JsonMapper}. The module is registered by its name, so if a
	 * module with the same name is already registered, it will be overridden. This allows registering modules to the
	 * underlying {@link JsonMapper} without creating a dependency on the module's library.
	 *
	 * @param moduleName the module name
	 * @param moduleSupplier the supplier of the module to register
	 * @return the existing module supplier if a module with the same name is already registered, null otherwise
	 */
	public static Supplier<SimpleModule> registerModule(final String moduleName, final Supplier<SimpleModule> moduleSupplier) {
		if (Jackson3JsonBuilder.singletonMaterialized) {
			LOGGER.warn(ErrorMessage.MODULE_REGISTERED_AFTER_INITIALIZATION, moduleName);
		}
		Supplier<SimpleModule> existing = MODULES.putIfAbsent(moduleName, moduleSupplier);
		if (null != existing) {
			LOGGER.warn(ErrorMessage.MODULE_ALREADY_REGISTERED, moduleName);
		}
		return existing;
	}

	/**
	 * Registers modules from the given service loader.
	 *
	 * @param providers an iterable of module providers to register
	 */
	public static void registerModules(final Iterable<Jackson3ModuleProvider> providers) {
		for (Jackson3ModuleProvider provider : providers) {
			registerModule(provider.getModuleName(), provider.getModuleSupplier());
		}
	}

	/**
	 * Returns a {@link SimpleModule} with custom serializers/deserializers named {@link ApiphanyJackson3Module#NAME}.
	 *
	 * @return simple module
	 */
	public static SimpleModule apiphanySerializationModule() {
		return ApiphanyJackson3Module.instance();
	}

	/**
	 * Configures the underlying {@link JsonMapper.Builder} with the given {@link SensitiveJackson3AnnotationIntrospector}.
	 *
	 * @param sensitiveAnnotationIntrospector the sensitive annotation introspector
	 */
	public void configureSensitivity(final SensitiveJackson3AnnotationIntrospector sensitiveAnnotationIntrospector) {
		configureSensitivity(jsonMapperBuilder, sensitiveAnnotationIntrospector, defaultAnnotationIntrospector);
	}

	/**
	 * Configures the given {@link JsonMapper.Builder} with the given {@link SensitiveJackson3AnnotationIntrospector}.
	 *
	 * @param jsonMapperBuilder the JSON mapper builder to configure
	 * @param sensitiveAnnotationIntrospector the sensitive annotation introspector
	 * @return the configured object mapper
	 */
	public static JsonMapper.Builder configureSensitivity(final JsonMapper.Builder jsonMapperBuilder,
			final SensitiveJackson3AnnotationIntrospector sensitiveAnnotationIntrospector) {
		AnnotationIntrospector baseAnnotationIntrospector = jsonMapperBuilder.annotationIntrospector();
		return configureSensitivity(jsonMapperBuilder, sensitiveAnnotationIntrospector, baseAnnotationIntrospector);
	}

	/**
	 * Configures the given {@link JsonMapper.Builder} with the given {@link SensitiveJackson3AnnotationIntrospector}.
	 *
	 * @param jsonMapperBuilder the JSON mapper builder to configure
	 * @param sensitiveAnnotationIntrospector the sensitive annotation introspector
	 * @param baseAnnotationIntrospector the base annotation introspector
	 * @return the configured object mapper
	 */
	public static JsonMapper.Builder configureSensitivity(final JsonMapper.Builder jsonMapperBuilder,
			final SensitiveJackson3AnnotationIntrospector sensitiveAnnotationIntrospector, final AnnotationIntrospector baseAnnotationIntrospector) {
		return jsonMapperBuilder.annotationIntrospector(
				AnnotationIntrospector.pair(sensitiveAnnotationIntrospector, baseAnnotationIntrospector));
	}

	/**
	 * Returns the underlying {@link JsonMapper}.
	 *
	 * @return the underlying {@link JsonMapper}
	 */
	public JsonMapper getJsonMapper() {
		return jsonMapper;
	}

	/**
	 * Returns the JSON mapper builder used to create the underlying {@link JsonMapper}.
	 *
	 * @return the JSON mapper builder used to create the underlying {@link JsonMapper}
	 */
	public JsonMapper.Builder getJsonMapperBuilder() {
		return jsonMapperBuilder;
	}
}

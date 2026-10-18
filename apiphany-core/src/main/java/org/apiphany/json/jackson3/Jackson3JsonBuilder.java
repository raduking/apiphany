package org.apiphany.json.jackson3;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.Consumers;
import org.morphix.reflection.GenericClass;

import com.fasterxml.jackson.annotation.JsonInclude;

import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
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
	 * The {@link ThreadLocal#remove()} is handled correctly in {@link #with(Jackson3JsonBuilder, Supplier)} which calls
	 * {@link JsonBuilder#with(JsonBuilder, ThreadLocal, Supplier)} so no memory leak problems occur.
	 */
	private static final ThreadLocal<Jackson3JsonBuilder> OVERRIDE = new ThreadLocal<>(); // NOSONAR see Javadoc

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
	 * <p>
	 * Because Jackson 3 mappers are immutable, reconfiguring the builder at runtime (see {@link #configure(Consumer)})
	 * builds a new mapper and replaces this reference. An {@link AtomicReference} is used because it can be replaced while
	 * other threads serialize or de-serialize.
	 */
	private final AtomicReference<JsonMapper> jsonMapper = new AtomicReference<>();

	/**
	 * The default annotation introspector.
	 */
	protected final AnnotationIntrospector defaultAnnotationIntrospector;

	/**
	 * Constructor with extra configurations:
	 * <ul>
	 * <li>do not fail on empty beans</li>
	 * <li>ignore unknown JSON fields on de-serialization instead of failing</li>
	 * <li>fail when JSON {@code null} is read into a primitive field</li>
	 * <li>fail when tokens follow the first JSON value</li>
	 * <li>for some strange reason the properties are sorted alphabetically by default so we change the default to keep the
	 * order of the properties as they are in the class</li>
	 * <li>accept case-insensitive enums</li>
	 * <li>write and read enum values and map keys with {@code toString()}</li>
	 * <li>do not use a collection or map getter as a setter</li>
	 * <li>do not set {@code final} fields by reflection</li>
	 * <li>derive an acronym getter such as {@code getURL()} as the property name {@code URL}</li>
	 * <li>do not serialize <code>null</code> values</li>
	 * <li>lock the date/time serialization features to explicit values so the JSON output is stable</li>
	 * </ul>
	 * Note: date/time and duration values are written as ISO strings (not numeric timestamps), and classic
	 * {@link java.util.Date}/ {@link java.util.Calendar} values use a numeric zero offset (<code>+00:00</code>) instead of
	 * <code>Z</code>
	 *
	 * @param builder the JSON mapper builder
	 * @param builderCustomizer the JSON mapper builder customizer
	 */
	Jackson3JsonBuilder(final JsonMapper.Builder builder, final Consumer<JsonMapper.Builder> builderCustomizer) {
		this.defaultAnnotationIntrospector = builder.annotationIntrospector();
		configure(builder, Indentation.fromBoolean(isIndentOutput()), builderCustomizer);
		indentOutput(builder.isEnabled(SerializationFeature.INDENT_OUTPUT));
		this.jsonMapper.set(builder.build());
	}

	/**
	 * Uses the given {@link JsonMapper} and applies this builder's configuration on top of it.
	 *
	 * @param jsonMapper the JSON mapper to use
	 */
	Jackson3JsonBuilder(final JsonMapper jsonMapper) {
		this(jsonMapper, Customization.DEFAULT);
	}

	/**
	 * Uses the given {@link JsonMapper}.
	 * <p>
	 * {@link Customization#DEFAULT} applies the configuration described by
	 * {@link #Jackson3JsonBuilder(JsonMapper.Builder, Consumer)} on a rebuild of the mapper. {@link Customization#NONE}
	 * stores the mapper as given. Modules, features, and the sensitivity introspector are left unchanged.
	 * <p>
	 * Indentation always mirrors the mapper: with {@link Customization#NONE}, {@link #eol()} is derived from the mapper's
	 * own {@link SerializationFeature#INDENT_OUTPUT}, not from the {@code json-builder.to-json.indent-output} property, so
	 * {@link #toJsonString(Object)} never adds a line separator the mapper itself would not have written.
	 *
	 * @param jsonMapper the JSON mapper to use
	 * @param customization the customization applied to the mapper
	 */
	Jackson3JsonBuilder(final JsonMapper jsonMapper, final Customization customization) {
		Objects.requireNonNull(jsonMapper, "jsonMapper cannot be null");
		switch (customization) {
			case DEFAULT -> {
				JsonMapper.Builder builder = jsonMapper.rebuild();
				this.defaultAnnotationIntrospector = builder.annotationIntrospector();
				configure(builder, Indentation.fromBoolean(isIndentOutput()), Consumers.noConsumer());
				indentOutput(builder.isEnabled(SerializationFeature.INDENT_OUTPUT));
				this.jsonMapper.set(builder.build());
			}
			default -> {
				this.defaultAnnotationIntrospector = jsonMapper.serializationConfig().getAnnotationIntrospector();
				this.jsonMapper.set(jsonMapper);
				super.indentOutput(jsonMapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
			}
		}
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
	public static Jackson3JsonBuilder runtime() { // NOSONAR we want it hidden
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
	 * Creates a new JSON builder with the given {@link JsonMapper}.
	 *
	 * @param jsonMapper the JSON mapper to use for the underlying {@link JsonMapper}
	 * @return a new JSON builder with the given {@link JsonMapper}
	 */
	public static Jackson3JsonBuilder custom(final JsonMapper jsonMapper) {
		return new Jackson3JsonBuilder(jsonMapper);
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
	public static <T> String toJson(final T obj) { // NOSONAR we want it hidden
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
	public static <O, T> T fromJson(final O json, final Class<T> cls) { // NOSONAR we want it hidden
		return runtime().fromJsonObject(json, cls);
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
	public static <O, T> T fromJson(final O json, final GenericClass<T> genericClass) { // NOSONAR we want it hidden
		return runtime().fromJsonObject(json, genericClass);
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
		return runtime().fromJsonObject(json, typeReference);
	}

	/**
	 * Returns an object from the JSON input object using this builder.
	 * <p>
	 * {@link #fromJson(Object, TypeReference)} is static and selects the runtime builder. This method reads with this
	 * builder, so a caller can use a mapper other than the singleton.
	 *
	 * @param <O> input object type
	 * @param <T> type of the object
	 *
	 * @param json JSON input object
	 * @param typeReference type of the output object
	 * @return an object from the JSON input object
	 */
	public <O, T> T fromJsonObject(final O json, final TypeReference<T> typeReference) {
		return switch (json) {
			case String string -> fromJsonString(string, typeReference);
			case byte[] bytes -> fromJsonBytes(bytes, typeReference);
			case InputStream inputStream -> fromJsonInputStream(inputStream, typeReference);
			default -> throw unsupportedJsonInputType(json);
		};
	}

	/**
	 * Transforms the parameter to a JSON String. If the object is <code>null</code>, returns <code>null</code>. If the
	 * object cannot be serialized, returns the result of {@link #toIdentityJsonString(Object)}.
	 *
	 * @param <T> type of the object
	 *
	 * @param obj object to transform
	 * @return JSON String if conversion is possible, <code>null</code> if parameter is <code>null</code>,
	 * {@link #toIdentityJsonString(Object)} otherwise.
	 */
	@Override
	public <T> String toJsonString(final T obj) {
		return serialize(obj, o -> {
			ObjectWriter objectWriter = jsonMapper.get().writerFor(o.getClass());
			return eol() + objectWriter.writeValueAsString(o);
		});
	}

	/**
	 * Returns an object from the JSON string. If the JSON string cannot be de-serialized, returns <code>null</code>.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON string
	 * @param cls class of the object
	 * @return an object from the JSON string
	 */
	@Override
	public <T> T fromJsonString(final String json, final Class<T> cls) {
		return deserialize(json, cls, () -> jsonMapper.get().readValue(json, cls));
	}

	/**
	 * Returns an object from the JSON string. If the JSON string cannot be de-serialized, returns <code>null</code>.
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
	 * Returns an object from the JSON string. If the JSON string cannot be de-serialized, returns <code>null</code>.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON string
	 * @param typeReference type of the object
	 * @return an object from the JSON string
	 */
	public <T> T fromJsonString(final String json, final TypeReference<T> typeReference) {
		return deserialize(json, typeReference.getType(), () -> jsonMapper.get().readValue(json, typeReference));
	}

	/**
	 * Returns an object from the given byte array. If the byte array cannot be de-serialized, returns <code>null</code>.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON byte array
	 * @param cls class of the object
	 * @return an object from the JSON string
	 */
	@Override
	public <T> T fromJsonBytes(final byte[] json, final Class<T> cls) {
		return deserialize(json, cls, () -> jsonMapper.get().readValue(json, cls));
	}

	/**
	 * Returns an object from the JSON byte array. If the byte array cannot be de-serialized, returns <code>null</code>.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON byte array
	 * @param genericClass generic class wrapper for the type of the generic object
	 * @return an object from the JSON string
	 */
	@Override
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
	 * Returns an object from the JSON byte array. If the byte array cannot be de-serialized, returns <code>null</code>.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON byte array
	 * @param typeReference type of the object
	 * @return an object from the JSON string
	 */
	public <T> T fromJsonBytes(final byte[] json, final TypeReference<T> typeReference) {
		return deserialize(json, typeReference.getType(), () -> jsonMapper.get().readValue(json, typeReference));
	}

	/**
	 * Returns an object from the given input stream. If the byte array cannot be de-serialized, returns <code>null</code>.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON input stream
	 * @param cls class of the object
	 * @return an object from the JSON string
	 */
	@Override
	public <T> T fromJsonInputStream(final InputStream json, final Class<T> cls) {
		return deserialize(json, cls, () -> jsonMapper.get().readValue(json, cls));
	}

	/**
	 * Returns an object from the given input stream. If the byte array cannot be de-serialized, returns <code>null</code>.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON input stream
	 * @param genericClass generic class wrapper for the type of the generic object
	 * @return an object from the JSON string
	 */
	@Override
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
	 * Returns an object from the given input stream. If the byte array cannot be de-serialized, returns <code>null</code>.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON input stream
	 * @param typeReference type of the object
	 * @return an object from the JSON string
	 */
	public <T> T fromJsonInputStream(final InputStream json, final TypeReference<T> typeReference) {
		return deserialize(json, typeReference.getType(), () -> jsonMapper.get().readValue(json, typeReference));
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
		JsonMapper.Builder propertiesJsonMapperBuilder = jsonMapper.get().rebuild()
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
		JsonMapper.Builder propertiesJsonMapperBuilder = jsonMapper.get().rebuild()
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
	 * @return the existing module supplier if a module with the same name is already registered, <code>null</code>
	 * otherwise
	 */
	public static Supplier<SimpleModule> registerModule(final String moduleName, final Supplier<SimpleModule> moduleSupplier) {
		if (Jackson3JsonBuilder.singletonMaterialized) {
			instance().observability().lateModuleRegistration(moduleName);
		}
		Supplier<SimpleModule> existing = MODULES.putIfAbsent(moduleName, moduleSupplier);
		if (null != existing) {
			instance().observability().moduleAlreadyRegistered(moduleName);
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
	 * Applies the Apiphany module and feature configuration to the given {@link JsonMapper.Builder}.
	 *
	 * @param builder the JSON mapper builder to configure
	 * @param indentation output indentation
	 * @param builderCustomizer the customizer to apply after the standard configuration
	 */
	private static void configure(final JsonMapper.Builder builder, final Indentation indentation,
			final Consumer<JsonMapper.Builder> builderCustomizer) {
		for (Supplier<SimpleModule> moduleSupplier : MODULES.values()) {
			builder.addModule(moduleSupplier.get());
		}
		builder.changeDefaultPropertyInclusion(inclusion -> inclusion
				.withContentInclusion(JsonInclude.Include.NON_NULL)
				.withValueInclusion(JsonInclude.Include.NON_NULL));

		builder.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		builder.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		builder.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
		builder.configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
		builder.configure(EnumFeature.WRITE_ENUMS_USING_TO_STRING, true);
		builder.configure(EnumFeature.READ_ENUMS_USING_TO_STRING, true);
		builder.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
		builder.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);
		builder.configure(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS, false);
		builder.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false);

		configureDateTimeFeatures(builder);
		configureSensitivity(builder, SensitiveJackson3AnnotationIntrospector.hideSensitive());
		configureIndent(builder, indentation);

		builderCustomizer.accept(builder);
	}

	/**
	 * Locks the date/time serialization features of the given {@link JsonMapper.Builder} to explicit values so the JSON
	 * output does not silently change if library defaults change. Date/time and duration values are written as ISO strings
	 * (not numeric timestamps), and classic {@link java.util.Date}/{@link java.util.Calendar} values use a numeric zero
	 * offset (<code>+00:00</code>) instead of <code>Z</code>.
	 *
	 * @param builder the JSON mapper builder to configure
	 * @return the configured JSON mapper builder
	 */
	public static JsonMapper.Builder configureDateTimeFeatures(final JsonMapper.Builder builder) {
		builder.configure(DateTimeFeature.TRUNCATE_TO_MSECS_ON_WRITE, false);
		builder.configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		builder.configure(DateTimeFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
		builder.configure(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, true);
		builder.configure(DateTimeFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE, true);
		builder.configure(DateTimeFeature.WRITE_DATES_WITH_ZONE_ID, false);
		builder.configure(DateTimeFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
		builder.configure(DateTimeFeature.WRITE_UTC_AS_OFFSET, true);
		return builder;
	}

	/**
	 * Configures the given {@link JsonMapper.Builder} to indent the JSON output based on the given flag.
	 *
	 * @param builder the JSON mapper builder to configure
	 * @param indentation flag indicating whether to indent the JSON output or not
	 * @return the configured JSON mapper builder
	 */
	public static JsonMapper.Builder configureIndent(final JsonMapper.Builder builder, final Indentation indentation) {
		return switch (indentation) {
			case ENABLED -> builder.enable(SerializationFeature.INDENT_OUTPUT);
			case DISABLED -> builder.disable(SerializationFeature.INDENT_OUTPUT);
		};
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
		return jsonMapper.get();
	}

	/**
	 * Applies the given customizer to the underlying {@link JsonMapper}. Because the mapper is immutable, the current
	 * mapper is rebuilt with the customizer applied and the reference is replaced, so callers do not have to manage the
	 * replacement themselves.
	 * <p>
	 * The call is ignored while the builder instance is being constructed, because the mapper does not exist yet.
	 *
	 * @param customizer the customizer to apply to the underlying {@link JsonMapper.Builder}
	 */
	public void configure(final Consumer<JsonMapper.Builder> customizer) {
		JsonMapper mapper = this.jsonMapper.get();
		if (null == mapper) {
			return;
		}
		JsonMapper.Builder builder = mapper.rebuild();
		customizer.accept(builder);
		JsonMapper newMapper = builder.build();
		this.jsonMapper.set(newMapper);
		super.indentOutput(newMapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
	}

	/**
	 * Enable/disable JSON indentation. Because the underlying {@link JsonMapper} is immutable, the mapper is rebuilt via
	 * {@link #configure(Consumer)} with the indentation feature enabled or disabled, so the setting can be changed at any
	 * time.
	 *
	 * @param enable true to enable, false to disable
	 */
	@Override
	public void indentOutput(final boolean enable) {
		super.indentOutput(enable);
		configure(builder -> configureIndent(builder, Indentation.fromBoolean(enable)));
	}
}

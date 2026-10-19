package org.apiphany.json.jackson2;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.Consumers;
import org.morphix.reflection.GenericClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

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
public class Jackson2JsonBuilder extends JsonBuilder { // NOSONAR singleton implementation

	/**
	 * Singleton instance holder.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * Singleton instance.
		 */
		private static final Jackson2JsonBuilder INSTANCE = create();

		/**
		 * Creates the singleton instance.
		 *
		 * @return the singleton instance
		 */
		private static Jackson2JsonBuilder create() {
			Jackson2JsonBuilder instance = new Jackson2JsonBuilder();
			Jackson2JsonBuilder.singletonMaterialized = true;
			if (!Jackson2Library.isJdk8ModulePresent()) {
				instance.observability().jdk8ModuleNotFound();
			}
			if (!Jackson2Library.isJavaTimeModulePresent()) {
				instance.observability().javaTimeModuleNotFound();
			}
			return instance;
		}
	}

	/**
	 * Thread local override for the singleton instance.
	 * <p>
	 * The {@link ThreadLocal#remove()} is handled correctly in #with(Jackson2JsonBuilder, Supplier) which calls
	 * {@link JsonBuilder#with(JsonBuilder, ThreadLocal, Supplier)} so no memory leak problems occur.
	 */
	private static final ThreadLocal<Jackson2JsonBuilder> OVERRIDE = new ThreadLocal<>(); // NOSONAR see Javadoc

	/**
	 * Map of modules to register to the underlying {@link ObjectMapper}. The key is the module class name. This allows
	 * registering modules to the underlying {@link ObjectMapper} without creating a dependency on the module's library.
	 */
	protected static final Map<String, Supplier<Module>> MODULES = new ConcurrentHashMap<>();
	static {
		if (Jackson2Library.isJavaTimeModulePresent()) {
			registerModule(Jackson2Library.javaTimeModuleClassName(),
					() -> newJavaTimeModule(DateTimeFormatter.ISO_DATE_TIME, DateTimeFormatter.ISO_DATE));
		}
		if (Jackson2Library.isJdk8ModulePresent()) {
			registerModule(Jackson2Library.jdk8ModuleClassName(), Jackson2Library::newJdk8Module);
		}
		registerModule(ApiphanyJackson2Module.NAME, Jackson2JsonBuilder::apiphanySerializationModule);
		registerModules(ServiceLoader.load(Jackson2ModuleProvider.class));
	}

	/**
	 * Flag to indicate if the singleton instance has been created and initialized. This is used to log a warning if a
	 * module is registered after the singleton instance has been initialized, as the module will not be registered to the
	 * singleton instance's underlying {@link ObjectMapper}.
	 */
	private static volatile boolean singletonMaterialized;

	/**
	 * The underlying {@link ObjectMapper}.
	 */
	protected final ObjectMapper objectMapper;

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
	 * <li>accept case-insensitive enums</li>
	 * <li>write and read enum values and map keys with {@code toString()}</li>
	 * <li>do not use a collection or map getter as a setter</li>
	 * <li>do not set {@code final} fields by reflection</li>
	 * <li>derive an acronym getter such as {@code getURL()} as the property name {@code URL}</li>
	 * <li>do not serialize <code>null</code> values</li>
	 * <li>write date/time and duration values as ISO strings (not numeric timestamps)</li>
	 * </ul>
	 * Note: classic {@link java.util.Date}/{@link java.util.Calendar} values use a numeric zero offset
	 * (<code>+00:00</code>) instead of <code>Z</code>
	 *
	 * @param objectMapper the object mapper to use
	 */
	Jackson2JsonBuilder(final ObjectMapper objectMapper) {
		this(objectMapper, Customization.DEFAULT);
	}

	/**
	 * Uses the given {@link ObjectMapper}.
	 * <p>
	 * {@link Customization#DEFAULT} applies the configuration described by {@link #Jackson2JsonBuilder(ObjectMapper)}.
	 * {@link Customization#NONE} keeps the mapper's own modules and features, but always registers Apiphany annotations and
	 * serializers. Spring uses that form so a client keeps the mapper it already configured.
	 * <p>
	 * Indentation always mirrors the mapper: with {@link Customization#NONE}, {@link #eol()} is derived from the mapper's
	 * own {@link SerializationFeature#INDENT_OUTPUT}, not from the {@code json-builder.to-json.indent-output} property, so
	 * {@link #toJsonString(Object)} never adds a line separator the mapper itself would not have written.
	 *
	 * @param objectMapper the object mapper to use
	 * @param customization the customization applied to the mapper
	 */
	Jackson2JsonBuilder(final ObjectMapper objectMapper, final Customization customization) {
		this.defaultAnnotationIntrospector = objectMapper.getSerializationConfig().getAnnotationIntrospector();
		switch (customization) { // NOSONAR future setup
			case DEFAULT -> {
				Indentation indentation = Indentation.fromBoolean(isIndentOutput());
				configure(objectMapper, indentation, Consumers.noConsumer());
			}
			default -> configureApiphany(objectMapper);
		}
		this.objectMapper = objectMapper;
		setIndentOutput(objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
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
	Jackson2JsonBuilder(final JsonFactory jsonFactory) {
		this(new ObjectMapper(jsonFactory));
	}

	/**
	 * Returns the runtime JSON builder instance. If a thread local override is set it returns the override, otherwise it
	 * returns the singleton instance.
	 *
	 * @return the runtime JSON builder instance
	 */
	public static Jackson2JsonBuilder runtime() { // NOSONAR we want it hidden
		return runtime(OVERRIDE, InstanceHolder.INSTANCE);
	}

	/**
	 * Creates a new JSON builder with the given {@link JsonFactory}.
	 *
	 * @param jsonFactory the JSON factory to use for the underlying {@link ObjectMapper}, e.g. for YAML support.
	 * @return a new JSON builder with the given {@link JsonFactory}
	 */
	public static Jackson2JsonBuilder custom(final JsonFactory jsonFactory) {
		return new Jackson2JsonBuilder(jsonFactory);
	}

	/**
	 * Creates a new JSON builder with the given {@link ObjectMapper}.
	 *
	 * @param objectMapper the object mapper to use for the underlying {@link ObjectMapper}
	 * @return a new JSON builder with the given {@link ObjectMapper}
	 */
	public static Jackson2JsonBuilder custom(final ObjectMapper objectMapper) {
		return new Jackson2JsonBuilder(objectMapper);
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
		return serialize(obj, o -> {
			ObjectWriter objectWriter = objectMapper.writerFor(o.getClass());
			return eol() + objectWriter.writeValueAsString(o);
		});
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
		return deserialize(json, cls, () -> objectMapper.readValue(json, cls));
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
		return deserialize(json, typeReference.getType(), () -> objectMapper.readValue(json, typeReference));
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
	@Override
	public <T> T fromJsonBytes(final byte[] json, final Class<T> cls) {
		return deserialize(json, cls, () -> objectMapper.readValue(json, cls));
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
	 * Returns an object from the JSON byte array. If the byte array cannot be de-serialized, returns null.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON byte array
	 * @param typeReference type of the object
	 * @return an object from the JSON string
	 */
	public <T> T fromJsonBytes(final byte[] json, final TypeReference<T> typeReference) {
		return deserialize(json, typeReference.getType(), () -> objectMapper.readValue(json, typeReference));
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
	@Override
	public <T> T fromJsonInputStream(final InputStream json, final Class<T> cls) {
		return deserialize(json, cls, () -> objectMapper.readValue(json, cls));
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
	 * Returns an object from the given input stream. If the byte array cannot be de-serialized, returns null.
	 *
	 * @param <T> type of the object
	 *
	 * @param json JSON input stream
	 * @param typeReference type of the object
	 * @return an object from the JSON string
	 */
	public <T> T fromJsonInputStream(final InputStream json, final TypeReference<T> typeReference) {
		return deserialize(json, typeReference.getType(), () -> objectMapper.readValue(json, typeReference));
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
	 * Returns the singleton instance of the JSON builder.
	 *
	 * @return the singleton instance of the JSON builder
	 */
	protected static Jackson2JsonBuilder instance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Registers a module to be added to the underlying {@link ObjectMapper}. The module is registered by its name, so if a
	 * module with the same name is already registered, it will be overridden. This allows registering modules to the
	 * underlying {@link ObjectMapper} without creating a dependency on the module's library.
	 *
	 * @param moduleName the module name
	 * @param moduleSupplier the supplier of the module to register
	 * @return the existing module supplier if a module with the same name is already registered, null otherwise
	 */
	public static Supplier<Module> registerModule(final String moduleName, final Supplier<Module> moduleSupplier) {
		if (Jackson2JsonBuilder.singletonMaterialized) {
			instance().observability().lateModuleRegistration(moduleName);
		}
		Supplier<Module> existing = MODULES.putIfAbsent(moduleName, moduleSupplier);
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
	public static void registerModules(final Iterable<Jackson2ModuleProvider> providers) {
		for (Jackson2ModuleProvider provider : providers) {
			registerModule(provider.getModuleName(), provider.getModuleSupplier());
		}
	}

	/**
	 * Creates a new Java time module with the given formatters. Call this only when
	 * {@link Jackson2Library#isJavaTimeModulePresent()} is {@code true}.
	 *
	 * @param dateTimeFormatter the date time formatter object
	 * @param dateFormatter the date formatter object
	 * @return java time module
	 */
	public static SimpleModule newJavaTimeModule(final DateTimeFormatter dateTimeFormatter, final DateTimeFormatter dateFormatter) {
		return JavaTimeJackson2Module.create(dateTimeFormatter, dateFormatter);
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
	 * Registers Apiphany serializers and annotation handling on the given {@link ObjectMapper} without changing other
	 * mapper settings.
	 *
	 * @param objectMapper the object mapper to augment
	 */
	private static void configureApiphany(final ObjectMapper objectMapper) {
		objectMapper.registerModule(apiphanySerializationModule());
		configureSensitivity(objectMapper, SensitiveJackson2AnnotationIntrospector.hideSensitive());
	}

	/**
	 * Applies the Apiphany module and feature configuration to the given {@link ObjectMapper}.
	 *
	 * @param objectMapper object mapper to configure
	 */
	private static void configure(final ObjectMapper objectMapper, final Indentation indentation, final Consumer<ObjectMapper> mapperCustomizer) {
		for (Supplier<Module> moduleSupplier : MODULES.values()) {
			objectMapper.registerModule(moduleSupplier.get());
		}
		objectMapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(
				JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL));

		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
		objectMapper.configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		objectMapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
		objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
		objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);

		configure(objectMapper, MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
		configure(objectMapper, MapperFeature.USE_GETTERS_AS_SETTERS, false);
		configure(objectMapper, MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS, false);
		configure(objectMapper, MapperFeature.USE_STD_BEAN_NAMING, true);

		configureSensitivity(objectMapper, SensitiveJackson2AnnotationIntrospector.hideSensitive());
		configureIndent(objectMapper, indentation);

		mapperCustomizer.accept(objectMapper);
	}

	/**
	 * Applies the given customizer to the underlying {@link ObjectMapper}. Because the mapper is mutable, the customizer
	 * changes it in place, without replacing the reference.
	 * <p>
	 * The mapper caches are cleared afterwards so that structural changes (for example a property naming strategy) also
	 * take effect on types that were already serialized or de-serialized.
	 *
	 * @param customizer the customizer to apply to the underlying {@link ObjectMapper}
	 */
	public void configure(final Consumer<ObjectMapper> customizer) {
		customizer.accept(objectMapper);
		objectMapper.clearCaches();
		setIndentOutput(objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
	}

	/**
	 * Configures the given {@link MapperFeature} to the given state on both the serialization and de-serialization
	 * configurations of the given {@link ObjectMapper}.
	 * <p>
	 * {@code ObjectMapper.configure(MapperFeature, boolean)} is deprecated since Jackson 2.13, so the feature is applied
	 * directly on the configurations instead of using the deprecated method.
	 *
	 * @param objectMapper the object mapper to configure
	 * @param feature the mapper feature to configure
	 * @param state the state to configure the feature to
	 */
	private static void configure(final ObjectMapper objectMapper, final MapperFeature feature, final boolean state) {
		objectMapper.setConfig(objectMapper.getSerializationConfig().with(feature, state));
		objectMapper.setConfig(objectMapper.getDeserializationConfig().with(feature, state));
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
	 * Configures the given {@link ObjectMapper} with the given {@link Indentation}.
	 *
	 * @param objectMapper the object mapper to configure
	 * @param indentation the indentation
	 * @return the configured object mapper
	 */
	private static ObjectMapper configureIndent(final ObjectMapper objectMapper, final Indentation indentation) {
		switch (indentation) {
			case ENABLED -> objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			case DISABLED -> objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
		}
		return objectMapper;
	}

	/**
	 * Enable/disable JSON indentation.
	 *
	 * @param enable true to enable, false to disable
	 */
	@Override
	public void indentOutput(final boolean enable) {
		setIndentOutput(enable);
		configureIndent(objectMapper, Indentation.fromBoolean(enable));
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

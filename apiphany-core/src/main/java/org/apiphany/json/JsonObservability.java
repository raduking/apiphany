package org.apiphany.json;

import java.lang.reflect.Type;

import org.apiphany.lang.Strings;
import org.apiphany.logging.Logging;
import org.apiphany.logging.LoggingFormat;
import org.apiphany.logging.Slf4jLoggerAdapter;
import org.morphix.convert.Converter;
import org.morphix.lang.function.LoggerAdapter;
import org.morphix.reflection.Constructors;

/**
 * Observability support for JSON processing.
 *
 * @author Radu Sebastian LAZIN
 */
public class JsonObservability implements LoggerAdapter {

	/**
	 * Namespace for error messages. These messages are used for exception or logging error messages.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class ErrorMessage {

		/**
		 * Error message logged when no JSON library was found in the classpath.
		 */
		public static final String JSON_LIBRARY_NOT_FOUND = "No JSON library found in the classpath (like Jackson or Gson)";

		/**
		 * Error message logged when an object could not be serialized.
		 */
		public static final String COULD_NOT_SERIALIZE_OBJECT = "Could not serialize object: {}";

		/**
		 * Error message logged when an object could not be de-serialized.
		 */
		public static final String COULD_NOT_DESERIALIZE_OBJECT = "Could not deserialize object type: {}, input: {}";

		/**
		 * Error message logged when a JSON input could not be parsed.
		 */
		public static final String UNSUPPORTED_JSON_INPUT_TYPE = "Unsupported JSON input type: {}";

		/**
		 * Error message logged when a JSON library module is already registered.
		 */
		public static final String MODULE_ALREADY_REGISTERED = "Module already registered: {}";

		/**
		 * Error message logged when a JSON library module is registered after initialization.
		 */
		public static final String MODULE_REGISTERED_AFTER_INITIALIZATION = "Module registered after initialization: {}";

		/**
		 * Hide constructor.
		 */
		private ErrorMessage() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * Logger for JSON operations. This logger is used to log errors and other information related to JSON processing.
	 */
	private final LoggerAdapter logger;

	/**
	 * Reference to the JSON builder instance. This is used to access configuration options like debug-string.
	 */
	private final JsonBuilder jsonBuilder;

	/**
	 * Creates a new instance of {@link JsonObservability} with the given JSON builder.
	 *
	 * @param jsonBuilder the JSON builder instance to use for configuration and logging
	 */
	public JsonObservability(final JsonBuilder jsonBuilder) {
		this(Slf4jLoggerAdapter.of(jsonBuilder.getClass()), jsonBuilder);
	}

	/**
	 * Creates a new instance of {@link JsonObservability} with the given logger and JSON builder.
	 *
	 * @param logger the logger to use for logging JSON-related messages
	 * @param jsonBuilder the JSON builder instance to use for configuration and logging
	 */
	public JsonObservability(final LoggerAdapter logger, final JsonBuilder jsonBuilder) {
		this.logger = logger;
		this.jsonBuilder = jsonBuilder;
	}

	/**
	 * @see LoggerAdapter#log(LoggingLevel, String, Object...)
	 */
	@Override
	public void log(final LoggingLevel level, final String message, final Object... args) {
		logger.log(level, message, args);
	}

	/**
	 * @see LoggerAdapter#isEnabled(LoggingLevel)
	 */
	@Override
	public boolean isEnabled(final LoggingLevel level) {
		return logger.isEnabled(level);
	}

	/**
	 * Logs a warning when a JSON library is not found in the classpath.
	 */
	public void jsonLibraryNotFound() {
		warn("{}, JsonBuilder.toJson will only build JSONs like { \"identity\":\"<class-name>@<identity-hashcode>\" }!",
				ErrorMessage.JSON_LIBRARY_NOT_FOUND);
	}

	/**
	 * Logs a warning when a JSON library module is registered after initialization.
	 *
	 * @param moduleName the name of the module that was registered late
	 */
	public void lateModuleRegistration(final String moduleName) {
		warn(ErrorMessage.MODULE_REGISTERED_AFTER_INITIALIZATION, moduleName);
	}

	/**
	 * Logs a warning when a JSON library module is registered multiple times.
	 *
	 * @param moduleName the name of the module that was registered multiple times
	 */
	public void moduleAlreadyRegistered(final String moduleName) {
		warn(ErrorMessage.MODULE_ALREADY_REGISTERED, moduleName);
	}

	/**
	 * Logs a deserialization failure with the target type and a description of the input.
	 *
	 * @param obj the input object that failed to deserialize
	 * @param targetType the target type we attempted to deserialize into
	 * @param e the exception that was thrown during deserialization
	 */
	public void deserializationFailed(final Object obj, final Type targetType, final Exception e) {
		warn(ErrorMessage.COULD_NOT_DESERIALIZE_OBJECT, targetType, describeJsonInput(obj), e);
	}

	/**
	 * Logs a serialization failure with the object that failed to serialize.
	 *
	 * @param obj the object that failed to serialize
	 * @param e the exception that was thrown during serialization
	 * @return the JSON string representation of the object, or a description if serialization failed
	 */
	public String serializationFailed(final Object obj, final Exception e) {
		String result = jsonBuilder.isDebugString() ? toDebugJsonString(obj) : jsonBuilder.toIdentityJsonString(obj);
		warn(ErrorMessage.COULD_NOT_SERIALIZE_OBJECT, result, e);
		return result;
	}

	/**
	 * Returns the {@link Object#toString()} in a JSON format. If the input object has a field name called {@code id} then
	 * it adds it to the JSON.
	 *
	 * @param <T> type of the object
	 *
	 * @param obj input
	 * @return JSON String
	 */
	protected static <T> String toDebugJsonString(final T obj) {
		if (null == obj) {
			return "{ \"type\":null, \"identity\":null }";
		}
		class FieldExtractor {
			String id;
		}
		FieldExtractor fieldExtractor = Converter.convert(obj).to(FieldExtractor::new);
		String type = obj.getClass().getCanonicalName();
		String identity = Strings.identityHashCode(obj);
		String id = null == fieldExtractor.id ? null : fieldExtractor.id;
		return "{ \"type\":\"" + type + "\""
				+ (null != id ? ", \"id\":\"" + id + "\"" : "")
				+ ", \"identity\":\"" + identity + "\""
				+ " }";
	}

	/**
	 * Builds a safe diagnostic description for a JSON input value.
	 * <p>
	 * By default it only logs metadata (type/length). When debug-string is enabled via
	 * {@code -Djson-builder.to-json.debug-string=true}, it also includes a bounded preview to aid debugging.
	 *
	 * @param json JSON input object
	 * @return diagnostic string with input type and size (and preview when debug mode is enabled)
	 */
	protected String describeJsonInput(final Object json) {
		return Logging.describeInput(json, LoggingFormat.CUSTOM,
				Logging.Include.LENGTH,
				Logging.Include.HASH,
				Logging.Include.when(jsonBuilder::isDebugString, Logging.Include.PREVIEW));
	}
}

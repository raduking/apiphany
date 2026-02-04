package org.apiphany.json;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.apiphany.json.jackson2.JacksonLibrary;
import org.apiphany.lang.Pair;
import org.apiphany.lang.Strings;
import org.morphix.convert.Converter;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.GenericClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will serialize/de-serialize any JSON serializable/deserializable object to {@link String}/{@link Object}. This
 * class is used to uniformly serialize/de-serialize objects across the entire project. If no JSON library was found in
 * the classpath a warning will be issued and the {@link JsonBuilder#toJson(Object)} method will use the objects
 * {@link #toString()} method.
 * <p>
 * For serialization to {@link String} use {@link #toJson(Object)}.<br/>
 * For de-serialization {@link Object} use {@link #fromJson(Object, Class)} or {@link #fromJson(Object, GenericClass)}.
 * <p>
 * To indent the JSON output use the {@code json-builder.to-json.indent-output} property set to {@code true}.
 * <p>
 * Note: For indentation use: {@link #indentOutput(boolean)} with {@code true} to indent output and {@code false} to
 * have a single line JSON string.
 *
 * @author Radu Sebastian LAZIN
 */
public class JsonBuilder { // NOSONAR singleton implementation

	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonBuilder.class);

	/**
	 * Namespace for configurable properties.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Property {

		/**
		 * Property name for indenting output.
		 */
		public static final String INDENT_OUTPUT = "json-builder.to-json.indent-output";

		/**
		 * Property name for logging debug string.
		 */
		public static final String DEBUG_STRING = "json-builder.to-json.debug-string";

		/**
		 * Hide constructor.
		 */
		private Property() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * Namespace for error messages. These messages are used for exception or logging error messages.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	protected static class ErrorMessage {

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
		public static final String COULD_NOT_DESERIALIZE_OBJECT = "Could not deserialize object: {}";

		/**
		 * Hide constructor.
		 */
		private ErrorMessage() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * Singleton instance holder.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * Singleton instance.
		 */
		private static final JsonBuilder INSTANCE = initializeInstance(JacksonLibrary.INFORMATION);
	}

	/**
	 * Indent output flag.
	 */
	private boolean indentOutput;

	/**
	 * Debug string flag.
	 */
	private final boolean debugString;

	/**
	 * Line separator, depends on {@link #indentOutput}.
	 */
	private String lineSeparator;

	/**
	 * Constructor that initializes all fields.
	 */
	protected JsonBuilder() {
		this.indentOutput = isPropertyTrue(Property.INDENT_OUTPUT);
		computeLineSeparator(indentOutput);
		this.debugString = isPropertyTrue(Property.DEBUG_STRING);
	}

	/**
	 * Returns an instance based on the available JSON libraries.
	 *
	 * @param libraries the libraries information list
	 * @return a JSON builder
	 */
	@SafeVarargs
	protected static JsonBuilder initializeInstance(final Pair<Boolean, Class<? extends JsonBuilder>>... libraries) {
		if (null != libraries) {
			for (Pair<Boolean, Class<? extends JsonBuilder>> libraryInfo : libraries) {
				if (libraryInfo.left().booleanValue()) {
					return Constructors.IgnoreAccess.newInstance(libraryInfo.right());
				}
			}
		}
		LOGGER.warn("{}, JsonBuilder.toJson will use the objects toString method!", ErrorMessage.JSON_LIBRARY_NOT_FOUND);
		return new JsonBuilder();
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
	 * Returns an object from the JSON object.
	 *
	 * @param <O> input object type
	 * @param <T> type of the object
	 *
	 * @param json JSON object
	 * @param cls class of the object
	 * @return an object from the JSON object
	 */
	public static <O, T> T fromJson(final O json, final Class<T> cls) {
		return switch (json) {
			case String string -> InstanceHolder.INSTANCE.fromJsonString(string, cls);
			default -> throw unsupportedJsonInputType(json);
		};
	}

	/**
	 * Returns an object from the JSON object.
	 *
	 * @param <O> input object type
	 * @param <T> type of the object
	 *
	 * @param json JSON object
	 * @param genericClass generic class wrapper for the type of the generic object
	 * @return an object from the JSON object
	 */
	public static <O, T> T fromJson(final O json, final GenericClass<T> genericClass) {
		return switch (json) {
			case String string -> InstanceHolder.INSTANCE.fromJsonString(string, genericClass);
			default -> throw unsupportedJsonInputType(json);
		};
	}

	/**
	 * Returns an object from a properties map.
	 *
	 * @param <T> return type
	 *
	 * @param propertiesMap the map with the properties
	 * @param cls class of the return type
	 * @param onError on error exception consumer
	 * @return wanted object
	 */
	public static <T> T fromMap(final Map<String, Object> propertiesMap, final Class<T> cls, final Consumer<Exception> onError) {
		return InstanceHolder.INSTANCE.fromPropertiesMap(propertiesMap, cls, onError);
	}

	/**
	 * Returns a properties map from an object.
	 *
	 * @param <T> properties object type
	 *
	 * @param properties properties object
	 * @param onError on error exception consumer
	 * @return properties map
	 */
	public static <T> Map<String, Object> toMap(final T properties, final Consumer<Exception> onError) {
		return InstanceHolder.INSTANCE.toPropertiesMap(properties, onError);
	}

	/**
	 * Sets the indent flag for all JSON methods.
	 *
	 * @param enable indent flag
	 */
	public static void indentJsonOutput(final boolean enable) {
		InstanceHolder.INSTANCE.indentOutput(enable);
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
	public <T> String toJsonString(final T obj) {
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
	public <T> T fromJsonString(final String json, final Class<T> cls) {
		throw jsonLibraryNotFound();
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
	public <T> T fromJsonString(final String json, final GenericClass<T> genericClass) {
		throw jsonLibraryNotFound();
	}

	/**
	 * Returns an object from a properties map.
	 *
	 * @param <T> return type
	 *
	 * @param propertiesMap the map with the properties
	 * @param cls class of the return type
	 * @param onError on error exception consumer
	 * @return wanted object
	 */
	public <T> T fromPropertiesMap(final Map<String, Object> propertiesMap, final Class<T> cls, final Consumer<Exception> onError) {
		throw jsonLibraryNotFound();
	}

	/**
	 * Returns a properties map from an object.
	 *
	 * @param <T> properties object type
	 *
	 * @param properties properties object
	 * @param onError on error exception consumer
	 * @return properties map
	 */
	public <T> Map<String, Object> toPropertiesMap(final T properties, final Consumer<Exception> onError) {
		throw jsonLibraryNotFound();
	}

	/**
	 * Enable/disable JSON indentation.
	 *
	 * @param enable true to enable, false to disable
	 */
	public void indentOutput(final boolean enable) {
		this.indentOutput = enable;
		computeLineSeparator(enable);
	}

	/**
	 * Returns the indent output flag.
	 *
	 * @return the indent output flag
	 */
	public boolean isIndentOutput() {
		return indentOutput;
	}

	/**
	 * Returns the debug string flag.
	 *
	 * @return the debug string flag
	 */
	public boolean isDebugString() {
		return debugString;
	}

	/**
	 * Returns the line separator.
	 *
	 * @return the line separator
	 */
	public String eol() {
		return lineSeparator;
	}

	/**
	 * Computes the line separator based on indent output setting.
	 *
	 * @param indentOutput indent output flag
	 */
	private void computeLineSeparator(final boolean indentOutput) {
		this.lineSeparator = indentOutput ? Strings.EOL : "";
	}

	/**
	 * Returns the exception thrown from unimplemented methods.
	 *
	 * @return the exception thrown from unimplemented methods
	 */
	protected static UnsupportedOperationException jsonLibraryNotFound() {
		return new UnsupportedOperationException(ErrorMessage.JSON_LIBRARY_NOT_FOUND);
	}

	/**
	 * Returns true if a system property is set to {@code "true"}, false otherwise.
	 *
	 * @param propertyName name of the system property
	 * @return true if a system property is set to "true", false otherwise
	 */
	protected static boolean isPropertyTrue(final String propertyName) {
		return Objects.equals("true", System.getProperty(propertyName));
	}

	/**
	 * Returns {@code true} if Jackson library is present in the classpath.
	 *
	 * @return true if Jackson library is present in the classpath
	 */
	public static boolean isJacksonPresent() {
		return JacksonLibrary.INFORMATION.left().booleanValue();
	}

	/**
	 * Returns the {@link Object#toString()} in a JSON format.
	 *
	 * @param <T> type of the object
	 *
	 * @param obj input
	 * @return JSON String
	 */
	protected static <T> String toString(final T obj) {
		return "{ \"hash\":\"" + hexHash(obj) + "\" }";
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
	protected static <T> String toDebugString(final T obj) {
		if (null == obj) {
			return "{ \"type\":null, \"hash\":null }";
		}
		class FieldExtractor {
			Long id;
		}
		FieldExtractor fieldExtractor = Converter.convert(obj).to(FieldExtractor::new);
		return "{ \"type\":\"" + obj.getClass().getCanonicalName() + "\""
				+ (null != fieldExtractor.id ? ", \"id\":\"" + fieldExtractor.id + "\"" : "")
				+ ", \"hash\":\"" + hexHash(obj) + "\""
				+ " }";
	}

	/**
	 * Returns the string with the class name and hexadecimal hash of the input object appended. If the input object is null
	 * the result is <code>"null"</code>.
	 *
	 * @param <T> object type
	 *
	 * @param obj object to get the hash
	 * @return string which contains the class name and e hexadecimal hash
	 */
	protected static <T> String hexHash(final T obj) {
		if (null == obj) {
			return Objects.toString(obj);
		}
		return obj.getClass().getName() + "@" + Integer.toHexString(obj.hashCode());
	}

	/**
	 * Returns an {@link UnsupportedOperationException} for unsupported JSON input types.
	 *
	 * @param <O> JSON input type
	 *
	 * @param json JSON input object
	 * @return the exception to be thrown when the input type is not supported
	 */
	protected static <O> UnsupportedOperationException unsupportedJsonInputType(final O json) {
		return new UnsupportedOperationException("Unsupported JSON input type: " + json.getClass());
	}
}

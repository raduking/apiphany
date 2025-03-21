package org.apiphany.json;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.commons.lang3.NotImplementedException;
import org.apiphany.lang.Strings;
import org.morphix.convert.Converter;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.GenericClass;
import org.morphix.reflection.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will serialize/de-serialize any JSON serializable/deserializable object to {@link String}/{@link Object}. This
 * class is used to uniformly serialize/de-serialize objects across the entire project. If no JSON library was found in
 * the class path a warning will be issued and the {@link JsonBuilder#toJson(Object)} method will use the objects
 * {@link #toString()} method.
 * <p>
 * For serialization to {@link String} use {@link #toJson(Object)}.<br/>
 * For de-serialization {@link Object} use {@link #fromJson(String, Class)} or {@link #fromJson(String, GenericClass)}.
 * <p>
 * To indent the JSON output use the {@code json-builder.to-json.indent-output} property set to {@code true}.
 * <p>
 * Note: For indentation use: {@link #indentOutput(boolean)} with {@code true} to indent output and {@code false} to
 * have a single line JSON string.
 *
 * @author Radu Sebastian LAZIN
 */
public class JsonBuilder { // NOSONAR singleton implementation

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonBuilder.class);

	public static final String PROPERTY_INDENT_OUTPUT = "json-builder.to-json.indent-output";
	public static final String PROPERTY_DEBUG_STRING = "json-builder.to-json.debug-string";

	protected static final String ERROR_JSON_LIBRARY_NOT_FOUND = "No JSON library found in the class path (like Jackson or Gson)";

	protected static final String JACKSON_OBJECT_MAPPER_CLASS_NAME = "com.fasterxml.jackson.databind.ObjectMapper";
	protected static final String JACKSON_JSON_BUILDER_CLASS_NAME = "org.apiphany.json.jackson.JacksonJsonBuilder";

	/**
	 * Singleton instance holder.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * Flag that shows if Jackson JSON library is present in the class path.
		 */
		private static final boolean JACKSON_PRESENT = null != Reflection.getClass(JACKSON_OBJECT_MAPPER_CLASS_NAME);

		/**
		 * Singleton instance.
		 */
		private static final JsonBuilder INSTANCE = initializeInstance();

		/**
		 * Initializes the singleton instance.
		 *
		 * @return a JSON builder
		 */
		private static JsonBuilder initializeInstance() {
			if (JACKSON_PRESENT) {
				Class<? extends JsonBuilder> jacksonJsonBuilderClass = Reflection.getClass(JACKSON_JSON_BUILDER_CLASS_NAME);
				return Constructors.IgnoreAccess.newInstance(jacksonJsonBuilderClass);
			}
			LOGGER.warn("{}, JsonBuilder.toJson will use the objects toString method!", ERROR_JSON_LIBRARY_NOT_FOUND);
			return new JsonBuilder();
		}
	}

	/**
	 * Indent output flag.
	 */
	private boolean indentOutput;

	/**
	 * Debug string flag.
	 */
	private boolean debugString;

	/**
	 * Line separator, depends on {@link #indentOutput}.
	 */
	private String lineSeparator;

	/**
	 * Constructor that initializes all fields.
	 */
	protected JsonBuilder() {
		this.indentOutput = isPropertyTrue(PROPERTY_INDENT_OUTPUT);
		this.debugString = isPropertyTrue(PROPERTY_DEBUG_STRING);
		this.lineSeparator = indentOutput ? Strings.EOL : "";
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
		return Strings.safeToString(obj);
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
	 * Enable/disable JSON indentation.
	 *
	 * @param enable true to enable, false to disable
	 */
	public void indentOutput(final boolean enable) {
		this.indentOutput = enable;
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
	 * Returns the exception thrown from unimplemented methods.
	 *
	 * @return the exception thrown from unimplemented methods
	 */
	protected static NotImplementedException jsonLibraryNotFound() {
		return new NotImplementedException(ERROR_JSON_LIBRARY_NOT_FOUND);
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
	 * Returns {@code true} if Jackson library is present in the class path.
	 *
	 * @return true if Jackson library is present in the class path
	 */
	public static boolean isJacksonPresent() {
		return InstanceHolder.JACKSON_PRESENT;
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
		return "{ \"hash\": \"" + hexHash(obj) + "\" }";
	}

	/**
	 * Returns the {@link Object#toString()} in a JSON format.
	 *
	 * @param <T> type of the object
	 *
	 * @param obj input
	 * @return JSON String
	 */
	protected static <T> String toDebugString(final T obj) {
		class FieldExtractor {
			Long id;
		}
		FieldExtractor fieldExtractor = Converter.convert(obj).to(FieldExtractor::new);
		return "{ type:" + obj.getClass().getSimpleName()
				+ (null != fieldExtractor.id ? ", id:" + fieldExtractor.id : "")
				+ ", hash:" + hexHash(obj)
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

}

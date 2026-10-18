package org.apiphany.json.jackson2;

import org.apiphany.json.JsonBuilder;
import org.morphix.reflection.Classes;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Reflection;
import org.morphix.runtime.OptionalLibrary;

/**
 * Utility class for Jackson 2 JSON library related operations.
 * <p>
 * This class provides information about the presence of the Jackson 2 JSON library in the classpath and should not have
 * any Jackson-specific dependencies itself.
 * <p>
 * WARNING: This class should not have any dependencies on Jackson-specific classes to avoid class loading issues when
 * the library is not present in the classpath. It should only contain information about the presence of the library and
 * the specific {@link JsonBuilder} implementation to use when the library is available.
 *
 * @author Radu Sebastian LAZIN
 */
public class Jackson2Library {

	/**
	 * Jackson 2 JSON library ObjectMapper class name.
	 */
	private static final String JACKSON_2_OBJECT_MAPPER_CLASS_NAME = "com.fasterxml.jackson.databind.ObjectMapper";

	/**
	 * Jackson JDK 8 module class name. Kept as a string so callers can load when that library is absent.
	 */
	private static final String JDK8_MODULE_CLASS_NAME = "com.fasterxml.jackson.datatype.jdk8.Jdk8Module";

	/**
	 * Jackson Java time module class name. Kept as a string so callers can load when that library is absent.
	 */
	private static final String JAVA_TIME_MODULE_CLASS_NAME = "com.fasterxml.jackson.datatype.jsr310.JavaTimeModule";

	/**
	 * Library descriptor that shows if Jackson 2 JSON library is present in the classpath and the {@link JsonBuilder}
	 * specific class.
	 * <p>
	 * WARNING: Instance function needs to be lambda not method reference to avoid direct reference to class.
	 */
	public static final OptionalLibrary<? extends JsonBuilder> DESCRIPTOR =
			OptionalLibrary.of(
					JACKSON_2_OBJECT_MAPPER_CLASS_NAME,
					Jackson2JsonBuilder.class,
					() -> Jackson2JsonBuilder.instance()); // NOSONAR lambda to avoid direct reference to class

	/**
	 * Checks if the Jackson 2 JSON library is present in the classpath.
	 *
	 * @return {@code true} if the Jackson 2 JSON library is present, {@code false} otherwise
	 */
	public static boolean isPresent() {
		return DESCRIPTOR.isPresent();
	}

	/**
	 * Checks if Jackson's JDK 8 module is present in the classpath.
	 *
	 * @return {@code true} if {@code jackson-datatype-jdk8} is present, {@code false} otherwise
	 */
	public static boolean isJdk8ModulePresent() {
		return Reflection.isClassPresent(JDK8_MODULE_CLASS_NAME);
	}

	/**
	 * Returns the class name of Jackson's JDK 8 module.
	 *
	 * @return the JDK 8 module class name
	 */
	public static String jdk8ModuleClassName() {
		return JDK8_MODULE_CLASS_NAME;
	}

	/**
	 * Checks if Jackson's Java time module is present in the classpath.
	 *
	 * @return {@code true} if {@code jackson-datatype-jsr310} is present, {@code false} otherwise
	 */
	public static boolean isJavaTimeModulePresent() {
		return Reflection.isClassPresent(JAVA_TIME_MODULE_CLASS_NAME);
	}

	/**
	 * Returns the class name of Jackson's Java time module.
	 *
	 * @return the Java time module class name
	 */
	public static String javaTimeModuleClassName() {
		return JAVA_TIME_MODULE_CLASS_NAME;
	}

	/**
	 * Creates Jackson's JDK 8 module by name. Call this only when {@link #isJdk8ModulePresent()} is {@code true}.
	 *
	 * @param <T> module type
	 *
	 * @return the module instance
	 */
	public static <T> T newJdk8Module() {
		return Constructors.IgnoreAccess.newInstance(Classes.getOne(JDK8_MODULE_CLASS_NAME));
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Jackson2Library() {
		throw Constructors.unsupportedOperationException();
	}
}

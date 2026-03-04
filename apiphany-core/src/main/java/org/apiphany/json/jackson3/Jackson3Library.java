package org.apiphany.json.jackson3;

import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.LibraryDescriptor;
import org.morphix.reflection.Constructors;

/**
 * Utility class for Jackson 3 JSON library related operations.
 * <p>
 * This class provides information about the presence of the Jackson 3 JSON library in the classpath and should not have
 * any Jackson-specific dependencies itself.
 * <p>
 * WARNING: This class should not have any dependencies on Jackson-specific classes to avoid class loading issues when
 * the library is not present in the classpath. It should only contain information about the presence of the library and
 * the specific {@link JsonBuilder} implementation to use when the library is available.
 *
 * @author Radu Sebastian LAZIN
 */
public class Jackson3Library {

	/**
	 * Jackson 3 JSON library ObjectMapper class name.
	 */
	private static final String JACKSON_3_OBJECT_MAPPER_CLASS_NAME = "tools.jackson.databind.ObjectMapper";

	/**
	 * Jackson 3 JSON library JsonSerializeAs annotation class name.
	 */
	private static final String JACKSON_3_JSON_SERIALIZE_AS_CLASS_NAME = "com.fasterxml.jackson.annotation.JsonSerializeAs";

	/**
	 * Library descriptor that shows if Jackson 3 JSON library is present in the classpath and the {@link JsonBuilder}
	 * specific class.
	 * <p>
	 * WARNING: Instance function needs to be lambda not method reference to avoid direct reference to class.
	 */
	public static final LibraryDescriptor<? extends JsonBuilder> DESCRIPTOR =
			LibraryDescriptor.of(
					List.of(JACKSON_3_OBJECT_MAPPER_CLASS_NAME, JACKSON_3_JSON_SERIALIZE_AS_CLASS_NAME),
					Jackson3JsonBuilder.class,
					() -> Jackson3JsonBuilder.instance());

	/**
	 * Checks if the Jackson 3 JSON library is present in the classpath.
	 *
	 * @return {@code true} if the Jackson 3 JSON library is present, {@code false} otherwise
	 */
	public static boolean isPresent() {
		return DESCRIPTOR.isLibraryPresent();
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Jackson3Library() {
		throw Constructors.unsupportedOperationException();
	}
}

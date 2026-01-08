package org.apiphany.json.jackson;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Pair;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Reflection;

/**
 * Utility class for Jackson JSON library related operations.
 * <p>
 * This class provides information about the presence of the Jackson JSON library in the class path and should not have
 * any Jackson-specific dependencies itself.
 *
 * @author Radu Sebastian LAZIN
 */
public class JacksonLibrary {

	/**
	 * Jackson JSON library ObjectMapper class name.
	 */
	private static final String JACKSON_OBJECT_MAPPER_CLASS_NAME = "com.fasterxml.jackson.databind.ObjectMapper";

	/**
	 * Pair that shows if Jackson JSON library is present in the class path and the {@link JsonBuilder} specific class.
	 */
	public static final Pair<Boolean, Class<? extends JsonBuilder>> INFORMATION =
			Pair.of(Reflection.isClassPresent(JACKSON_OBJECT_MAPPER_CLASS_NAME), JacksonJsonBuilder.class);

	/**
	 * Private constructor to prevent instantiation.
	 */
	private JacksonLibrary() {
		throw Constructors.unsupportedOperationException();
	}
}

package org.apiphany.json.jackson2;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Pair;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Reflection;

/**
 * Utility class for Jackson 2 JSON library related operations.
 * <p>
 * This class provides information about the presence of the Jackson 2 JSON library in the classpath and should not have
 * any Jackson-specific dependencies itself.
 *
 * @author Radu Sebastian LAZIN
 */
public class Jackson2Library {

	/**
	 * Jackson 2 JSON library ObjectMapper class name.
	 */
	private static final String JACKSON_2_OBJECT_MAPPER_CLASS_NAME = "com.fasterxml.jackson.databind.ObjectMapper";

	/**
	 * Pair that shows if Jackson 2 JSON library is present in the classpath and the {@link JsonBuilder} specific class.
	 */
	public static final Pair<Boolean, Class<? extends JsonBuilder>> INFORMATION =
			Pair.of(Reflection.isClassPresent(JACKSON_2_OBJECT_MAPPER_CLASS_NAME), Jackson2JsonBuilder.class);

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Jackson2Library() {
		throw Constructors.unsupportedOperationException();
	}
}

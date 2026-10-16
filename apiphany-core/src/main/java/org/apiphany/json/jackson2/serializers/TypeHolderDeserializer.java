package org.apiphany.json.jackson2.serializers;

import java.io.IOException;

import org.morphix.reflection.Classes;
import org.morphix.reflection.TypeHolder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Custom JSON deserializer for {@link TypeHolder}.
 * <p>
 * The class type is read either from a JSON object with a {@code classType} property holding the class name (for
 * example <code>{"classType":"java.lang.String"}</code>) or from a plain JSON string holding the class name. Only class
 * types are supported, a holder describing a generic type cannot be expressed in JSON, so objects without a
 * {@code classType} are read as {@link TypeHolder#NONE}.
 *
 * @author Radu Sebastian LAZIN
 */
public class TypeHolderDeserializer extends JsonDeserializer<TypeHolder> {

	/**
	 * Name of the JSON property holding the class name.
	 */
	public static final String CLASS_TYPE = "classType";

	/**
	 * Default constructor.
	 */
	public TypeHolderDeserializer() {
		// empty
	}

	/**
	 * Deserializes JSON content into a {@link TypeHolder} instance.
	 * <p>
	 * The method processes the JSON input as follows:
	 * <ol>
	 * <li>Reads the JSON value (a JSON {@code null} deserializes to {@code null})</li>
	 * <li>A JSON string is read as a class name</li>
	 * <li>A JSON object is read using its {@code classType} property</li>
	 * <li>Any other object is read as {@link TypeHolder#NONE}</li>
	 * </ol>
	 *
	 * @param p the JSON parser containing the input to deserialize
	 * @param ctxt the deserialization context
	 * @return the deserialized {@link TypeHolder} instance or {@code null} if the JSON value is {@code null}
	 * @throws IOException if there is an error reading the JSON content
	 */
	@Override
	public TypeHolder deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
		JsonNode node = p.readValueAsTree();
		if (null == node || node.isNull()) {
			return null;
		}
		if (node.isTextual()) {
			return TypeHolder.of(Classes.getOne(node.asText()));
		}
		JsonNode classType = node.get(CLASS_TYPE);
		if (null != classType && !classType.isNull()) {
			return TypeHolder.of(Classes.getOne(classType.asText()));
		}
		return TypeHolder.NONE;
	}
}

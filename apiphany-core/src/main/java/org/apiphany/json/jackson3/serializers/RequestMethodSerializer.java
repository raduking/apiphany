package org.apiphany.json.jackson3.serializers;

import org.apiphany.RequestMethod;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Simple {@link RequestMethod} serializer that only writes the method name.
 *
 * @author Radu Sebastian LAZIN
 */
public class RequestMethodSerializer extends StdSerializer<RequestMethod> {

	/**
	 * Default constructor.
	 */
	public RequestMethodSerializer() {
		super(RequestMethod.class);
	}

	/**
	 * Serializes a {@link RequestMethod} instance by writing its name as a JSON string.
	 *
	 * @param value the {@link RequestMethod} instance to serialize
	 * @param gen the JSON generator used to write the output
	 * @param ctxt the serialization context
	 * @throws JacksonException if there is an error during serialization
	 */
	@Override
	@SuppressWarnings("resource")
	public void serialize(final RequestMethod value, final JsonGenerator gen, final SerializationContext ctxt) throws JacksonException {
		gen.writeString(value.name());
	}
}

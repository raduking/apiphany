package org.apiphany.json.jackson.serializers;

import java.io.IOException;

import org.apiphany.RequestMethod;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Simple {@link RequestMethod} serializer that only writes the method name.
 *
 * @author Radu Sebastian LAZIN
 */
public class RequestMethodSerializer extends JsonSerializer<RequestMethod> {

	/**
	 * @see #serialize(RequestMethod, JsonGenerator, SerializerProvider)
	 */
	@Override
	public void serialize(final RequestMethod value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
		gen.writeString(value.name());
	}

}

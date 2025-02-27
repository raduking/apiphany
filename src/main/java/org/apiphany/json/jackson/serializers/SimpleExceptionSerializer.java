package org.apiphany.json.jackson.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Simple exception serializer that only writes the class name.
 *
 * @author Radu Sebastian LAZIN
 */
public class SimpleExceptionSerializer extends JsonSerializer<Exception> {

	/**
	 * @see #serialize(Exception, JsonGenerator, SerializerProvider)
	 */
	@Override
	public void serialize(final Exception value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
		gen.writeString(value.getClass().getCanonicalName());
	}

}

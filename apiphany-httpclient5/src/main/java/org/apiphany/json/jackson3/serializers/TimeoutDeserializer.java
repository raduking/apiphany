package org.apiphany.json.jackson3.serializers;

import java.text.ParseException;
import java.time.Duration;

import org.apache.hc.core5.util.Timeout;
import org.apiphany.lang.Strings;
import org.apiphany.lang.Temporals;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom deserializer for {@link Timeout} to handle string representations like "infinite" or duration formats.
 *
 * @author Radu Sebastian LAZIN
 */
public class TimeoutDeserializer extends StdDeserializer<Timeout> {

	/**
	 * Default constructor.
	 */
	public TimeoutDeserializer() {
		super(Timeout.class);
	}

	/**
	 * @see StdDeserializer#deserialize(JsonParser, DeserializationContext)
	 */
	@Override
	public Timeout deserialize(final JsonParser p, final DeserializationContext ctxt) throws JacksonException {
		String value = p.getValueAsString();
		if (Strings.isEmpty(value)) {
			return null;
		}
		if ("infinite".equalsIgnoreCase(value)) {
			return Timeout.INFINITE;
		}
		try {
			Duration duration = Temporals.parseSimpleDuration(value);
			return Timeout.of(duration);
		} catch (Exception e) {
			// ignore and try other formats
		}
		try {
			return Timeout.parse(value);
		} catch (ParseException e) {
			throw DatabindException.from(p, "Invalid timeout format: " + value + ", " + e.getMessage());
		}
	}
}

package org.apiphany.json.jackson3.serializers;

import java.time.Month;
import java.util.Locale;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom {@link Month} deserializer that accepts the month name (case insensitive) and the 1-based month number (either
 * as a JSON number or as a string).
 *
 * @author Radu Sebastian LAZIN
 */
public class MonthDeserializer extends StdDeserializer<Month> {

	/**
	 * Default constructor.
	 */
	public MonthDeserializer() {
		super(Month.class);
	}

	/**
	 * @see StdDeserializer#deserialize(JsonParser, DeserializationContext)
	 */
	@Override
	public Month deserialize(final JsonParser p, final DeserializationContext ctxt) throws JacksonException {
		if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
			return Month.of(p.getIntValue());
		}
		String value = p.getValueAsString();
		if (null == value) {
			return null;
		}
		try {
			return Month.valueOf(value.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			return Month.of(Integer.parseInt(value));
		}
	}
}

package org.apiphany.json.jackson2.serializers;

import java.io.IOException;
import java.time.Month;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Custom {@link Month} deserializer that accepts the month name (case-insensitive) and the 1-based month number (either
 * as a JSON number or as a string).
 *
 * @author Radu Sebastian LAZIN
 */
public class MonthDeserializer extends JsonDeserializer<Month> {

	/**
	 * Default constructor.
	 */
	public MonthDeserializer() {
		// empty
	}

	/**
	 * @see JsonDeserializer#deserialize(JsonParser, DeserializationContext)
	 */
	@Override
	public Month deserialize(final JsonParser p, final DeserializationContext c) throws IOException {
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

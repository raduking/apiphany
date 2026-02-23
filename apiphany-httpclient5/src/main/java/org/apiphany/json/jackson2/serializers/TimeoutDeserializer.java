package org.apiphany.json.jackson2.serializers;

import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;

import org.apache.hc.core5.util.Timeout;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom deserializer for {@link Timeout} to handle string representations like "infinite" or duration formats.
 *
 * @author Radu Sebastian LAZIN
 */
public class TimeoutDeserializer extends StdDeserializer<Timeout> {

	private static final long serialVersionUID = 3036490563258813683L;

	public TimeoutDeserializer() {
		super(Timeout.class);
	}

	@Override
	public Timeout deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
		String value = p.getValueAsString();
		if (value == null || value.isEmpty()) {
			return null;
		}
		if ("infinite".equalsIgnoreCase(value)) {
			return Timeout.INFINITE;
		}
		Duration duration = null;
		try {
			duration = parseSimpleDuration(value);
			return Timeout.of(duration);
		} catch (Exception e) {
			// ignore and try other formats
		}
		try {
			return Timeout.parse(value);
		} catch (ParseException e) {
			throw new IOException("Invalid timeout format: " + value, e);
		}
	}

	public static Duration parseSimpleDuration(final String input) {
		if (input.endsWith("s")) {
			long seconds = Long.parseLong(input.replace("s", ""));
			return Duration.ofSeconds(seconds);
		} else if (input.endsWith("m")) {
			long minutes = Long.parseLong(input.replace("m", ""));
			return Duration.ofMinutes(minutes);
		} else if (input.endsWith("h")) {
			long hours = Long.parseLong(input.replace("h", ""));
			return Duration.ofHours(hours);
		}
		throw new IllegalArgumentException("Unsupported format: " + input);
	}
}

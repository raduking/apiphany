package org.apiphany.json.jackson2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.morphix.reflection.Constructors;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

/**
 * Builds Jackson's {@link JavaTimeModule} with the formatters Apiphany uses for {@link LocalDateTime} and
 * {@link LocalDate}.
 * <p>
 * WARNING: This class references {@code jackson-datatype-jsr310} types directly, so it can only be loaded when that
 * library is on the classpath. Call {@link #create(DateTimeFormatter, DateTimeFormatter)} only when
 * {@link Jackson2Library#isJavaTimeModulePresent()} is {@code true}. Keeping these references here instead of in
 * {@link Jackson2JsonBuilder} lets the builder load when the library is absent.
 *
 * @author Radu Sebastian LAZIN
 */
public final class JavaTimeJackson2Module {

	/**
	 * Creates a new Java time module with the given formatters.
	 *
	 * @param dateTimeFormatter the date time formatter object
	 * @param dateFormatter the date formatter object
	 * @return java time module
	 */
	public static SimpleModule create(final DateTimeFormatter dateTimeFormatter, final DateTimeFormatter dateFormatter) {
		return new JavaTimeModule()
				.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter))
				.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter))
				.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter))
				.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
	}

	/**
	 * Hide constructor.
	 */
	private JavaTimeJackson2Module() {
		throw Constructors.unsupportedOperationException();
	}
}

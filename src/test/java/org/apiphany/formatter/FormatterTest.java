package org.apiphany.formatter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.HashMap;
import java.util.Map;

import org.apiphany.lang.Strings;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Test class for `formatter/java-formatter.xml`.
 *
 * @author Radu Sebastian LAZIN
 */
public class FormatterTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(FormatterTest.class);

	private static final String FORMATTER_FILE_PATH = "java-formatter.xml";

	@SuppressWarnings("unchecked")
	@Test
	void shouldHaveValidFormatterFile() throws JsonMappingException, JsonProcessingException {
		String xml = Strings.fromFile(FORMATTER_FILE_PATH, e -> LOGGER.error("Could not read formatter file", e));

		XmlMapper xmlMapper = new XmlMapper();
		Map<String, String> optionsMap = xmlMapper.readValue(xml, HashMap.class);

		DefaultCodeFormatterOptions options = DefaultCodeFormatterOptions.getDefaultSettings();

		assertDoesNotThrow(() -> options.set(optionsMap));
	}

}

package org.apiphany.codeformat;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Test class for `java-code-style.xml` copied from `code-format` module.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaCodeStyleTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(JavaCodeStyleTest.class);

	private static final String FORMATTER_FILE = "java-code-style.xml";

	@Test
	void shouldValidateFormatterFileWithProvidedJDT() throws IOException {
		String xml = readString(FORMATTER_FILE);

		Map<String, String> settings = getSettingsMap(xml);

		DefaultCodeFormatterOptions options = DefaultCodeFormatterOptions.getDefaultSettings();

		assertDoesNotThrow(() -> options.set(settings));

		for (Map.Entry<String, String> entry : options.getMap().entrySet()) {
			String key = entry.getKey();
			String expectedValue = entry.getValue();
			String actualValue = settings.get(key);

			if (!Objects.equals(actualValue, expectedValue)) {
				LOGGER.info("Wrong/missing setting: '{}' : expected='{}' actual='{}'", key, expectedValue, actualValue);
			}
			assertThat("Setting '" + key + "' should match", actualValue, equalTo(expectedValue));
		}
	}

	private static String readString(final String formatterFile) throws IOException {
		try (InputStream inputStream = JavaCodeStyleTest.class.getClassLoader().getResourceAsStream(formatterFile)) {
			if (inputStream == null) {
				throw new FileNotFoundException("File not found in classpath: " + formatterFile);
			}
			return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static Map<String, String> getSettingsMap(final String xml) throws JsonProcessingException {
		XmlMapper xmlMapper = new XmlMapper();
		JsonNode xmlRoot = xmlMapper.readTree(xml);

		Map<String, String> settings = new HashMap<>();

		xmlRoot.get("profile")
				.forEach(child -> {
					if (!child.isArray()) {
						return;
					}
					ArrayNode arrayNode = (ArrayNode) child;
					arrayNode.forEach(setting -> {
						JsonNode idNode = setting.get("id");
						if (idNode == null) {
							return;
						}
						JsonNode valueNode = setting.get("value");
						if (valueNode == null) {
							return;
						}
						String id = idNode.asText();
						String value = valueNode.asText();
						settings.put(id, value);
					});
				});
		return settings;
	}
}

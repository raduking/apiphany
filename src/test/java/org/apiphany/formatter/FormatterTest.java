package org.apiphany.formatter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apiphany.lang.Strings;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Test class for `formatter/java-formatter.xml`.
 *
 * @author Radu Sebastian LAZIN
 */
public class FormatterTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(FormatterTest.class);

	private static final String FORMATTER_FILE_PATH = "java-formatter.xml";

	@Test
	void shouldHaveValidFormatterFile() throws JsonMappingException, JsonProcessingException {
		String xml = Strings.fromFile(FORMATTER_FILE_PATH, e -> LOGGER.error("Could not read formatter file", e));

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

	private static Map<String, String> getSettingsMap(final String xml) throws JsonProcessingException, JsonMappingException {
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

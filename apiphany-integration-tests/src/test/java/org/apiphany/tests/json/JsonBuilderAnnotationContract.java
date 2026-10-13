package org.apiphany.tests.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.lang.annotation.AsValue;
import org.apiphany.lang.annotation.Creator;
import org.apiphany.lang.annotation.FieldName;
import org.apiphany.lang.annotation.FieldOrder;
import org.apiphany.lang.annotation.Ignored;
import org.apiphany.security.Sensitive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for the Apiphany JSON annotations ({@link Sensitive}, {@link Creator}, {@link FieldName},
 * {@link FieldOrder}, {@link Ignored}, {@link AsValue}) that must behave identically for every JSON library
 * implementation.
 *
 * @author Radu Sebastian LAZIN
 */
public interface JsonBuilderAnnotationContract extends JsonBuilderContract {

	/**
	 * Transfer object with a sensitive field.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class SensitiveDTO {

		String id;

		@Sensitive
		String password;

		public SensitiveDTO() {
			// empty
		}

		public SensitiveDTO(final String id, final String password) {
			this.id = id;
			this.password = password;
		}

		public String getId() {
			return id;
		}

		public void setId(final String id) {
			this.id = id;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(final String password) {
			this.password = password;
		}
	}

	@DisplayName("JSON: sensitive fields are not included in the JSON output")
	@Test
	default void shouldNotIncludeSensitiveFieldsInJson() {
		SensitiveDTO dto = new SensitiveDTO(ID, PASSWORD);

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

		assertThat(json, equalTo("{\"id\":\"id1\"}"));
	}

	/**
	 * Transfer object with a redacted sensitive field.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class RedactedDTO {

		String id;

		@Sensitive(visibility = Sensitive.Visibility.REDACTED)
		String password;

		public RedactedDTO() {
			// empty
		}

		public RedactedDTO(final String id, final String password) {
			this.id = id;
			this.password = password;
		}

		public String getId() {
			return id;
		}

		public void setId(final String id) {
			this.id = id;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(final String password) {
			this.password = password;
		}
	}

	@DisplayName("JSON: redacted sensitive fields are replaced with the redacted value")
	@Test
	default void shouldRedactSensitiveFieldsInJson() {
		RedactedDTO dto = new RedactedDTO(ID, PASSWORD);

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

		assertThat(json, equalTo("{\"id\":\"id1\",\"password\":\"" + Sensitive.Value.REDACTED + "\"}"));
	}

	/**
	 * Transfer object combining redacted and hidden sensitive fields.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class CombinedSensitiveDTO {

		String id;

		@Sensitive(visibility = Sensitive.Visibility.REDACTED)
		String password;

		@Sensitive(visibility = Sensitive.Visibility.HIDDEN)
		String secret;

		public CombinedSensitiveDTO() {
			// empty
		}

		public CombinedSensitiveDTO(final String id, final String password, final String secret) {
			this.id = id;
			this.password = password;
			this.secret = secret;
		}

		public String getId() {
			return id;
		}

		public void setId(final String id) {
			this.id = id;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(final String password) {
			this.password = password;
		}

		public String getSecret() {
			return secret;
		}

		public void setSecret(final String secret) {
			this.secret = secret;
		}
	}

	@DisplayName("JSON: hidden sensitive fields are omitted and redacted ones are replaced")
	@Test
	default void shouldRedactAndHideSensitiveFieldsInJson() {
		CombinedSensitiveDTO dto = new CombinedSensitiveDTO(ID, PASSWORD, "s123");

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

		assertThat(json, equalTo("{\"id\":\"id1\",\"password\":\"" + Sensitive.Value.REDACTED + "\"}"));
	}

	/**
	 * Credit card transfer object used by the complex redaction contract.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class CreditCard {

		private String number;
		private String expiry;

		public CreditCard() {
			// empty
		}

		public CreditCard(final String number, final String expiry) {
			this.number = number;
			this.expiry = expiry;
		}

		public String getNumber() {
			return number;
		}

		public void setNumber(final String number) {
			this.number = number;
		}

		public String getExpiry() {
			return expiry;
		}

		public void setExpiry(final String expiry) {
			this.expiry = expiry;
		}
	}

	/**
	 * Transfer object with complex redacted sensitive fields.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class ComplexRedactedDTO {

		@Sensitive(visibility = Sensitive.Visibility.REDACTED)
		private List<String> passwordHistory;

		@Sensitive(visibility = Sensitive.Visibility.REDACTED)
		private Map<String, String> securityQA;

		@Sensitive(visibility = Sensitive.Visibility.REDACTED)
		private CreditCard creditCard;

		public ComplexRedactedDTO() {
			// empty
		}

		public ComplexRedactedDTO(final List<String> passwordHistory, final Map<String, String> securityQA, final CreditCard creditCard) {
			this.passwordHistory = passwordHistory;
			this.securityQA = securityQA;
			this.creditCard = creditCard;
		}

		public List<String> getPasswordHistory() {
			return passwordHistory;
		}

		public void setPasswordHistory(final List<String> passwordHistory) {
			this.passwordHistory = passwordHistory;
		}

		public Map<String, String> getSecurityQA() {
			return securityQA;
		}

		public void setSecurityQA(final Map<String, String> securityQA) {
			this.securityQA = securityQA;
		}

		public CreditCard getCreditCard() {
			return creditCard;
		}

		public void setCreditCard(final CreditCard creditCard) {
			this.creditCard = creditCard;
		}
	}

	@DisplayName("JSON: complex redacted sensitive fields are emptied recursively")
	@Test
	default void shouldRedactComplexSensitiveFieldsInJsonGoingRecursively() {
		ComplexRedactedDTO dto = new ComplexRedactedDTO(
				List.of("p1", "p2"),
				Map.of("q1", "a1", "q2", "a2"),
				new CreditCard("4111111111111111", "12/25"));

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

		assertThat(json,
				equalTo("{\"passwordHistory\":[],"
						+ "\"securityQA\":{},"
						+ "\"creditCard\":{}}"));
	}

	/**
	 * Transfer object without a default constructor.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class NoDefaultConstructorDTO {

		private String id;

		@Creator
		public NoDefaultConstructorDTO(final @FieldName("id") String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public void setId(final String id) {
			this.id = id;
		}
	}

	@DisplayName("JSON: objects without a default constructor use the annotated creator")
	@Test
	default void shouldTransformObjectWithoutDefaultConstructorToJsonAndReadItBack() {
		NoDefaultConstructorDTO a1 = new NoDefaultConstructorDTO(ID);

		Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

		NoDefaultConstructorDTO a2 = JsonBuilder.fromJson(json1, NoDefaultConstructorDTO.class);

		Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

		assertThat(json1, equalTo(json2));
	}

	/**
	 * Transfer object with a custom field name.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class CustomFieldNameDTO {

		@FieldName("custom_id")
		private String id;

		public CustomFieldNameDTO() {
			// empty
		}

		public CustomFieldNameDTO(final String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public void setId(final String id) {
			this.id = id;
		}
	}

	@DisplayName("JSON: the custom field name annotation changes the JSON property name")
	@Test
	default void shouldUseCustomFieldNameInJson() {
		CustomFieldNameDTO dto = new CustomFieldNameDTO(ID);

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

		assertThat(json, equalTo("{\"custom_id\":\"id1\"}"));
	}

	/**
	 * Transfer object with a custom creator.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class CustomCreatorDTO {

		private String id;

		@Creator
		public CustomCreatorDTO(final @FieldName("id") String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public void setId(final String id) {
			this.id = id;
		}
	}

	@DisplayName("JSON: the custom creator is used to instantiate the object")
	@Test
	default void shouldUseCustomCreatorToInstantiateObject() {
		CustomCreatorDTO a1 = new CustomCreatorDTO(ID);

		Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

		CustomCreatorDTO a2 = JsonBuilder.fromJson(json1, CustomCreatorDTO.class);

		Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

		assertThat(json1, equalTo(json2));
	}

	/**
	 * Transfer object with an ignored field.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class IgnoredFieldDTO {

		private String id;

		@Ignored
		private String ignoredField;

		public IgnoredFieldDTO() {
			// empty
		}

		public IgnoredFieldDTO(final String id, final String ignoredField) {
			this.id = id;
			this.ignoredField = ignoredField;
		}

		public String getId() {
			return id;
		}

		public void setId(final String id) {
			this.id = id;
		}

		public String getIgnoredField() {
			return ignoredField;
		}

		public void setIgnoredField(final String ignoredField) {
			this.ignoredField = ignoredField;
		}
	}

	@DisplayName("JSON: ignored fields are not included in the JSON output")
	@Test
	default void shouldNotIncludeIgnoredFieldsInJson() {
		IgnoredFieldDTO dto = new IgnoredFieldDTO(ID, "ignored");

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

		assertThat(json, equalTo("{\"id\":\"id1\"}"));
	}

	/**
	 * Enum with a custom value used by the {@link AsValue} contract.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	enum CustomEnum {

		VALUE1,
		VALUE2;

		@AsValue
		@Override
		public String toString() {
			return this == VALUE1 ? "custom_value" : "VALUE2";
		}
	}

	@DisplayName("JSON: the custom enum value is used for serialization")
	@Test
	default void shouldUseCustomFieldNameForEnumValue() {
		CustomEnum e = CustomEnum.VALUE1;

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(e));

		assertThat(json, equalTo("\"custom_value\""));
	}

	@DisplayName("JSON: the custom enum toString is used for serialization")
	@Test
	default void shouldUseCustomToStringForEnumValue() {
		CustomEnum e = CustomEnum.VALUE2;

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(e));

		assertThat(json, equalTo("\"VALUE2\""));
	}

	/**
	 * Transfer object holding a custom enum value.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class EnumDTO {

		private CustomEnum value;

		public EnumDTO() {
			// empty
		}

		public EnumDTO(final CustomEnum value) {
			this.value = value;
		}

		public CustomEnum getValue() {
			return value;
		}

		public void setValue(final CustomEnum value) {
			this.value = value;
		}
	}

	@DisplayName("JSON: the custom enum value is used when nested in an object")
	@Test
	default void shouldUseCustomToStringForEnumValueWhenNestedInObject() {
		EnumDTO dto = new EnumDTO(CustomEnum.VALUE1);

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

		assertThat(json, equalTo("{\"value\":\"custom_value\"}"));
	}

	@DisplayName("JSON: the custom enum value is used when nested in an object with a generic type")
	@Test
	default void shouldUseCustomToStringForEnumValueWhenNestedInObjectWithGenericType() {
		GenericDTO<CustomEnum> dto = new GenericDTO<>(CustomEnum.VALUE2);

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

		assertThat(json, equalTo("{\"value\":\"VALUE2\"}"));
	}

	/**
	 * Transfer object with an explicit field order.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@FieldOrder({ "second", "first" })
	class FieldOrderDTO {

		private String first;

		private String second;

		public FieldOrderDTO() {
			// empty
		}

		public FieldOrderDTO(final String first, final String second) {
			this.first = first;
			this.second = second;
		}

		public String getFirst() {
			return first;
		}

		public void setFirst(final String first) {
			this.first = first;
		}

		public String getSecond() {
			return second;
		}

		public void setSecond(final String second) {
			this.second = second;
		}
	}

	@DisplayName("JSON: the field order annotation controls the JSON property order")
	@Test
	default void shouldUseCustomFieldOrderInJson() {
		FieldOrderDTO dto = new FieldOrderDTO("firstValue", "secondValue");

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

		assertThat(json, equalTo("{\"second\":\"secondValue\",\"first\":\"firstValue\"}"));
	}
}

package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.morphix.lang.function.Predicates.allOf;
import static org.morphix.lang.function.Predicates.not;
import static org.morphix.reflection.predicates.MemberPredicates.isNotStatic;
import static org.morphix.reflection.predicates.MemberPredicates.nameIn;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

/**
 * Test class for {@link OAuth2ClientRegistration}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2ClientRegistrationTest {

	@Test
	void shouldLoadFromFile() {
		String json = Strings.fromFile("security/oauth2/oauth2-client-registration.json");

		OAuth2ClientRegistration result = JsonBuilder.fromJson(json, OAuth2ClientRegistration.class);

		assertThat(result, notNullValue());
	}

	@Test
	void shouldSerializeToJson() {
		String json = Strings.fromFile("security/oauth2/oauth2-client-registration.json");

		OAuth2ClientRegistration result1 = JsonBuilder.fromJson(json, OAuth2ClientRegistration.class);

		json = result1.toString();

		OAuth2ClientRegistration result2 = JsonBuilder.fromJson(json, OAuth2ClientRegistration.class);

		assertThat(result1.toString(), equalTo(result2.toString()));
	}

	@Test
	void shouldHaveTheSameFieldsAsTheSpringVersion() {
		List<Field> fieldsA = Fields.getAllDeclared(OAuth2ClientRegistration.class, isNotStatic());

		List<String> excluded = List.of("providerDetails", "clientSettings");
		List<Field> fieldsS = Fields.getAllDeclared(ClientRegistration.class, allOf(isNotStatic(), not(nameIn(excluded))));

		for (Field fieldS : fieldsS) {
			String fieldSName = fieldS.getName();
			Field foundFieldA = null;
			for (Field fieldA : fieldsA) {
				if (Objects.equals(fieldA.getName(), fieldSName)) {
					foundFieldA = fieldA;
					break;
				}
			}
			if (null == foundFieldA) {
				fail("Field: " + fieldS + " not found");
			}
		}
	}

	@Test
	void shouldDeserializeButNotSerializeClientSecret() {
		String jsonRead = Strings.fromFile("security/oauth2/oauth2-client-registration.json");
		OAuth2ClientRegistration registrationRead = JsonBuilder.fromJson(jsonRead, OAuth2ClientRegistration.class);

		String jsonWrite = registrationRead.toString();
		OAuth2ClientRegistration registrationWrite = JsonBuilder.fromJson(jsonWrite, OAuth2ClientRegistration.class);

		assertThat(registrationRead.getClientSecret(), notNullValue());
		assertThat(registrationWrite.getClientSecret(), nullValue());
	}
}

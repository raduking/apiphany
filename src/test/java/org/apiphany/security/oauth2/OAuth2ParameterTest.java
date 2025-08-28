package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.MemberAccessor;

/**
 * Test class {@link OAuth2Parameter}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2ParameterTest {

	@ParameterizedTest
	@EnumSource(OAuth2Parameter.class)
	void shouldReturnEnumFromString(final OAuth2Parameter param) {
		OAuth2Parameter result = OAuth2Parameter.fromString(param.value());

		assertThat(result, equalTo(param));
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateDefaultNestedClass() throws Exception {
		Throwable targetException = null;
		Constructor<OAuth2Parameter.Default> defaultConstructor = OAuth2Parameter.Default.class.getDeclaredConstructor();
		try (MemberAccessor<Constructor<OAuth2Parameter.Default>> ignored = new MemberAccessor<>(null, defaultConstructor)) {
			defaultConstructor.newInstance();
		} catch (InvocationTargetException e) {
			assertThat(e.getTargetException().getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
			targetException = e.getTargetException();
		}
		assertTrue(targetException instanceof UnsupportedOperationException);
	}
}

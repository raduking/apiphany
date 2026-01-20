package org.apiphany.security.oauth2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apiphany.utils.Tests;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link OAuth2TokenProviderProperties}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2TokenProviderOptionsTest {

	@Test
	void shouldNotInstantiateDefaultClass() {
		UnsupportedOperationException unsupportedOperationException =
				Tests.verifyDefaultConstructorThrows(OAuth2TokenProviderProperties.Default.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

}

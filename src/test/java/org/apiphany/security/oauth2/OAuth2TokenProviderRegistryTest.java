package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.apiphany.lang.ScopedResource;
import org.apiphany.security.AuthenticationTokenProvider;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link OAuth2TokenProviderRegistry}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2TokenProviderRegistryTest {

	private static final String PROVIDER_NAME_MY_PROVIDER = "myProvider";
	private static final String PROVIDER_NAME_GOOD = "good";
	private static final String PROVIDER_NAME_BAD = "bad";

	private static final String ERROR_MESSAGE = "boom";

	private static final Object CLIENT_REGISTRATION_NAME = "clientA";

	@SuppressWarnings({ "resource", "unchecked" })
	@Test
	void shouldAddProviderAndRetrieveIt() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProvider provider = mock(OAuth2TokenProvider.class);
		ScopedResource<OAuth2TokenProvider> resource = mock(ScopedResource.class);
		doReturn(provider).when(resource).unwrap();

		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		registry.add(PROVIDER_NAME_MY_PROVIDER, resource);
		registry.close();

		assertThat(registry.getProviderNames(), hasSize(1));
		assertThat(registry.getProviderNames().getFirst(), equalTo(PROVIDER_NAME_MY_PROVIDER));

		assertThat(registry.getProviders(), hasSize(1));
		assertThat(registry.getProviders().getFirst(), equalTo(provider));

		verify(resource).closeIfManaged();
	}

	@SuppressWarnings({ "resource", "unchecked" })
	@Test
	void shouldRejectDuplicateProvider() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		ScopedResource<OAuth2TokenProvider> first = mock(ScopedResource.class);
		ScopedResource<OAuth2TokenProvider> second = mock(ScopedResource.class);

		registry.add(PROVIDER_NAME_MY_PROVIDER, first);

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.add(PROVIDER_NAME_MY_PROVIDER, second));

		assertThat(ex.getMessage(), equalTo("An OAuth2 token provider with name '" + PROVIDER_NAME_MY_PROVIDER + "' is already registered"));

		verify(second).closeIfManaged(); // cleanup path covered
	}

	@SuppressWarnings("unchecked")
	@Test
	void shouldRejectAddWhenRegistryClosing() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		registry.close();

		ScopedResource<OAuth2TokenProvider> resource = mock(ScopedResource.class);

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.add(PROVIDER_NAME_MY_PROVIDER, resource));

		assertThat(ex.getMessage(), equalTo("Cannot add new OAuth2 token provider " + PROVIDER_NAME_MY_PROVIDER + " to a closing registry"));

		verify(resource).closeIfManaged(); // cleanup-on-add-during-close
	}

	@SuppressWarnings("unchecked")
	@Test
	void shouldCloseAllProvidersEvenIfSomeFail() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		ScopedResource<OAuth2TokenProvider> good = mock(ScopedResource.class);
		ScopedResource<OAuth2TokenProvider> bad = mock(ScopedResource.class);

		doThrow(new RuntimeException(ERROR_MESSAGE)).when(bad).closeIfManaged();

		registry.add(PROVIDER_NAME_GOOD, good);
		registry.add(PROVIDER_NAME_BAD, bad);

		registry.close();

		verify(good).closeIfManaged();
		verify(bad).closeIfManaged(); // exception path covered
	}

    @Test
    void shouldCreateRegistryFromTokenSupplierFactories() {
        OAuth2Registry mockRegistry = mock(OAuth2Registry.class);

        doAnswer(inv -> {
            OAuth2TokenProvider wrapper = mock(OAuth2TokenProvider.class);
            doReturn(CLIENT_REGISTRATION_NAME).when(wrapper).getClientRegistrationName();
            return List.of(wrapper);
        }).when(mockRegistry).tokenProviders(any());

        OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(
                mockRegistry,
                (r, d) -> mock(AuthenticationTokenProvider.class),
                name -> "mapped-" + name
        );

		assertThat(registry.getProviders(), hasSize(1));
		assertThat(registry.getProviderNames(), hasSize(1));
		assertThat(registry.getProviderNames().getFirst(), equalTo("mapped-" + CLIENT_REGISTRATION_NAME));
    }
}

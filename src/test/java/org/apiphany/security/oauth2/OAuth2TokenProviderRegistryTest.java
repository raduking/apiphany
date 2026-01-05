package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import org.apiphany.lang.ScopedResource;
import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationToken;
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
	private static final String PROVIDER_NAME_1 = "provider1";
	private static final String PROVIDER_NAME_2 = "provider2";

	private static final String ERROR_MESSAGE = "boom";

	private static final String CLIENT_REGISTRATION_NAME = "clientRegistration";
	private static final String CLIENT_REGISTRATION_1 = "registration1";
	private static final String CLIENT_REGISTRATION_2 = "registration2";

	private static final int THREADS = 200;
	private static final int CLOSE_COUNT = 10;

	private static final long EXPIRES_IN = 300;
	private static final Instant DEFAULT_EXPIRATION = Instant.now();
	private static final String TOKEN = Strings.fromFile("/security/oauth2/access-token.txt");

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

		OAuth2TokenProvider retrievedProvider = registry.getProvider(PROVIDER_NAME_MY_PROVIDER);

		assertThat(registry.getProviderNames(), hasSize(1));
		assertThat(registry.getProviderNames().getFirst(), equalTo(PROVIDER_NAME_MY_PROVIDER));

		assertThat(registry.getProviders(), hasSize(1));
		assertThat(registry.getProviders().getFirst(), equalTo(provider));

		assertThat(retrievedProvider, equalTo(provider));

		verify(resource).closeIfManaged();
	}

	@SuppressWarnings({ "resource", "unchecked" })
	@Test
	void shouldReturnNullWhenRetrievingProviderThatDoesNotExist() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProvider provider = mock(OAuth2TokenProvider.class);
		ScopedResource<OAuth2TokenProvider> resource = mock(ScopedResource.class);
		doReturn(provider).when(resource).unwrap();

		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		registry.add(PROVIDER_NAME_MY_PROVIDER, resource);
		registry.close();

		OAuth2TokenProvider retrievedProvider = registry.getProvider("ubknownProvider");

		assertThat(registry.getProviderNames(), hasSize(1));
		assertThat(registry.getProviderNames().getFirst(), equalTo(PROVIDER_NAME_MY_PROVIDER));

		assertThat(registry.getProviders(), hasSize(1));
		assertThat(registry.getProviders().getFirst(), equalTo(provider));

		assertThat(retrievedProvider, equalTo(null));

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

		assertThat(ex.getMessage(), equalTo("An OAuth2 token provider with name '" + PROVIDER_NAME_MY_PROVIDER + "' is already registered."));

		verify(second).closeIfManaged();
	}

	@SuppressWarnings({ "resource", "unchecked" })
	@Test
	void shouldRejectDuplicateProviderEvenIfProviderCannotBeClosed() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		ScopedResource<OAuth2TokenProvider> first = mock(ScopedResource.class);
		ScopedResource<OAuth2TokenProvider> second = mock(ScopedResource.class);
		doThrow(new RuntimeException(ERROR_MESSAGE)).when(second).closeIfManaged();

		registry.add(PROVIDER_NAME_MY_PROVIDER, first);

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.add(PROVIDER_NAME_MY_PROVIDER, second));

		assertThat(ex.getMessage(), equalTo("An OAuth2 token provider with name '" + PROVIDER_NAME_MY_PROVIDER + "' is already registered."));

		verify(second).closeIfManaged();
	}

	@SuppressWarnings("unchecked")
	@Test
	void shouldRejectAddWhenRegistryClosing() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		registry.close();

		ScopedResource<OAuth2TokenProvider> resource = mock(ScopedResource.class);

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.add(PROVIDER_NAME_MY_PROVIDER, resource));

		assertThat(ex.getMessage(), equalTo("Cannot add new OAuth2 token provider " + PROVIDER_NAME_MY_PROVIDER + " to a closing registry."));

		verify(resource).closeIfManaged();
	}

	@SuppressWarnings("unchecked")
	@Test
	void shouldNotThrowExceptionWhenClosingProviderThrowsExceptionOndRejectAddWhenRegistryClosing() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		registry.close();

		ScopedResource<OAuth2TokenProvider> resource = mock(ScopedResource.class);
		doThrow(new RuntimeException(ERROR_MESSAGE)).when(resource).closeIfManaged();

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.add(PROVIDER_NAME_MY_PROVIDER, resource));

		assertThat(ex.getMessage(), equalTo("Cannot add new OAuth2 token provider " + PROVIDER_NAME_MY_PROVIDER + " to a closing registry."));

		verify(resource).closeIfManaged();
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
		verify(bad).closeIfManaged();
	}

	@SuppressWarnings({ "unchecked", "resource" })
	@Test
	void shouldNotCallCloseOnAllProvidersEvenIfRegistryCloseIsCalledMultipleTimes() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		ScopedResource<OAuth2TokenProvider> tokenProvider = mock(ScopedResource.class);

		registry.add(PROVIDER_NAME_MY_PROVIDER, tokenProvider);

		for (int i = 0; i < CLOSE_COUNT; ++i) {
			registry.close();
		}

		verify(tokenProvider).closeIfManaged();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldCreateRegistryFromTokenSupplierFactories() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProvider tokenProvider = mock(OAuth2TokenProvider.class);
		doReturn(CLIENT_REGISTRATION_NAME).when(tokenProvider).getClientRegistrationName();

		OAuth2TokenClientSupplier tokenClientSupplier = (r, d) -> mock(AuthenticationTokenProvider.class);
		doReturn(List.of(tokenProvider)).when(mockRegistry).tokenProviders(any());

		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(
				mockRegistry,
				tokenClientSupplier,
				OAuth2TokenProviderRegistryTest::nameConverter);

		registry.close();

		assertThat(registry.getProviders(), hasSize(1));
		assertThat(registry.getProviderNames(), hasSize(1));
		assertThat(registry.getProviderNames().getFirst(), equalTo(nameConverter(CLIENT_REGISTRATION_NAME)));

		verify(mockRegistry).tokenProviders(tokenClientSupplier);
	}

	@SuppressWarnings("resource")
	@Test
	void shouldCreateRegistryFromTokenSupplierFactoriesWithoutNameConverters() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProvider tokenProvider = mock(OAuth2TokenProvider.class);
		doReturn(CLIENT_REGISTRATION_NAME).when(tokenProvider).getClientRegistrationName();

		OAuth2TokenClientSupplier tokenClientSupplier = (r, d) -> mock(AuthenticationTokenProvider.class);
		doReturn(List.of(tokenProvider)).when(mockRegistry).tokenProviders(any());

		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(
				mockRegistry,
				tokenClientSupplier);

		registry.close();

		assertThat(registry.getProviders(), hasSize(1));
		assertThat(registry.getProviderNames(), hasSize(1));
		assertThat(registry.getProviderNames().getFirst(), equalTo(CLIENT_REGISTRATION_NAME));

		verify(mockRegistry).tokenProviders(tokenClientSupplier);
	}

	@SuppressWarnings({ "resource", "unchecked" })
	@Test
	void shouldCreateRegistryFromTokenSupplierFactoriesWithCustomizer() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProvider tokenProvider = mock(OAuth2TokenProvider.class);
		doReturn(CLIENT_REGISTRATION_NAME).when(tokenProvider).getClientRegistrationName();

		OAuth2TokenClientSupplier tokenClientSupplier = (r, d) -> mock(AuthenticationTokenProvider.class);
		doReturn(List.of(tokenProvider)).when(mockRegistry).tokenProviders(any());

		BiConsumer<String, OAuth2TokenProvider> customizer = mock(BiConsumer.class);

		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(
				mockRegistry,
				tokenClientSupplier,
				OAuth2TokenProviderRegistryTest::nameConverter,
				customizer);

		registry.close();

		String expectedName = nameConverter(CLIENT_REGISTRATION_NAME);

		assertThat(registry.getProviders(), hasSize(1));
		assertThat(registry.getProviderNames(), hasSize(1));
		assertThat(registry.getProviderNames().getFirst(), equalTo(expectedName));

		verify(mockRegistry).tokenProviders(tokenClientSupplier);
		verify(customizer).accept(expectedName, tokenProvider);
	}

	@SuppressWarnings("resource")
	@Test
	void shouldCreateRegistryFromProperties() throws Exception {
		OAuth2Properties mockProperties = mock(OAuth2Properties.class);

		OAuth2ClientRegistration registration1 = mock(OAuth2ClientRegistration.class);
		doReturn(PROVIDER_NAME_1).when(registration1).getProvider();
		OAuth2ProviderDetails provider1 = mock(OAuth2ProviderDetails.class);

		OAuth2ClientRegistration registration2 = mock(OAuth2ClientRegistration.class);
		doReturn(PROVIDER_NAME_2).when(registration1).getProvider();
		OAuth2ProviderDetails provider2 = mock(OAuth2ProviderDetails.class);

		Map<String, OAuth2ClientRegistration> registrations =
				Map.of(CLIENT_REGISTRATION_1, registration1, CLIENT_REGISTRATION_2, registration2);

		doReturn(registrations).when(mockProperties).getRegistration();
		doReturn(provider1).when(mockProperties).getProviderDetails(registration1);
		doReturn(provider2).when(mockProperties).getProviderDetails(registration2);

		OAuth2TokenProvider tokenProvider = mock(OAuth2TokenProvider.class);
		doReturn(CLIENT_REGISTRATION_NAME).when(tokenProvider).getClientRegistrationName();

		AuthenticationToken token = createToken();
		AuthenticationTokenProvider tokenClient = mock(AuthenticationTokenProvider.class);
		doReturn(token).when(tokenClient).getAuthenticationToken();

		OAuth2TokenClientSupplier tokenClientSupplier = (r, d) -> tokenClient;

		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(
				mockProperties,
				tokenClientSupplier,
				OAuth2TokenProviderRegistryTest::nameConverter);

		registry.close();

		assertThat(registry.getOAuth2Registry().entries(), hasSize(2));
		assertThat(registry.getProviders(), hasSize(2));
		assertThat(registry.getProviderNames(), hasSize(2));
		assertThat(registry.getProviderNames(),
				containsInAnyOrder(nameConverter(CLIENT_REGISTRATION_1), nameConverter(CLIENT_REGISTRATION_2)));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldCreateRegistryFromPropertiesWithoutNameConverter() throws Exception {
		OAuth2Properties mockProperties = mock(OAuth2Properties.class);

		OAuth2ClientRegistration registration1 = mock(OAuth2ClientRegistration.class);
		doReturn(PROVIDER_NAME_1).when(registration1).getProvider();
		OAuth2ProviderDetails provider1 = mock(OAuth2ProviderDetails.class);

		OAuth2ClientRegistration registration2 = mock(OAuth2ClientRegistration.class);
		doReturn(PROVIDER_NAME_2).when(registration1).getProvider();
		OAuth2ProviderDetails provider2 = mock(OAuth2ProviderDetails.class);

		Map<String, OAuth2ClientRegistration> registrations =
				Map.of(CLIENT_REGISTRATION_1, registration1, CLIENT_REGISTRATION_2, registration2);

		doReturn(registrations).when(mockProperties).getRegistration();
		doReturn(provider1).when(mockProperties).getProviderDetails(registration1);
		doReturn(provider2).when(mockProperties).getProviderDetails(registration2);

		OAuth2TokenProvider tokenProvider = mock(OAuth2TokenProvider.class);
		doReturn(CLIENT_REGISTRATION_NAME).when(tokenProvider).getClientRegistrationName();

		AuthenticationToken token = createToken();
		AuthenticationTokenProvider tokenClient = mock(AuthenticationTokenProvider.class);
		doReturn(token).when(tokenClient).getAuthenticationToken();

		OAuth2TokenClientSupplier tokenClientSupplier = (r, d) -> tokenClient;

		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(
				mockProperties,
				tokenClientSupplier);

		registry.close();

		assertThat(registry.getOAuth2Registry().entries(), hasSize(2));
		assertThat(registry.getProviders(), hasSize(2));
		assertThat(registry.getProviderNames(), hasSize(2));
		assertThat(registry.getProviderNames(),
				containsInAnyOrder(CLIENT_REGISTRATION_1, CLIENT_REGISTRATION_2));
	}

	@SuppressWarnings({ "unchecked", "resource" })
	@Test
	void shouldBeThreadSafeForConcurrentAddAndClose() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		ExecutorService executor = Executors.newFixedThreadPool(THREADS);

		List<ScopedResource<OAuth2TokenProvider>> resources =
				new java.util.concurrent.CopyOnWriteArrayList<>();

		CountDownLatch startTasksGate = new CountDownLatch(1);
		CountDownLatch doneTasksGate = new CountDownLatch(THREADS);

		// Create tasks:
		// - most will attempt add()
		// - one will call close()
		// - the rest will attempt add() after/during close()
		for (int i = 0; i < THREADS; ++i) {
			executor.submit(() -> {
				try {
					ScopedResource<OAuth2TokenProvider> resource = mock(ScopedResource.class);
					resources.add(resource);

					startTasksGate.await();

					registry.add(PROVIDER_NAME_MY_PROVIDER, resource);
				} catch (Exception ignored) {
					// expected exception when duplicate or closing
				} finally {
					doneTasksGate.countDown();
				}
			});
		}

		executor.submit(() -> {
			try {
				startTasksGate.await();
				registry.close();
			} catch (Exception ignored) {
				// ignored
			}
		});

		startTasksGate.countDown();
		doneTasksGate.await();

		executor.shutdownNow();
		registry.close();

		assertThat(resources, hasSize(THREADS));

		// only one provider must be registered
		assertThat(registry.getProviders(), hasSize(1));
		assertThat(registry.getProviderNames(), hasSize(1));
		assertThat(registry.getProviderNames(), contains(PROVIDER_NAME_MY_PROVIDER));

		// all other ScopedResource objects must have been closed except the single successful one
		OAuth2TokenProvider provider = registry.getProviders().getFirst();
		ScopedResource<OAuth2TokenProvider> registeredResource = resources.stream()
				.filter(r -> r.unwrap() == provider)
				.findFirst()
				.orElse(null);

		for (ScopedResource<OAuth2TokenProvider> resource : resources) {
			if (resource != registeredResource) {
				verify(resource, atLeastOnce()).closeIfManaged();
			}
		}
	}

	private static String nameConverter(final String clientRegistrationName) {
		return "prefix" + clientRegistrationName + "Suffix";
	}

	private static AuthenticationToken createToken() {
		AuthenticationToken authenticationToken = new AuthenticationToken();
		authenticationToken.setAccessToken(TOKEN);
		authenticationToken.setExpiresIn(EXPIRES_IN);
		authenticationToken.setExpiration(DEFAULT_EXPIRATION.plusSeconds(EXPIRES_IN));
		return authenticationToken;
	}
}

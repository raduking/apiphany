package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

	@Test
	@SuppressWarnings({ "resource", "unchecked" })
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

		verify(resource).closeIfManaged(any());
	}

	@Test
	@SuppressWarnings({ "resource", "unchecked" })
	void shouldReturnNullWhenRetrievingProviderThatDoesNotExist() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProvider provider = mock(OAuth2TokenProvider.class);
		ScopedResource<OAuth2TokenProvider> resource = mock(ScopedResource.class);
		doReturn(provider).when(resource).unwrap();

		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		registry.add(PROVIDER_NAME_MY_PROVIDER, resource);
		registry.close();

		OAuth2TokenProvider retrievedProvider = registry.getProvider("unknownProvider");

		assertThat(registry.getProviderNames(), hasSize(1));
		assertThat(registry.getProviderNames().getFirst(), equalTo(PROVIDER_NAME_MY_PROVIDER));

		assertThat(registry.getProviders(), hasSize(1));
		assertThat(registry.getProviders().getFirst(), equalTo(provider));

		assertThat(retrievedProvider, equalTo(null));

		verify(resource).closeIfManaged(any());
	}

	@Test
	@SuppressWarnings({ "resource", "unchecked" })
	void shouldRejectDuplicateProvider() {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		ScopedResource<OAuth2TokenProvider> first = mock(ScopedResource.class);
		ScopedResource<OAuth2TokenProvider> second = mock(ScopedResource.class);

		registry.add(PROVIDER_NAME_MY_PROVIDER, first);

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.add(PROVIDER_NAME_MY_PROVIDER, second));

		assertThat(ex.getMessage(), equalTo("An OAuth2 token provider with name '" + PROVIDER_NAME_MY_PROVIDER + "' is already registered."));

		verify(second).closeIfManaged(any());
	}

	@Test
	@SuppressWarnings({ "resource" })
	void shouldRejectDuplicateProviderEvenIfProviderCannotBeClosed() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		OAuth2TokenProvider firstProvider = mock(OAuth2TokenProvider.class);
		OAuth2TokenProvider secondProvider = mock(OAuth2TokenProvider.class);

		ScopedResource<OAuth2TokenProvider> first = ScopedResource.managed(firstProvider);
		ScopedResource<OAuth2TokenProvider> second = ScopedResource.managed(secondProvider);

		doThrow(new RuntimeException(ERROR_MESSAGE)).when(secondProvider).close();

		registry.add(PROVIDER_NAME_MY_PROVIDER, first);

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.add(PROVIDER_NAME_MY_PROVIDER, second));

		assertThat(ex.getMessage(), equalTo("An OAuth2 token provider with name '" + PROVIDER_NAME_MY_PROVIDER + "' is already registered."));

		verify(secondProvider).close();
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldRejectAddWhenRegistryClosing() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		registry.close();

		ScopedResource<OAuth2TokenProvider> resource = mock(ScopedResource.class);

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.add(PROVIDER_NAME_MY_PROVIDER, resource));

		assertThat(ex.getMessage(), equalTo("Cannot add new OAuth2 token provider " + PROVIDER_NAME_MY_PROVIDER + " to a closing registry."));

		verify(resource).closeIfManaged(any());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldNotThrowExceptionWhenClosingProviderThrowsExceptionOndRejectAddWhenRegistryClosing() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		registry.close();

		OAuth2TokenProvider provider = mock(OAuth2TokenProvider.class);

		ScopedResource<OAuth2TokenProvider> resource = ScopedResource.managed(provider);
		doThrow(new RuntimeException(ERROR_MESSAGE)).when(provider).close();

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.add(PROVIDER_NAME_MY_PROVIDER, resource));

		assertThat(ex.getMessage(), equalTo("Cannot add new OAuth2 token provider " + PROVIDER_NAME_MY_PROVIDER + " to a closing registry."));

		verify(provider).close();
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCloseAllProvidersEvenIfSomeFail() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		OAuth2TokenProvider providerGood = mock(OAuth2TokenProvider.class);
		OAuth2TokenProvider providerBad = mock(OAuth2TokenProvider.class);

		ScopedResource<OAuth2TokenProvider> good = ScopedResource.managed(providerGood);
		ScopedResource<OAuth2TokenProvider> bad = ScopedResource.managed(providerBad);

		doThrow(new RuntimeException(ERROR_MESSAGE)).when(providerBad).close();

		registry.add(PROVIDER_NAME_GOOD, good);
		registry.add(PROVIDER_NAME_BAD, bad);

		registry.close();

		verify(providerGood).close();
		verify(providerBad).close();
	}

	@Test
	@SuppressWarnings({ "unchecked", "resource" })
	void shouldNotCallCloseOnAllProvidersEvenIfRegistryCloseIsCalledMultipleTimes() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

		ScopedResource<OAuth2TokenProvider> tokenProvider = mock(ScopedResource.class);

		registry.add(PROVIDER_NAME_MY_PROVIDER, tokenProvider);

		for (int i = 0; i < CLOSE_COUNT; ++i) {
			registry.close();
		}

		verify(tokenProvider).closeIfManaged(any());
	}

	@Test
	@SuppressWarnings("resource")
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

	@Test
	@SuppressWarnings("resource")
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

	@Test
	@SuppressWarnings({ "resource", "unchecked" })
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

	@Test
	@SuppressWarnings({ "resource", "unchecked" })
	void shouldCreateRegistryFromTokenSupplierFactoriesWithCustomizerAndNoFilter() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
		OAuth2ResolvedRegistration registration = mock(OAuth2ResolvedRegistration.class);
		doReturn(List.of(registration)).when(mockRegistry).entries();
		doReturn(CLIENT_REGISTRATION_NAME).when(registration).getClientRegistrationName();

		OAuth2TokenProvider tokenProvider = mock(OAuth2TokenProvider.class);

		OAuth2TokenClientSupplier tokenClientSupplier = (r, d) -> mock(AuthenticationTokenProvider.class);
		doReturn(tokenProvider).when(mockRegistry).tokenProvider(eq(CLIENT_REGISTRATION_NAME), any(OAuth2TokenClientSupplier.class));

		BiConsumer<String, OAuth2TokenProvider> customizer = mock(BiConsumer.class);

		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(
				mockRegistry,
				tokenClientSupplier,
				OAuth2TokenProviderRegistryTest::nameConverter,
				convertedName -> true,
				customizer);

		registry.close();

		String expectedName = nameConverter(CLIENT_REGISTRATION_NAME);

		assertThat(registry.getProviders(), hasSize(1));
		assertThat(registry.getProviderNames(), hasSize(1));
		assertThat(registry.getProviderNames().getFirst(), equalTo(expectedName));

		verify(mockRegistry).entries();
		verify(mockRegistry).tokenProvider(CLIENT_REGISTRATION_NAME, tokenClientSupplier);
		verify(customizer).accept(expectedName, tokenProvider);
	}

	@Test
	@SuppressWarnings({ "resource", "unchecked" })
	void shouldCreateRegistryFromTokenSupplierFactoriesWithCustomizerAndFilterOutProviders() throws Exception {
		OAuth2Registry mockRegistry = mock(OAuth2Registry.class);

		OAuth2ResolvedRegistration registration1 = mock(OAuth2ResolvedRegistration.class);
		doReturn(CLIENT_REGISTRATION_1).when(registration1).getClientRegistrationName();

		OAuth2ResolvedRegistration registration2 = mock(OAuth2ResolvedRegistration.class);
		doReturn(CLIENT_REGISTRATION_2).when(registration2).getClientRegistrationName();

		doReturn(List.of(registration1, registration2)).when(mockRegistry).entries();

		OAuth2TokenProvider tokenProvider = mock(OAuth2TokenProvider.class);

		OAuth2TokenClientSupplier tokenClientSupplier = (r, d) -> mock(AuthenticationTokenProvider.class);
		doReturn(tokenProvider).when(mockRegistry).tokenProvider(any(), any(OAuth2TokenClientSupplier.class));

		BiConsumer<String, OAuth2TokenProvider> customizer = mock(BiConsumer.class);

		OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(
				mockRegistry,
				tokenClientSupplier,
				OAuth2TokenProviderRegistryTest::nameConverter,
				convertedName -> !convertedName.equals(nameConverter(CLIENT_REGISTRATION_1)),
				customizer);

		registry.close();

		String expectedRegistration1Name = nameConverter(CLIENT_REGISTRATION_1);
		String expectedRegistration2Name = nameConverter(CLIENT_REGISTRATION_2);

		assertThat(registry.getProviders(), hasSize(1));
		assertThat(registry.getProviderNames(), hasSize(1));
		assertThat(registry.getProviderNames().getFirst(), equalTo(expectedRegistration2Name));

		verify(mockRegistry).entries();

		verify(mockRegistry, never()).tokenProvider(CLIENT_REGISTRATION_1, tokenClientSupplier);
		verify(customizer, never()).accept(expectedRegistration1Name, tokenProvider);

		verify(mockRegistry).tokenProvider(CLIENT_REGISTRATION_2, tokenClientSupplier);
		verify(customizer).accept(expectedRegistration2Name, tokenProvider);
	}

	@Test
	@SuppressWarnings("resource")
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

	@Test
	@SuppressWarnings("resource")
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

	@Test
	@SuppressWarnings({ "unchecked", "resource" })
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
				verify(resource, atLeastOnce()).closeIfManaged(any());
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

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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apiphany.lang.Strings;
import org.apiphany.logging.slf4j.Slf4jLoggerAdapter;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationTokenProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.lang.function.LoggerAdapter;
import org.morphix.lang.function.ThrowingRunnable;
import org.morphix.lang.resource.ScopedResource;
import org.morphix.lang.thread.Threads;
import org.morphix.reflection.Fields;

/**
 * Test class for {@link OAuth2TokenProviderRegistry}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2TokenProviderRegistryTest {

	private static final LoggerAdapter LOGGER = Slf4jLoggerAdapter.of(OAuth2ClientRegistrationTest.class);

	private static final String PROVIDER_NAME_MY_PROVIDER = "myProvider";
	private static final String PROVIDER_NAME_GOOD = "good";
	private static final String PROVIDER_NAME_BAD = "bad";
	private static final String PROVIDER_NAME_1 = "provider1";
	private static final String PROVIDER_NAME_2 = "provider2";
	private static final String PROVIDER_NAME_3 = "provider3";

	private static final String ERROR_MESSAGE = "boom";

	private static final String CLIENT_REGISTRATION_NAME = "clientRegistration";
	private static final String CLIENT_REGISTRATION_1 = "registration1";
	private static final String CLIENT_REGISTRATION_2 = "registration2";
	private static final String CLIENT_REGISTRATION_3 = "registration3";

	private static final String UNKNOWN_PROVIDER = "unknownProvider";

	private static final int THREADS = 200;
	private static final int CLOSE_COUNT = 10;
	private static final long WAIT_SECONDS = 5;

	private static final long EXPIRES_IN = 300;
	private static final Instant DEFAULT_EXPIRATION = Instant.now();
	private static final String TOKEN = Strings.fromFile("/security/oauth2/access-token.txt");

	@Nested
	class AddAndGetProviderTests {

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldAddProviderAndRetrieveIt() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
			OAuth2TokenProvider provider = mock(OAuth2TokenProvider.class);
			ScopedResource<OAuth2TokenProvider> resource = mock(ScopedResource.class);
			doReturn(provider).when(resource).unwrap();

			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

			registry.addProvider(PROVIDER_NAME_MY_PROVIDER, resource);
			registry.close();

			OAuth2TokenProvider retrievedProvider = registry.getProvider(PROVIDER_NAME_MY_PROVIDER);

			assertThat(registry.getProviderNames(), hasSize(1));
			assertThat(registry.getProviderNames().getFirst(), equalTo(PROVIDER_NAME_MY_PROVIDER));

			assertThat(registry.getProviders(), hasSize(1));
			assertThat(registry.getProviders().getFirst(), equalTo(provider));

			assertThat(retrievedProvider, equalTo(provider));

			verify(resource).closeIfManaged(any(Consumer.class));
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldReturnNullWhenRetrievingProviderThatDoesNotExist() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
			OAuth2TokenProvider provider = mock(OAuth2TokenProvider.class);
			ScopedResource<OAuth2TokenProvider> resource = mock(ScopedResource.class);
			doReturn(provider).when(resource).unwrap();

			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

			registry.addProvider(PROVIDER_NAME_MY_PROVIDER, resource);
			registry.close();

			OAuth2TokenProvider retrievedProvider = registry.getProvider(UNKNOWN_PROVIDER);

			assertThat(registry.getProviderNames(), hasSize(1));
			assertThat(registry.getProviderNames().getFirst(), equalTo(PROVIDER_NAME_MY_PROVIDER));

			assertThat(registry.getProviders(), hasSize(1));
			assertThat(registry.getProviders().getFirst(), equalTo(provider));

			assertThat(retrievedProvider, equalTo(null));

			verify(resource).closeIfManaged(any(Consumer.class));
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldThrowExceptionWhenRetrievingProviderThatDoesNotExistAndAnExceptionIsSupplied() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
			OAuth2TokenProvider provider = mock(OAuth2TokenProvider.class);
			ScopedResource<OAuth2TokenProvider> resource = mock(ScopedResource.class);
			doReturn(provider).when(resource).unwrap();

			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

			registry.addProvider(PROVIDER_NAME_MY_PROVIDER, resource);
			registry.close();

			Exception exception = new Exception(ERROR_MESSAGE);
			IllegalStateException e = assertThrows(IllegalStateException.class, () -> registry.getProvider(UNKNOWN_PROVIDER, name -> exception));

			assertThat(registry.getProviderNames(), hasSize(1));
			assertThat(registry.getProviderNames().getFirst(), equalTo(PROVIDER_NAME_MY_PROVIDER));

			assertThat(registry.getProviders(), hasSize(1));
			assertThat(registry.getProviders().getFirst(), equalTo(provider));

			assertThat(e.getMessage(), equalTo("No OAuth2 token provider found with name: " + UNKNOWN_PROVIDER));
			assertThat(e.getCause(), equalTo(exception));

			verify(resource).closeIfManaged(any(Consumer.class));
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldRejectDuplicateProvider() {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

			ScopedResource<OAuth2TokenProvider> first = mock(ScopedResource.class);
			ScopedResource<OAuth2TokenProvider> second = mock(ScopedResource.class);

			registry.addProvider(PROVIDER_NAME_MY_PROVIDER, first);

			IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.addProvider(PROVIDER_NAME_MY_PROVIDER, second));

			assertThat(ex.getMessage(), equalTo("An OAuth2 token provider with name '" + PROVIDER_NAME_MY_PROVIDER + "' is already registered."));

			verify(second).closeIfManaged(any(Consumer.class));
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

			registry.addProvider(PROVIDER_NAME_MY_PROVIDER, first);

			IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.addProvider(PROVIDER_NAME_MY_PROVIDER, second));

			assertThat(ex.getMessage(), equalTo("An OAuth2 token provider with name '" + PROVIDER_NAME_MY_PROVIDER + "' is already registered."));

			verify(secondProvider).close();

			first.close();
		}

		@Test
		@SuppressWarnings({ "unchecked", "resource" })
		void shouldRejectAddWhenRegistryClosing() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

			registry.close();

			ScopedResource<OAuth2TokenProvider> resource = mock(ScopedResource.class);

			IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.addProvider(PROVIDER_NAME_MY_PROVIDER, resource));

			assertThat(ex.getMessage(), equalTo("Cannot add new OAuth2 token provider " + PROVIDER_NAME_MY_PROVIDER + " to a closing registry."));

			verify(resource).closeIfManaged(any(Consumer.class));
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

			IllegalStateException ex = assertThrows(IllegalStateException.class, () -> registry.addProvider(PROVIDER_NAME_MY_PROVIDER, resource));

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

			registry.addProvider(PROVIDER_NAME_GOOD, good);
			registry.addProvider(PROVIDER_NAME_BAD, bad);

			registry.close();

			verify(providerGood).close();
			verify(providerBad).close();
		}
	}

	@Nested
	class FactoryMethodsTests {

		@Test
		@SuppressWarnings({ "resource" })
		void shouldCreateProviderRegistryFromRegistry() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);

			OAuth2ResolvedRegistration registration1 = mock(OAuth2ResolvedRegistration.class);
			doReturn(CLIENT_REGISTRATION_1).when(registration1).getClientRegistrationName();
			OAuth2ResolvedRegistration registration2 = mock(OAuth2ResolvedRegistration.class);
			doReturn(CLIENT_REGISTRATION_2).when(registration2).getClientRegistrationName();

			doReturn(List.of(registration1, registration2)).when(mockRegistry).entries();

			OAuth2TokenProvider tokenProvider1 = mock(OAuth2TokenProvider.class);
			doReturn(CLIENT_REGISTRATION_1).when(tokenProvider1).getClientRegistrationName();

			OAuth2TokenProvider tokenProvider2 = mock(OAuth2TokenProvider.class);
			doReturn(CLIENT_REGISTRATION_2).when(tokenProvider2).getClientRegistrationName();

			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

			registry.close();

			assertThat(registry.getProviders(), hasSize(2));
			assertThat(registry.getProviderNames(), hasSize(2));
			assertThat(registry.getProviderNames(), containsInAnyOrder(CLIENT_REGISTRATION_1, CLIENT_REGISTRATION_2));
		}

		@Test
		@SuppressWarnings({ "resource" })
		void shouldCreateRegistryFromTokenSupplierFactories() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);

			OAuth2ResolvedRegistration registration1 = mock(OAuth2ResolvedRegistration.class);
			doReturn(CLIENT_REGISTRATION_1).when(registration1).getClientRegistrationName();
			OAuth2ResolvedRegistration registration2 = mock(OAuth2ResolvedRegistration.class);
			doReturn(CLIENT_REGISTRATION_2).when(registration2).getClientRegistrationName();

			doReturn(List.of(registration1, registration2)).when(mockRegistry).entries();

			OAuth2TokenProvider tokenProvider1 = mock(OAuth2TokenProvider.class);
			doReturn(CLIENT_REGISTRATION_1).when(tokenProvider1).getClientRegistrationName();

			OAuth2TokenProvider tokenProvider2 = mock(OAuth2TokenProvider.class);
			doReturn(CLIENT_REGISTRATION_2).when(tokenProvider2).getClientRegistrationName();

			OAuth2TokenClientSupplier tokenClientSupplier = mock(OAuth2TokenClientSupplier.class);

			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry, tokenClientSupplier);

			registry.close();

			assertThat(registry.getProviders(), hasSize(2));
			assertThat(registry.getProviderNames(), hasSize(2));
			assertThat(registry.getProviderNames(), containsInAnyOrder(CLIENT_REGISTRATION_1, CLIENT_REGISTRATION_2));
		}

		@Test
		void shouldCreateRegistryFromProperties() throws Exception {
			OAuth2Properties mockProperties = mock(OAuth2Properties.class);

			OAuth2ClientRegistration registration1 = mock(OAuth2ClientRegistration.class);
			doReturn(PROVIDER_NAME_1).when(registration1).getProvider();
			doReturn(true).when(registration1).hasClientId();
			doReturn(true).when(registration1).hasClientSecret();
			OAuth2ProviderDetails provider1 = mock(OAuth2ProviderDetails.class);

			OAuth2ClientRegistration registration2 = mock(OAuth2ClientRegistration.class);
			doReturn(PROVIDER_NAME_2).when(registration2).getProvider();
			doReturn(true).when(registration2).hasClientId();
			doReturn(true).when(registration2).hasClientSecret();
			OAuth2ProviderDetails provider2 = mock(OAuth2ProviderDetails.class);

			Map<String, OAuth2ClientRegistration> registrations =
					Map.of(CLIENT_REGISTRATION_1, registration1, CLIENT_REGISTRATION_2, registration2);
			Map<String, OAuth2ProviderDetails> providers =
					Map.of(PROVIDER_NAME_1, provider1, PROVIDER_NAME_2, provider2);

			doReturn(registrations).when(mockProperties).getRegistration();
			doReturn(providers).when(mockProperties).getProvider();
			doReturn(registration1).when(mockProperties).getClientRegistration(CLIENT_REGISTRATION_1);
			doReturn(registration2).when(mockProperties).getClientRegistration(CLIENT_REGISTRATION_2);
			doReturn(provider1).when(mockProperties).getProviderDetails(registration1);
			doReturn(provider2).when(mockProperties).getProviderDetails(registration2);

			AuthenticationToken token = createToken();
			AuthenticationTokenProvider tokenClient = mock(AuthenticationTokenProvider.class);
			doReturn(token).when(tokenClient).getAuthenticationToken();

			OAuth2TokenClientSupplier tokenClientSupplier = (r, d) -> tokenClient;

			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockProperties, tokenClientSupplier);

			registry.close();

			assertThat(registry.getOAuth2Registry().entries(), hasSize(2));
			assertThat(registry.getProviders(), hasSize(2));
			assertThat(registry.getProviderNames(), hasSize(2));
			assertThat(registry.getProviderNames(),
					containsInAnyOrder(CLIENT_REGISTRATION_1, CLIENT_REGISTRATION_2));
		}
	}

	@Nested
	class BuilderTests {

		@Test
		@SuppressWarnings({ "resource" })
		void shouldCreateRegistryFromTokenSupplierFactories() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);

			OAuth2ResolvedRegistration registration1 = mock(OAuth2ResolvedRegistration.class);
			doReturn(CLIENT_REGISTRATION_1).when(registration1).getClientRegistrationName();
			OAuth2ResolvedRegistration registration2 = mock(OAuth2ResolvedRegistration.class);
			doReturn(CLIENT_REGISTRATION_2).when(registration2).getClientRegistrationName();

			doReturn(List.of(registration1, registration2)).when(mockRegistry).entries();

			OAuth2TokenProvider tokenProvider1 = mock(OAuth2TokenProvider.class);
			doReturn(CLIENT_REGISTRATION_1).when(tokenProvider1).getClientRegistrationName();

			OAuth2TokenProvider tokenProvider2 = mock(OAuth2TokenProvider.class);
			doReturn(CLIENT_REGISTRATION_2).when(tokenProvider2).getClientRegistrationName();

			OAuth2TokenClientSupplier tokenClientSupplier = mock(OAuth2TokenClientSupplier.class);

			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.builder()
					.oAuth2Registry(mockRegistry)
					.customizeProviderBuilder(builder -> builder.tokenClientSupplier(tokenClientSupplier))
					.build();

			registry.close();

			assertThat(registry.getProviders(), hasSize(2));
			assertThat(registry.getProviderNames(), hasSize(2));
			assertThat(registry.getProviderNames(), containsInAnyOrder(CLIENT_REGISTRATION_1, CLIENT_REGISTRATION_2));
		}

		@Test
		@SuppressWarnings({ "resource" })
		void shouldCreateRegistryFromTokenSupplierFactoriesWithNameConverters() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);

			OAuth2ResolvedRegistration registration1 = mock(OAuth2ResolvedRegistration.class);
			doReturn(CLIENT_REGISTRATION_1).when(registration1).getClientRegistrationName();
			OAuth2ResolvedRegistration registration2 = mock(OAuth2ResolvedRegistration.class);
			doReturn(CLIENT_REGISTRATION_2).when(registration2).getClientRegistrationName();

			doReturn(List.of(registration1, registration2)).when(mockRegistry).entries();

			OAuth2TokenProvider tokenProvider1 = mock(OAuth2TokenProvider.class);
			doReturn(CLIENT_REGISTRATION_1).when(tokenProvider1).getClientRegistrationName();

			OAuth2TokenProvider tokenProvider2 = mock(OAuth2TokenProvider.class);
			doReturn(CLIENT_REGISTRATION_2).when(tokenProvider2).getClientRegistrationName();

			OAuth2TokenClientSupplier tokenClientSupplier = mock(OAuth2TokenClientSupplier.class);

			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.builder()
					.oAuth2Registry(mockRegistry)
					.customizeProviderBuilder(builder -> builder.tokenClientSupplier(tokenClientSupplier))
					.providerNameConverter(OAuth2TokenProviderRegistryTest::nameConverter)
					.build();

			registry.close();

			assertThat(registry.getProviders(), hasSize(2));
			assertThat(registry.getProviderNames(), hasSize(2));
			assertThat(registry.getProviderNames(),
					containsInAnyOrder(nameConverter(CLIENT_REGISTRATION_1), nameConverter(CLIENT_REGISTRATION_2)));
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateRegistryFromTokenSupplierFactoriesWithCustomizer() throws Exception {
			BiConsumer<String, OAuth2TokenProvider> customizer = mock(BiConsumer.class);

			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);

			OAuth2ResolvedRegistration registration1 = mock(OAuth2ResolvedRegistration.class);
			doReturn(CLIENT_REGISTRATION_1).when(registration1).getClientRegistrationName();
			OAuth2ResolvedRegistration registration2 = mock(OAuth2ResolvedRegistration.class);
			doReturn(CLIENT_REGISTRATION_2).when(registration2).getClientRegistrationName();

			doReturn(List.of(registration1, registration2)).when(mockRegistry).entries();

			OAuth2TokenClientSupplier tokenClientSupplier = mock(OAuth2TokenClientSupplier.class);

			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.builder()
					.oAuth2Registry(mockRegistry)
					.customizeProviderBuilder(builder -> builder.tokenClientSupplier(tokenClientSupplier))
					.providerNameConverter(OAuth2TokenProviderRegistryTest::nameConverter)
					.providerPostConstruct(customizer)
					.build();

			registry.close();

			String expectedName1 = nameConverter(CLIENT_REGISTRATION_1);
			String expectedName2 = nameConverter(CLIENT_REGISTRATION_2);

			assertThat(registry.getProviders(), hasSize(2));
			assertThat(registry.getProviderNames(), hasSize(2));
			assertThat(registry.getProviderNames(),
					containsInAnyOrder(nameConverter(CLIENT_REGISTRATION_1), nameConverter(CLIENT_REGISTRATION_2)));

			verify(customizer).accept(eq(expectedName1), any(OAuth2TokenProvider.class));
			verify(customizer).accept(eq(expectedName2), any(OAuth2TokenProvider.class));
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateRegistryFromTokenSupplierFactoriesWithCustomizerAndNoFilter() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
			OAuth2ResolvedRegistration registration = mock(OAuth2ResolvedRegistration.class);
			doReturn(List.of(registration)).when(mockRegistry).entries();
			doReturn(CLIENT_REGISTRATION_NAME).when(registration).getClientRegistrationName();

			OAuth2TokenClientSupplier tokenClientSupplier = mock(OAuth2TokenClientSupplier.class);
			BiConsumer<String, OAuth2TokenProvider> customizer = mock(BiConsumer.class);

			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.builder()
					.oAuth2Registry(mockRegistry)
					.customizeProviderBuilder(builder -> builder.tokenClientSupplier(tokenClientSupplier))
					.providerNameConverter(OAuth2TokenProviderRegistryTest::nameConverter)
					.providerNameFilter(convertedName -> true)
					.providerPostConstruct(customizer)
					.build();

			registry.close();

			String expectedName = nameConverter(CLIENT_REGISTRATION_NAME);

			assertThat(registry.getProviders(), hasSize(1));
			assertThat(registry.getProviderNames(), hasSize(1));
			assertThat(registry.getProviderNames().getFirst(), equalTo(expectedName));

			verify(mockRegistry).entries();
			verify(customizer).accept(eq(expectedName), any(OAuth2TokenProvider.class));
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

			OAuth2TokenClientSupplier tokenClientSupplier = mock(OAuth2TokenClientSupplier.class);
			BiConsumer<String, OAuth2TokenProvider> customizer = mock(BiConsumer.class);

			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.builder()
					.oAuth2Registry(mockRegistry)
					.customizeProviderBuilder(builder -> builder.tokenClientSupplier(tokenClientSupplier))
					.providerNameConverter(OAuth2TokenProviderRegistryTest::nameConverter)
					.providerNameFilter(convertedName -> !convertedName.equals(nameConverter(CLIENT_REGISTRATION_1)))
					.providerPostConstruct(customizer)
					.build();

			registry.close();

			String expectedRegistration1Name = nameConverter(CLIENT_REGISTRATION_1);
			String expectedRegistration2Name = nameConverter(CLIENT_REGISTRATION_2);

			assertThat(registry.getProviders(), hasSize(1));
			assertThat(registry.getProviderNames(), hasSize(1));
			assertThat(registry.getProviderNames().getFirst(), equalTo(expectedRegistration2Name));

			verify(mockRegistry).entries();

			verify(customizer, never()).accept(eq(expectedRegistration1Name), any(OAuth2TokenProvider.class));
			verify(customizer).accept(eq(expectedRegistration2Name), any(OAuth2TokenProvider.class));
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateRegistryWithClientRegistrationFilterAndNoFilter() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);

			OAuth2ClientRegistration clientRegistration = mock(OAuth2ClientRegistration.class);

			OAuth2ResolvedRegistration registration = mock(OAuth2ResolvedRegistration.class);
			doReturn(List.of(registration)).when(mockRegistry).entries();
			doReturn(CLIENT_REGISTRATION_NAME).when(registration).getClientRegistrationName();
			doReturn(clientRegistration).when(registration).getClientRegistration();

			OAuth2TokenClientSupplier tokenClientSupplier = mock(OAuth2TokenClientSupplier.class);
			BiConsumer<String, OAuth2TokenProvider> customizer = mock(BiConsumer.class);

			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.builder()
					.oAuth2Registry(mockRegistry)
					.customizeProviderBuilder(builder -> builder.tokenClientSupplier(tokenClientSupplier))
					.providerNameConverter(OAuth2TokenProviderRegistryTest::nameConverter)
					.clientRegistrationFilter(cr -> true)
					.providerPostConstruct(customizer)
					.build();

			registry.close();

			String expectedName = nameConverter(CLIENT_REGISTRATION_NAME);

			assertThat(registry.getProviders(), hasSize(1));
			assertThat(registry.getProviderNames(), hasSize(1));
			assertThat(registry.getProviderNames().getFirst(), equalTo(expectedName));

			verify(mockRegistry).entries();
			verify(customizer).accept(eq(expectedName), any(OAuth2TokenProvider.class));
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCreateRegistryWithClientRegistrationFilterAndFilterOutRegistrations() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);

			OAuth2ClientRegistration clientRegistration1 = mock(OAuth2ClientRegistration.class);
			doReturn(PROVIDER_NAME_1).when(clientRegistration1).getProvider();
			OAuth2ClientRegistration clientRegistration2 = mock(OAuth2ClientRegistration.class);
			doReturn(PROVIDER_NAME_2).when(clientRegistration2).getProvider();

			OAuth2ResolvedRegistration registration1 = mock(OAuth2ResolvedRegistration.class);
			doReturn(CLIENT_REGISTRATION_1).when(registration1).getClientRegistrationName();
			doReturn(clientRegistration1).when(registration1).getClientRegistration();

			OAuth2ResolvedRegistration registration2 = mock(OAuth2ResolvedRegistration.class);
			doReturn(CLIENT_REGISTRATION_2).when(registration2).getClientRegistrationName();
			doReturn(clientRegistration2).when(registration2).getClientRegistration();

			doReturn(List.of(registration1, registration2)).when(mockRegistry).entries();

			OAuth2TokenClientSupplier tokenClientSupplier = mock(OAuth2TokenClientSupplier.class);
			BiConsumer<String, OAuth2TokenProvider> customizer = mock(BiConsumer.class);

			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.builder()
					.oAuth2Registry(mockRegistry)
					.customizeProviderBuilder(builder -> builder.tokenClientSupplier(tokenClientSupplier))
					.providerNameConverter(OAuth2TokenProviderRegistryTest::nameConverter)
					.clientRegistrationFilter(cr -> !PROVIDER_NAME_1.equals(cr.getProvider()))
					.providerPostConstruct(customizer)
					.build();

			registry.close();

			String expectedRegistration1Name = nameConverter(CLIENT_REGISTRATION_1);
			String expectedRegistration2Name = nameConverter(CLIENT_REGISTRATION_2);

			assertThat(registry.getProviders(), hasSize(1));
			assertThat(registry.getProviderNames(), hasSize(1));
			assertThat(registry.getProviderNames().getFirst(), equalTo(expectedRegistration2Name));

			verify(mockRegistry).entries();

			verify(customizer, never()).accept(eq(expectedRegistration1Name), any(OAuth2TokenProvider.class));
			verify(customizer).accept(eq(expectedRegistration2Name), any(OAuth2TokenProvider.class));
		}

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldApplyBothClientRegistrationFilterAndProviderNameFilter() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);

			OAuth2ClientRegistration clientRegistration1 = mock(OAuth2ClientRegistration.class);
			doReturn(PROVIDER_NAME_1).when(clientRegistration1).getProvider();
			OAuth2ClientRegistration clientRegistration2 = mock(OAuth2ClientRegistration.class);
			doReturn(PROVIDER_NAME_2).when(clientRegistration2).getProvider();
			OAuth2ClientRegistration clientRegistration3 = mock(OAuth2ClientRegistration.class);
			doReturn(PROVIDER_NAME_3).when(clientRegistration3).getProvider();

			OAuth2ResolvedRegistration registration1 = mock(OAuth2ResolvedRegistration.class);
			doReturn(CLIENT_REGISTRATION_1).when(registration1).getClientRegistrationName();
			doReturn(clientRegistration1).when(registration1).getClientRegistration();

			OAuth2ResolvedRegistration registration2 = mock(OAuth2ResolvedRegistration.class);
			doReturn(CLIENT_REGISTRATION_2).when(registration2).getClientRegistrationName();
			doReturn(clientRegistration2).when(registration2).getClientRegistration();

			OAuth2ResolvedRegistration registration3 = mock(OAuth2ResolvedRegistration.class);
			doReturn(CLIENT_REGISTRATION_3).when(registration3).getClientRegistrationName();
			doReturn(clientRegistration3).when(registration3).getClientRegistration();

			doReturn(List.of(registration1, registration2, registration3)).when(mockRegistry).entries();

			OAuth2TokenClientSupplier tokenClientSupplier = mock(OAuth2TokenClientSupplier.class);
			BiConsumer<String, OAuth2TokenProvider> customizer = mock(BiConsumer.class);

			// registration1 is dropped by the client registration filter, before the provider name is even computed,
			// registration2 passes that filter but is dropped by the provider name filter, and registration3 passes both
			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.builder()
					.oAuth2Registry(mockRegistry)
					.customizeProviderBuilder(builder -> builder.tokenClientSupplier(tokenClientSupplier))
					.providerNameConverter(OAuth2TokenProviderRegistryTest::nameConverter)
					.clientRegistrationFilter(cr -> !PROVIDER_NAME_1.equals(cr.getProvider()))
					.providerNameFilter(name -> !nameConverter(CLIENT_REGISTRATION_2).equals(name))
					.providerPostConstruct(customizer)
					.build();

			registry.close();

			String expectedName1 = nameConverter(CLIENT_REGISTRATION_1);
			String expectedName2 = nameConverter(CLIENT_REGISTRATION_2);
			String expectedName3 = nameConverter(CLIENT_REGISTRATION_3);

			assertThat(registry.getProviders(), hasSize(1));
			assertThat(registry.getProviderNames(), hasSize(1));
			assertThat(registry.getProviderNames().getFirst(), equalTo(expectedName3));

			verify(mockRegistry).entries();

			verify(customizer, never()).accept(eq(expectedName1), any(OAuth2TokenProvider.class));
			verify(customizer, never()).accept(eq(expectedName2), any(OAuth2TokenProvider.class));
			verify(customizer).accept(eq(expectedName3), any(OAuth2TokenProvider.class));
		}

		@Test
		void shouldCreateRegistryFromProperties() throws Exception {
			OAuth2Properties mockProperties = mock(OAuth2Properties.class);

			OAuth2ClientRegistration registration1 = mock(OAuth2ClientRegistration.class);
			doReturn(PROVIDER_NAME_1).when(registration1).getProvider();
			doReturn(true).when(registration1).hasClientId();
			doReturn(true).when(registration1).hasClientSecret();
			OAuth2ProviderDetails provider1 = mock(OAuth2ProviderDetails.class);

			OAuth2ClientRegistration registration2 = mock(OAuth2ClientRegistration.class);
			doReturn(PROVIDER_NAME_2).when(registration2).getProvider();
			doReturn(true).when(registration2).hasClientId();
			doReturn(true).when(registration2).hasClientSecret();
			OAuth2ProviderDetails provider2 = mock(OAuth2ProviderDetails.class);

			Map<String, OAuth2ClientRegistration> registrations =
					Map.of(CLIENT_REGISTRATION_1, registration1, CLIENT_REGISTRATION_2, registration2);
			Map<String, OAuth2ProviderDetails> providers =
					Map.of(PROVIDER_NAME_1, provider1, PROVIDER_NAME_2, provider2);

			doReturn(registrations).when(mockProperties).getRegistration();
			doReturn(providers).when(mockProperties).getProvider();
			doReturn(registration1).when(mockProperties).getClientRegistration(CLIENT_REGISTRATION_1);
			doReturn(registration2).when(mockProperties).getClientRegistration(CLIENT_REGISTRATION_2);
			doReturn(provider1).when(mockProperties).getProviderDetails(registration1);
			doReturn(provider2).when(mockProperties).getProviderDetails(registration2);

			AuthenticationToken token = createToken();
			AuthenticationTokenProvider tokenClient = mock(AuthenticationTokenProvider.class);
			doReturn(token).when(tokenClient).getAuthenticationToken();

			OAuth2TokenClientSupplier tokenClientSupplier = (r, d) -> tokenClient;

			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.builder()
					.oAuth2Properties(mockProperties)
					.customizeProviderBuilder(builder -> builder.tokenClientSupplier(tokenClientSupplier))
					.providerNameConverter(OAuth2TokenProviderRegistryTest::nameConverter)
					.build();

			registry.close();

			assertThat(registry.getOAuth2Registry().entries(), hasSize(2));
			assertThat(registry.getProviders(), hasSize(2));
			assertThat(registry.getProviderNames(), hasSize(2));
			assertThat(registry.getProviderNames(),
					containsInAnyOrder(nameConverter(CLIENT_REGISTRATION_1), nameConverter(CLIENT_REGISTRATION_2)));
		}

		@Test
		void shouldCreateRegistryFromPropertiesWithoutNameConverter() throws Exception {
			OAuth2Properties mockProperties = mock(OAuth2Properties.class);

			OAuth2ClientRegistration registration1 = mock(OAuth2ClientRegistration.class);
			doReturn(PROVIDER_NAME_1).when(registration1).getProvider();
			doReturn(true).when(registration1).hasClientId();
			doReturn(true).when(registration1).hasClientSecret();
			OAuth2ProviderDetails provider1 = mock(OAuth2ProviderDetails.class);

			OAuth2ClientRegistration registration2 = mock(OAuth2ClientRegistration.class);
			doReturn(PROVIDER_NAME_2).when(registration2).getProvider();
			doReturn(true).when(registration2).hasClientId();
			doReturn(true).when(registration2).hasClientSecret();
			OAuth2ProviderDetails provider2 = mock(OAuth2ProviderDetails.class);

			Map<String, OAuth2ClientRegistration> registrations =
					Map.of(CLIENT_REGISTRATION_1, registration1, CLIENT_REGISTRATION_2, registration2);
			Map<String, OAuth2ProviderDetails> providers =
					Map.of(PROVIDER_NAME_1, provider1, PROVIDER_NAME_2, provider2);

			doReturn(registrations).when(mockProperties).getRegistration();
			doReturn(providers).when(mockProperties).getProvider();
			doReturn(registration1).when(mockProperties).getClientRegistration(CLIENT_REGISTRATION_1);
			doReturn(registration2).when(mockProperties).getClientRegistration(CLIENT_REGISTRATION_2);
			doReturn(provider1).when(mockProperties).getProviderDetails(registration1);
			doReturn(provider2).when(mockProperties).getProviderDetails(registration2);

			AuthenticationToken token = createToken();
			AuthenticationTokenProvider tokenClient = mock(AuthenticationTokenProvider.class);
			doReturn(token).when(tokenClient).getAuthenticationToken();

			OAuth2TokenClientSupplier tokenClientSupplier = (r, d) -> tokenClient;

			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.builder()
					.oAuth2Properties(mockProperties)
					.customizeProviderBuilder(builder -> builder.tokenClientSupplier(tokenClientSupplier))
					.build();

			registry.close();

			assertThat(registry.getOAuth2Registry().entries(), hasSize(2));
			assertThat(registry.getProviders(), hasSize(2));
			assertThat(registry.getProviderNames(), hasSize(2));
			assertThat(registry.getProviderNames(),
					containsInAnyOrder(CLIENT_REGISTRATION_1, CLIENT_REGISTRATION_2));
		}
	}

	@Nested
	class CloseTests {

		@Test
		@SuppressWarnings({ "resource", "unchecked" })
		void shouldCloseAllProvidersWhenRegistryIsClosed() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

			ScopedResource<OAuth2TokenProvider> provider1 = mock(ScopedResource.class);
			doReturn(mock(OAuth2TokenProvider.class)).when(provider1).unwrap();

			ScopedResource<OAuth2TokenProvider> provider2 = mock(ScopedResource.class);
			doReturn(mock(OAuth2TokenProvider.class)).when(provider2).unwrap();

			registry.addProvider(PROVIDER_NAME_1, provider1);
			registry.addProvider(PROVIDER_NAME_2, provider2);

			registry.close();

			verify(provider1).closeIfManaged(any(Consumer.class));
			verify(provider2).closeIfManaged(any(Consumer.class));
		}

		@Test
		@SuppressWarnings({ "unchecked", "resource" })
		void shouldBeThreadSafeForConcurrentAddAndClose() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

			ExecutorService executor = Executors.newFixedThreadPool(THREADS);

			List<ScopedResource<OAuth2TokenProvider>> resources = new CopyOnWriteArrayList<>();

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

						registry.addProvider(PROVIDER_NAME_MY_PROVIDER, resource);
					} catch (Exception ignored) {
						// expected exception when duplicate or closing
						LOGGER.info("Expected: {}: {}", ignored.getClass().getName(), ignored.getMessage());
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
					verify(resource, atLeastOnce()).closeIfManaged(any(Consumer.class));
				}
			}
		}

		@Test
		@SuppressWarnings({ "unchecked", "resource" })
		void shouldCloseProviderWhenCloseIsCalledWhileTheProviderIsBeingAdded() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

			CountDownLatch addingGate = new CountDownLatch(1);
			CountDownLatch continueAddGate = new CountDownLatch(1);

			// pauses the adding thread right after it checked that the registry is not closing
			Map<String, ScopedResource<OAuth2TokenProvider>> pausingProviders = new ConcurrentHashMap<>() {
				@Override
				public ScopedResource<OAuth2TokenProvider> putIfAbsent(final String key, final ScopedResource<OAuth2TokenProvider> value) {
					addingGate.countDown();
					Threads.safeWait(continueAddGate, Duration.ofSeconds(WAIT_SECONDS));
					return super.putIfAbsent(key, value);
				}
			};
			Fields.IgnoreAccess.set(registry, "providers", pausingProviders);

			ScopedResource<OAuth2TokenProvider> tokenProvider = mock(ScopedResource.class);
			doReturn(mock(OAuth2TokenProvider.class)).when(tokenProvider).unwrap();

			Thread addThread = Thread.ofPlatform()
					.start(() -> registry.addProvider(PROVIDER_NAME_MY_PROVIDER, tokenProvider));
			addingGate.await(WAIT_SECONDS, TimeUnit.SECONDS);

			Thread closeThread = Thread.ofPlatform()
					.start(ThrowingRunnable.unchecked(() -> registry.close()));
			// wait for close to finish iterating over an empty map before the provider is put in the map
			closeThread.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));
			continueAddGate.countDown();

			addThread.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));
			closeThread.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));

			assertThat(registry.getProviderNames(), contains(PROVIDER_NAME_MY_PROVIDER));
			verify(tokenProvider).closeIfManaged(any(Consumer.class));
		}

		@Test
		@SuppressWarnings({ "unchecked", "resource" })
		void shouldNotCallCloseOnAllProvidersEvenIfRegistryCloseIsCalledMultipleTimes() throws Exception {
			OAuth2Registry mockRegistry = mock(OAuth2Registry.class);
			OAuth2TokenProviderRegistry registry = OAuth2TokenProviderRegistry.of(mockRegistry);

			ScopedResource<OAuth2TokenProvider> tokenProvider = mock(ScopedResource.class);

			registry.addProvider(PROVIDER_NAME_MY_PROVIDER, tokenProvider);

			for (int i = 0; i < CLOSE_COUNT; ++i) {
				registry.close();
			}

			verify(tokenProvider).closeIfManaged(any(Consumer.class));
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

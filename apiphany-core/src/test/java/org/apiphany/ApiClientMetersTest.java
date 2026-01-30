package org.apiphany;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Duration;
import java.util.List;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.lang.retry.Retry;
import org.apiphany.lang.retry.WaitCounter;
import org.apiphany.meters.BasicMeters;
import org.apiphany.meters.MeterCounter;
import org.apiphany.meters.MeterFactory;
import org.apiphany.meters.MeterTimer;
import org.apiphany.security.AuthenticationType;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Tags;

/**
 * Test class for {@link ApiClient} meters.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientMetersTest {

	private static final String BASE_URL = "http://localhost";
	private static final String PATH_TEST = "test";

	private static final String METRICS_PREFIX = "test.metrics";
	private static final int RETRY_COUNT = 3;

	private static final String SOME_ERROR_MESSAGE = "someErrorMessage";

	@Test
	@SuppressWarnings("resource")
	void shouldSetMetricsToThisMethod() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		ApiClientFluentAdapter adapter = api
				.client()
				.http()
				.get()
				.path(PATH_TEST)
				.metersOnMethod(METRICS_PREFIX);

		String metricStart = METRICS_PREFIX + ".should-set-metrics-to-this-method.";

		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));
		assertThat(adapter.getMeters().requests().getName(), equalTo(metricStart + BasicMeters.Name.REQUEST));
		assertThat(adapter.getMeters().errors().getName(), equalTo(metricStart + BasicMeters.Name.ERROR));
		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldSetMetricsToThisMethodWithTags() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		ApiClientFluentAdapter adapter = api
				.client()
				.http()
				.get()
				.path(PATH_TEST)
				.metersOnMethod(METRICS_PREFIX, Tags.empty());

		String metricStart = METRICS_PREFIX + ".should-set-metrics-to-this-method-with-tags.";

		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));
		assertThat(adapter.getMeters().requests().getName(), equalTo(metricStart + BasicMeters.Name.REQUEST));
		assertThat(adapter.getMeters().errors().getName(), equalTo(metricStart + BasicMeters.Name.ERROR));
		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldSetMetricsWithoutMethod() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		ApiClientFluentAdapter adapter = api
				.client()
				.http()
				.get()
				.path(PATH_TEST)
				.meters(METRICS_PREFIX);

		String metricStart = METRICS_PREFIX + ".";

		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));
		assertThat(adapter.getMeters().requests().getName(), equalTo(metricStart + BasicMeters.Name.REQUEST));
		assertThat(adapter.getMeters().errors().getName(), equalTo(metricStart + BasicMeters.Name.ERROR));
		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldUseDefaultMetricsIfNoMetricsAreSetAndMetricsAreDisabled() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setMetricsEnabled(false);

		ApiClientFluentAdapter adapter = api
				.client()
				.http()
				.get()
				.path(PATH_TEST);

		assertThat(adapter.getMeters(), nullValue());

		assertThat(api.getActiveMeters(adapter).latency().getName(), equalTo(BasicMeters.Name.LATENCY));
		assertThat(api.getActiveMeters(adapter).requests().getName(), equalTo(BasicMeters.Name.REQUEST));
		assertThat(api.getActiveMeters(adapter).errors().getName(), equalTo(BasicMeters.Name.ERROR));
		assertThat(api.getActiveMeters(adapter).latency().getName(), equalTo(BasicMeters.Name.LATENCY));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldUseDefaultMetricsEvenIfNoMetricsAreSetButMetricsAreDisabled() {
		HttpExchangeClient exchangeClient = mock(HttpExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setMetricsEnabled(false);

		ApiClientFluentAdapter adapter = api
				.client()
				.http()
				.get()
				.path(PATH_TEST)
				.meters(METRICS_PREFIX);

		String metricStart = METRICS_PREFIX + ".";

		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));
		assertThat(adapter.getMeters().requests().getName(), equalTo(metricStart + BasicMeters.Name.REQUEST));
		assertThat(adapter.getMeters().errors().getName(), equalTo(metricStart + BasicMeters.Name.ERROR));
		assertThat(adapter.getMeters().latency().getName(), equalTo(metricStart + BasicMeters.Name.LATENCY));

		assertThat(api.getActiveMeters(adapter).latency().getName(), equalTo(BasicMeters.Name.LATENCY));
		assertThat(api.getActiveMeters(adapter).requests().getName(), equalTo(BasicMeters.Name.REQUEST));
		assertThat(api.getActiveMeters(adapter).errors().getName(), equalTo(BasicMeters.Name.ERROR));
		assertThat(api.getActiveMeters(adapter).latency().getName(), equalTo(BasicMeters.Name.LATENCY));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldSetTheMeters() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();
		BasicMeters basicMeters = mock(BasicMeters.class);
		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setMeters(basicMeters);

		BasicMeters result = api.getMeters();

		assertThat(result, sameInstance(basicMeters));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldSetTheMeterRegistry() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();
		MeterFactory meterFactory = mock(MeterFactory.class);
		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setMeterFactory(meterFactory);

		MeterFactory result = api.getMeterFactory();

		assertThat(result, sameInstance(meterFactory));
	}

	@Test
	@SuppressWarnings({ "unchecked", "resource" })
	void shouldSetMetricsOnExchangeWhenThereAreNoExceptions() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		MeterFactory meterFactory = mock(MeterFactory.class);
		MeterTimer latency = mock(MeterTimer.class);
		MeterCounter requests = mock(MeterCounter.class);
		MeterCounter errors = mock(MeterCounter.class);
		MeterCounter retries = mock(MeterCounter.class);
		doReturn(latency).when(meterFactory).timer(eq(METRICS_PREFIX), eq(BasicMeters.Name.LATENCY), any(List.class));
		doReturn(requests).when(meterFactory).counter(eq(METRICS_PREFIX), eq(BasicMeters.Name.REQUEST), any(List.class));
		doReturn(errors).when(meterFactory).counter(eq(METRICS_PREFIX), eq(BasicMeters.Name.ERROR), any(List.class));
		doReturn(retries).when(meterFactory).counter(eq(METRICS_PREFIX), eq(BasicMeters.Name.RETRY), any(List.class));

		BasicMeters meters = BasicMeters.of(meterFactory, METRICS_PREFIX);

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setMetricsEnabled(true);
		api.setMeters(meters);

		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofMillis(10)));
		api.setRetry(retry);

		ApiRequest<?> request = mock(ApiRequest.class);
		doReturn(AuthenticationType.OAUTH2).when(request).getAuthenticationType();

		ApiResponse<?> response = mock(ApiResponse.class);
		doReturn(false).when(response).isSuccessful();
		doReturn(response).when(exchangeClient).exchange(request);

		ApiResponse<?> result = api.exchange(request);

		assertThat(result, sameInstance(response));

		verify(retries, times(RETRY_COUNT)).increment();
		verify(requests, times(RETRY_COUNT)).increment();
		verify(latency, times(RETRY_COUNT)).record(any(Duration.class));
		verifyNoInteractions(errors);
	}

	@Test
	@SuppressWarnings({ "unchecked", "resource" })
	void shouldSetMetricsOnExchangeWhenThereAreExceptions() {
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(AuthenticationType.OAUTH2).when(exchangeClient).getAuthenticationType();

		MeterFactory meterFactory = mock(MeterFactory.class);
		MeterTimer latency = mock(MeterTimer.class);
		MeterCounter requests = mock(MeterCounter.class);
		MeterCounter errors = mock(MeterCounter.class);
		MeterCounter retries = mock(MeterCounter.class);
		doReturn(latency).when(meterFactory).timer(eq(METRICS_PREFIX), eq(BasicMeters.Name.LATENCY), any(List.class));
		doReturn(requests).when(meterFactory).counter(eq(METRICS_PREFIX), eq(BasicMeters.Name.REQUEST), any(List.class));
		doReturn(errors).when(meterFactory).counter(eq(METRICS_PREFIX), eq(BasicMeters.Name.ERROR), any(List.class));
		doReturn(retries).when(meterFactory).counter(eq(METRICS_PREFIX), eq(BasicMeters.Name.RETRY), any(List.class));

		BasicMeters meters = BasicMeters.of(meterFactory, METRICS_PREFIX);

		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);
		api.setMetricsEnabled(true);
		api.setMeters(meters);

		Retry retry = Retry.of(WaitCounter.of(RETRY_COUNT, Duration.ofMillis(10)));
		api.setRetry(retry);

		ApiRequest<?> request = mock(ApiRequest.class);
		doReturn(AuthenticationType.OAUTH2).when(request).getAuthenticationType();

		RuntimeException exception = new RuntimeException(SOME_ERROR_MESSAGE);
		doThrow(exception).when(exchangeClient).exchange(request);

		ApiResponse<?> result = api.exchange(request);

		assertThat(result.getException(), sameInstance(exception));

		verify(retries, times(RETRY_COUNT)).increment();
		verify(requests, times(RETRY_COUNT)).increment();
		verify(latency, times(RETRY_COUNT)).record(any(Duration.class));
		verify(errors, times(RETRY_COUNT)).increment();
	}
}

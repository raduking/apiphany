package org.apiphany.meters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.core5.pool.ConnPoolControl;
import org.apache.hc.core5.pool.PoolStats;
import org.apiphany.meters.ConnectionManagerMetricsBinder.MetricPrefix;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Test class for {@link ConnectionManagerMetricsBinder}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConnectionManagerMetricsBinderTest {

	/**
	 * The client name used in the tests.
	 */
	private static final String CLIENT_NAME = "test-client";

	/**
	 * The metric name prefix for the test client.
	 */
	private static final String METRIC_PREFIX = MetricPrefix.HTTP_CLIENT + "." + CLIENT_NAME;

	@Test
	void shouldExposeTheMetricPrefixes() {
		assertThat(MetricPrefix.HTTP_CLIENT, equalTo("httpcomponents.httpclient"));
		assertThat(MetricPrefix.POOL_TOTAL, equalTo("pool.total"));
		assertThat(MetricPrefix.POOL_ROUTE, equalTo("pool.route"));
	}

	@Test
	void shouldReturnAMeterBinder() {
		ConnectionManagerMetricsBinder binder = ConnectionManagerMetricsBinder.of(connPoolControl(), CLIENT_NAME);

		assertInstanceOf(MeterBinder.class, binder);
	}

	@Test
	void shouldRegisterAllTotalPoolMetricsWithTheExpectedNamesAndValues() {
		ConnPoolControl<HttpRoute> connPoolControl = connPoolControl();
		SimpleMeterRegistry registry = new SimpleMeterRegistry();

		ConnectionManagerMetricsBinder.of(connPoolControl, CLIENT_NAME).bindTo(registry);

		assertGaugeValue(registry, MetricPrefix.POOL_TOTAL + ".max", 20.0);
		assertGaugeValue(registry, MetricPrefix.POOL_TOTAL + ".connections.available", 5.0);
		assertGaugeValue(registry, MetricPrefix.POOL_TOTAL + ".connections.leased", 3.0);
		assertGaugeValue(registry, MetricPrefix.POOL_TOTAL + ".pending", 2.0);
		assertGaugeValue(registry, MetricPrefix.POOL_ROUTE + ".max.default", 10.0);
	}

	@Test
	void shouldRegisterTheMetricsUnderThePoolTotalAndPoolRoutePrefixes() {
		SimpleMeterRegistry registry = new SimpleMeterRegistry();

		ConnectionManagerMetricsBinder.of(connPoolControl(), CLIENT_NAME).bindTo(registry);

		assertThat(registry.find(METRIC_PREFIX + "." + MetricPrefix.POOL_TOTAL + ".max").gauge(), notNullValue());
		assertThat(registry.find(METRIC_PREFIX + "." + MetricPrefix.POOL_ROUTE + ".max.default").gauge(), notNullValue());
	}

	@Test
	void shouldReadTheValuesFromTheConnectionPoolControlOnEachRead() {
		ConnPoolControl<HttpRoute> connPoolControl = connPoolControl();
		SimpleMeterRegistry registry = new SimpleMeterRegistry();
		ConnectionManagerMetricsBinder.of(connPoolControl, CLIENT_NAME).bindTo(registry);

		doReturn(new PoolStats(7, 6, 8, 30)).when(connPoolControl).getTotalStats();
		doReturn(15).when(connPoolControl).getDefaultMaxPerRoute();

		assertGaugeValue(registry, MetricPrefix.POOL_TOTAL + ".max", 30.0);
		assertGaugeValue(registry, MetricPrefix.POOL_TOTAL + ".connections.available", 8.0);
		assertGaugeValue(registry, MetricPrefix.POOL_TOTAL + ".connections.leased", 7.0);
		assertGaugeValue(registry, MetricPrefix.POOL_TOTAL + ".pending", 6.0);
		assertGaugeValue(registry, MetricPrefix.POOL_ROUTE + ".max.default", 15.0);
	}

	@Test
	void shouldAddTheGivenTagsToAllMetrics() {
		SimpleMeterRegistry registry = new SimpleMeterRegistry();

		ConnectionManagerMetricsBinder.of(connPoolControl(), CLIENT_NAME, "client", "acme", "environment", "test")
				.bindTo(registry);

		assertThat(registry.get(METRIC_PREFIX + "." + MetricPrefix.POOL_TOTAL + ".max")
				.tags("client", "acme", "environment", "test")
				.gauge(), notNullValue());
		assertThat(registry.get(METRIC_PREFIX + "." + MetricPrefix.POOL_TOTAL + ".pending")
				.tags("client", "acme", "environment", "test")
				.gauge(), notNullValue());
		assertThat(registry.get(METRIC_PREFIX + "." + MetricPrefix.POOL_ROUTE + ".max.default")
				.tags("client", "acme", "environment", "test")
				.gauge()
				.getId()
				.getTag("client"), equalTo("acme"));
	}

	@Test
	void shouldNotAddAnyTagWhenNoTagIsGiven() {
		SimpleMeterRegistry registry = new SimpleMeterRegistry();

		ConnectionManagerMetricsBinder.of(connPoolControl(), CLIENT_NAME).bindTo(registry);

		Gauge gauge = registry.get(METRIC_PREFIX + "." + MetricPrefix.POOL_TOTAL + ".max").gauge();

		assertThat(gauge.getId().getTags().isEmpty(), equalTo(true));
	}

	/**
	 * Asserts the value of the gauge with the given suffix.
	 *
	 * @param registry the meter registry
	 * @param suffix the metric name suffix after {@code httpcomponents.httpclient.${clientName}.}
	 * @param expected the expected gauge value
	 */
	private static void assertGaugeValue(final SimpleMeterRegistry registry, final String suffix, final double expected) {
		assertThat(registry.get(METRIC_PREFIX + "." + suffix).gauge().value(), equalTo(expected));
	}

	/**
	 * Creates a mocked connection pool control with the default total statistics and default max per route.
	 *
	 * @return a mocked connection pool control
	 */
	private static ConnPoolControl<HttpRoute> connPoolControl() {
		@SuppressWarnings("unchecked")
		ConnPoolControl<HttpRoute> connPoolControl = mock(ConnPoolControl.class);
		doReturn(new PoolStats(3, 2, 5, 20)).when(connPoolControl).getTotalStats();
		doReturn(10).when(connPoolControl).getDefaultMaxPerRoute();
		return connPoolControl;
	}
}

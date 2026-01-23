package org.apiphany.meters;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.core5.pool.ConnPoolControl;
import org.apiphany.lang.builder.PropertyNameBuilder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.httpcomponents.hc5.PoolingHttpClientConnectionManagerMetricsBinder;

/**
 * Utility methods to register connection manager metrics. This is similar to
 * {@link PoolingHttpClientConnectionManagerMetricsBinder} but it also adds the client name.
 * <p>
 * The following metrics will be published:
 *
 * <pre>
 * 	httpcomponents.httpclient.${clientName}.pool.total.max
 * 	httpcomponents.httpclient.${clientName}.pool.total.connections.available
 * 	httpcomponents.httpclient.${clientName}.pool.total.connections.leased
 * 	httpcomponents.httpclient.${clientName}.pool.total.pending
 * 	httpcomponents.httpclient.${clientName}.pool.route.max.default
 * </pre>
 *
 * where {@code ${clientName}} is the parameter given when constructing the binder.
 *
 * @author Radu Sebastian LAZIN
 */
public class ConnectionManagerMetricsBinder implements MeterBinder {

	/**
	 * The base metric prefix for all HTTP client metrics.
	 */
	public static final String METRIC_HTTP_CLIENT_PREFIX = "httpcomponents.httpclient";

	/**
	 * The metric prefix for total pool statistics.
	 */
	public static final String METRIC_POOL_TOTAL_PREFIX = "pool.total";

	/**
	 * The metric prefix for route-specific pool statistics.
	 */
	public static final String METRIC_POOL_ROUTE_PREFIX = "pool.route";

	/**
	 * The connection pool control instance being monitored.
	 */
	private final ConnPoolControl<HttpRoute> connPoolControl;

	/**
	 * Tags for metrics.
	 */
	private final Iterable<Tag> tags;

	/**
	 * The name of the HTTP client being monitored.
	 */
	private final String clientName;

	/**
	 * Constructs a new metrics binder for the given connection pool control.
	 *
	 * @param connPoolControl the connection pool control to monitor
	 * @param clientName the name of the HTTP client (used in metric names)
	 */
	private ConnectionManagerMetricsBinder(final ConnPoolControl<HttpRoute> connPoolControl, final String clientName, final String... tags) {
		this.connPoolControl = connPoolControl;
		this.clientName = clientName;
		this.tags = Tags.of(tags);
	}

	/**
	 * Creates a new metrics binder for the given connection pool control.
	 *
	 * @param connPoolControl the connection pool control to monitor
	 * @param clientName the name of the HTTP client (used in metric names)
	 * @param tags metric tags
	 * @return a new ConnectionManagerMetricsBinder instance
	 */
	public static ConnectionManagerMetricsBinder of(final ConnPoolControl<HttpRoute> connPoolControl, final String clientName, final String... tags) {
		return new ConnectionManagerMetricsBinder(connPoolControl, clientName, tags);
	}

	/**
	 * Binds the metrics to the specified meter registry.
	 *
	 * @param registry the meter registry to bind metrics to
	 */
	@Override
	public void bindTo(final MeterRegistry registry) {
		registerTotalMetrics(registry);
	}

	/**
	 * Registers all total pool metrics with the meter registry.
	 *
	 * @param registry the meter registry to register metrics with
	 */
	private void registerTotalMetrics(final MeterRegistry registry) {
		// httpcomponents.httpclient.${clientName}.pool.total.max
		Gauge.builder(metricName(METRIC_HTTP_CLIENT_PREFIX, clientName, METRIC_POOL_TOTAL_PREFIX, "max"), connPoolControl,
				cpc -> cpc.getTotalStats().getMax())
				.description("The configured maximum number of allowed persistent connections for all routes.")
				.tags(tags)
				.register(registry);

		// httpcomponents.httpclient.${clientName}.pool.total.connections.available
		Gauge.builder(metricName(METRIC_HTTP_CLIENT_PREFIX, clientName, METRIC_POOL_TOTAL_PREFIX, "connections", "available"), connPoolControl,
				cpc -> cpc.getTotalStats().getAvailable())
				.description("The number of idle persistent connections for all routes.")
				.tags(tags)
				.register(registry);

		// httpcomponents.httpclient.${clientName}.pool.total.connections.leased
		Gauge.builder(metricName(METRIC_HTTP_CLIENT_PREFIX, clientName, METRIC_POOL_TOTAL_PREFIX, "connections", "leased"), connPoolControl,
				cpc -> cpc.getTotalStats().getLeased())
				.description(
						"The number of persistent connections tracked by the connection manager currently being used to execute requests for all routes.")
				.tags(tags)
				.register(registry);

		// httpcomponents.httpclient.${clientName}.pool.total.pending
		Gauge.builder(metricName(METRIC_HTTP_CLIENT_PREFIX, clientName, METRIC_POOL_TOTAL_PREFIX, "pending"), connPoolControl,
				cpc -> cpc.getTotalStats().getPending())
				.description("The number of connection requests being blocked awaiting a free connection for all routes.")
				.tags(tags)
				.register(registry);

		// httpcomponents.httpclient.${clientName}.pool.route.max.default
		Gauge.builder(metricName(METRIC_HTTP_CLIENT_PREFIX, clientName, METRIC_POOL_ROUTE_PREFIX, "max", "default"), connPoolControl,
				ConnPoolControl::getDefaultMaxPerRoute)
				.description("The configured default maximum number of allowed persistent connections per route.")
				.tags(tags)
				.register(registry);
	}

	/**
	 * Builds a metric name from the given path components.
	 *
	 * @param paths the components of the metric name
	 * @return the constructed metric name //
	 */
	private static String metricName(final String... paths) {
		return PropertyNameBuilder.builder()
				.path(paths)
				.build();
	}

}

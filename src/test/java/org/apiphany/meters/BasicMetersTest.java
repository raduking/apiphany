package org.apiphany.meters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.function.Supplier;

import org.apiphany.lang.builder.PropertyNameBuilder;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Methods;

import io.micrometer.core.instrument.Tags;

/**
 * Test class for {@link BasicMeters}.
 *
 * @author Radu Sebastian LAZIN
 */
class BasicMetersTest {

	private static final String METRICS_PREFIX = "test.basic.metrics";

	@Test
	void shouldSetMetricsToThisMethod() {
		BasicMeters meters = BasicMeters.onMethod(METRICS_PREFIX);

		assertThat(meters.latency().getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-this-method." + BasicMeters.LATENCY_METRIC));
	}

	@Test
	void shouldSetMetricsToCallerMethod() {
		BasicMeters meters = myUtilityMethod(METRICS_PREFIX);

		assertThat(meters.latency().getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-caller-method." + BasicMeters.LATENCY_METRIC));
	}

	@Test
	void shouldSetMetricsToThisMethodWithTags() {
		BasicMeters meters = BasicMeters.onMethod(METRICS_PREFIX, Tags.empty());

		assertThat(meters.latency().getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-this-method-with-tags." + BasicMeters.LATENCY_METRIC));
	}

	@Test
	void shouldSetMetricsToCallerMethodWithTags() {
		BasicMeters meters = myUtilityMethod(METRICS_PREFIX, Tags.empty());

		assertThat(meters.latency().getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-caller-method-with-tags." + BasicMeters.LATENCY_METRIC));
	}

	@Test
	void shouldSetMetricsToOfMethodWhenDepthIsTwo() {
		BasicMeters meters = BasicMeters.onMethod(METRICS_PREFIX, 2);

		assertThat(meters.latency().getId().getName(),
				equalTo(METRICS_PREFIX + ".on-method." + BasicMeters.LATENCY_METRIC));
	}

	@Test
	void shouldNotSetMetricsToThisMethodWhenDepthIsFour() {
		BasicMeters metersDepth3 = BasicMeters.onMethod(METRICS_PREFIX);
		BasicMeters metersDepth4 = BasicMeters.onMethod(METRICS_PREFIX, 4);

		assertThat(metersDepth3.latency().getId().getName(),
				equalTo(METRICS_PREFIX + ".should-not-set-metrics-to-this-method-when-depth-is-four." + BasicMeters.LATENCY_METRIC));
		assertThat(metersDepth4.latency().getId().getName(),
				not(equalTo(metersDepth3.latency().getId().getName())));
	}

	@Test
	void shouldReturnCallerMethodName() {
		String methodName = methodName(this::foo);

		assertThat(methodName, equalTo(".BasicMetersTest.shouldReturnCallerMethodName"));
	}

	@Test
	void shouldReturnCallerMethodNameFromSupplier() {
		String methodName = methodNameSupplier(this::foo).get();

		assertThat(methodName, equalTo(".BasicMetersTest.shouldReturnCallerMethodNameFromSupplier"));
	}

	private Integer foo() {
		return 0;
	}

	private static BasicMeters myUtilityMethod(final String prefix) {
		return BasicMeters.onCallerMethod(prefix);
	}

	private static BasicMeters myUtilityMethod(final String prefix, final Tags tags) {
		return BasicMeters.onCallerMethod(prefix, tags);
	}

	private static <T> Supplier<String> methodNameSupplier(final Supplier<T> statements) {
		return () -> Methods.getCallerMethodName(statements, 2, BasicMeters::toFullMethodName)
				.map(callerMethodName -> PropertyNameBuilder.builder()
						.asSuffix()
						.path(callerMethodName)
						.build())
				.orElseThrow();
	}

	private static <T> String methodName(final Supplier<T> statements) {
		return Methods.getCallerMethodName(statements, 2, BasicMeters::toFullMethodName)
				.map(callerMethodName -> PropertyNameBuilder.builder()
						.asSuffix()
						.path(callerMethodName)
						.build())
				.orElseThrow();
	}
}

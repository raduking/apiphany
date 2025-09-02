package org.apiphany.meters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.apiphany.lang.builder.PropertyNameBuilder;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Methods;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

/**
 * Test class for {@link BasicMeters}.
 *
 * @author Radu Sebastian LAZIN
 */
class BasicMetersTest {

	private static final String METRICS_PREFIX = "test.basic.metrics";
	private static final String TAG_KEY = "tagKey1";
	private static final String TAG_VALUE = "tagValue1";
	private static final String TEST_EXCEPTION_MESSAGE = "metrics test exception";

	@Test
	void shouldSetMetricsToThisMethod() {
		BasicMeters meters = BasicMeters.onMethod(METRICS_PREFIX);

		assertThat(meters.latency(Timer.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-this-method." + BasicMeters.Name.LATENCY));
	}

	@Test
	void shouldSetMetricsToCallerMethod() {
		BasicMeters meters = myUtilityMethod(METRICS_PREFIX);

		assertThat(meters.latency(Timer.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-caller-method." + BasicMeters.Name.LATENCY));
	}

	@Test
	void shouldSetMetricsToThisMethodWithTags() {
		BasicMeters meters = BasicMeters.onMethod(METRICS_PREFIX, Tags.empty());

		assertThat(meters.latency(Timer.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-this-method-with-tags." + BasicMeters.Name.LATENCY));
	}

	@Test
	void shouldSetMetricsToCallerMethodWithTags() {
		BasicMeters meters = myUtilityMethod(METRICS_PREFIX, Tags.empty());

		assertThat(meters.latency(Timer.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-caller-method-with-tags." + BasicMeters.Name.LATENCY));
	}

	@Test
	void shouldSetMetricsToOfMethodWhenDepthIsTwo() {
		BasicMeters meters = BasicMeters.onMethod(METRICS_PREFIX, 2);

		assertThat(meters.latency(Timer.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".on-method." + BasicMeters.Name.LATENCY));
	}

	@Test
	void shouldNotSetMetricsToThisMethodWhenDepthIsFour() {
		BasicMeters metersDepth3 = BasicMeters.onMethod(METRICS_PREFIX);
		BasicMeters metersDepth4 = BasicMeters.onMethod(METRICS_PREFIX, 4);

		assertThat(metersDepth3.latency(Timer.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".should-not-set-metrics-to-this-method-when-depth-is-four." + BasicMeters.Name.LATENCY));
		assertThat(metersDepth4.latency(Timer.class).getId().getName(),
				not(equalTo(metersDepth3.latency(Timer.class).getId().getName())));
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

	@Test
	void shouldWrapAndSwallowExceptionButSendMetricsWithTagsForGivenMeterFactory() {
		Tags tags = Tags.of(TAG_KEY, TAG_VALUE);

		MeterFactory factory = mock(MeterFactory.class);

		MeterTimer latency = mock(MeterTimer.class);
		doReturn(latency).when(factory).timer(METRICS_PREFIX, BasicMeters.Name.LATENCY, tags);
		MeterCounter requests = mock(MeterCounter.class);
		doReturn(requests).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.REQUEST, tags);
		MeterCounter errors = mock(MeterCounter.class);
		doReturn(errors).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.ERROR, tags);

		BasicMeters.wrapAndSwallow(factory, METRICS_PREFIX, tags, () -> {
			throw new RuntimeException(TEST_EXCEPTION_MESSAGE);
		});

		verify(requests).increment();
		verify(errors).increment();
		verify(latency).record(any());
	}

	@Test
	void shouldWrapAndSwallowExceptionButSendMetricsWithoutTagsForGivenMeterFactory() {
		List<?> tags = Collections.emptyList();

		MeterFactory factory = mock(MeterFactory.class);

		MeterTimer latency = mock(MeterTimer.class);
		doReturn(latency).when(factory).timer(METRICS_PREFIX, BasicMeters.Name.LATENCY, tags);
		MeterCounter requests = mock(MeterCounter.class);
		doReturn(requests).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.REQUEST, tags);
		MeterCounter errors = mock(MeterCounter.class);
		doReturn(errors).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.ERROR, tags);

		BasicMeters.wrapAndSwallow(factory, METRICS_PREFIX, (Runnable) () -> {
			throw new RuntimeException(TEST_EXCEPTION_MESSAGE);
		});

		verify(requests).increment();
		verify(errors).increment();
		verify(latency).record(any());
	}

	@Test
	void shouldWrapAndSwallowExceptionButSendMetricsWithoutTagsForGivenMeterFactoryAndNotReturnSuppliedValue() {
		List<?> tags = Collections.emptyList();

		MeterFactory factory = mock(MeterFactory.class);

		MeterTimer latency = mock(MeterTimer.class);
		doReturn(latency).when(factory).timer(METRICS_PREFIX, BasicMeters.Name.LATENCY, tags);
		MeterCounter requests = mock(MeterCounter.class);
		doReturn(requests).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.REQUEST, tags);
		MeterCounter errors = mock(MeterCounter.class);
		doReturn(errors).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.ERROR, tags);

		String result = BasicMeters.wrapAndSwallow(factory, METRICS_PREFIX, (Supplier<String>) () -> {
			throw new RuntimeException(TEST_EXCEPTION_MESSAGE);
		});

		assertThat(result, nullValue());

		verify(requests).increment();
		verify(errors).increment();
		verify(latency).record(any());
	}

	@Test
	void shouldWrapAndSwallowExceptionButSendMetricsWithTagsAndDefaultMeterFactory() {
		String prefix = METRICS_PREFIX + "." + "some-random-string";
		BasicMeters.wrapAndSwallow(prefix, (Runnable) () -> {
			throw new RuntimeException(TEST_EXCEPTION_MESSAGE);
		});

		Collection<?> search = Metrics.globalRegistry.get(prefix + "." + BasicMeters.Name.LATENCY).meters();
		assertThat(search, hasSize(1));

		search = Metrics.globalRegistry.get(prefix + "." + BasicMeters.Name.REQUEST).meters();
		assertThat(search, hasSize(1));

		search = Metrics.globalRegistry.get(prefix + "." + BasicMeters.Name.ERROR).meters();
		assertThat(search, hasSize(1));

		search = Metrics.globalRegistry.get(prefix + "." + BasicMeters.Name.RETRY).meters();
		assertThat(search, hasSize(1));
	}

}

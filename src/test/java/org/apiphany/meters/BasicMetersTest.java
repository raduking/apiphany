package org.apiphany.meters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.apiphany.lang.builder.PropertyNameBuilder;
import org.apiphany.utils.Tests;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Methods;

import io.micrometer.core.instrument.Counter;
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
	private static final String SOME_STRING = "someString";

	@Test
	void shouldThrowExceptionOnCallingNameConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(BasicMeters.Name.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldSetMetricsOnOfWithPrefixPropertyNameBuilder() {
		PropertyNameBuilder prefixBuilder = PropertyNameBuilder.builder()
				.path(METRICS_PREFIX);

		BasicMeters.of(prefixBuilder);

		String prefix = prefixBuilder.build();

		Collection<?> search = Metrics.globalRegistry.get(prefix + "." + BasicMeters.Name.LATENCY).meters();
		assertThat(search, hasSize(1));

		search = Metrics.globalRegistry.get(prefix + "." + BasicMeters.Name.REQUEST).meters();
		assertThat(search, hasSize(1));

		search = Metrics.globalRegistry.get(prefix + "." + BasicMeters.Name.ERROR).meters();
		assertThat(search, hasSize(1));

		search = Metrics.globalRegistry.get(prefix + "." + BasicMeters.Name.RETRY).meters();
		assertThat(search, hasSize(1));
	}

	@Test
	void shouldSetMetricsOnOfWithPrefixTagsAndMeterFactory() {
		Tags tags = Tags.of(TAG_KEY, TAG_VALUE);

		MeterFactory factory = mock(MeterFactory.class);

		MeterTimer latency = mock(MeterTimer.class);
		doReturn(latency).when(factory).timer(METRICS_PREFIX, BasicMeters.Name.LATENCY, tags);
		MeterCounter requests = mock(MeterCounter.class);
		doReturn(requests).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.REQUEST, tags);
		MeterCounter errors = mock(MeterCounter.class);
		doReturn(errors).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.ERROR, tags);
		MeterCounter retries = mock(MeterCounter.class);
		doReturn(retries).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.RETRY, tags);

		BasicMeters meters = BasicMeters.of(factory, METRICS_PREFIX, tags);

		assertThat(meters.latency(), notNullValue());
		assertThat(meters.requests(), notNullValue());
		assertThat(meters.errors(), notNullValue());
		assertThat(meters.retries(), notNullValue());
	}

	@Test
	void shouldSetMetricsOnOfWithPrefixPropertyNameBuilderTagsAndMeterFactory() {
		PropertyNameBuilder prefixBuilder = PropertyNameBuilder.builder()
				.path(METRICS_PREFIX);

		MeterFactory factory = mock(MeterFactory.class);

		MeterTimer latency = mock(MeterTimer.class);
		doReturn(latency).when(factory).timer(METRICS_PREFIX, BasicMeters.Name.LATENCY, Collections.emptyList());
		MeterCounter requests = mock(MeterCounter.class);
		doReturn(requests).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.REQUEST, Collections.emptyList());
		MeterCounter errors = mock(MeterCounter.class);
		doReturn(errors).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.ERROR, Collections.emptyList());
		MeterCounter retries = mock(MeterCounter.class);
		doReturn(retries).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.RETRY, Collections.emptyList());

		BasicMeters meters = BasicMeters.of(factory, prefixBuilder);

		assertThat(meters.latency(), notNullValue());
		assertThat(meters.requests(), notNullValue());
		assertThat(meters.errors(), notNullValue());
		assertThat(meters.retries(), notNullValue());
	}

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
		Tags tags = Tags.of(TAG_KEY, TAG_VALUE);
		BasicMeters meters = BasicMeters.onMethod(METRICS_PREFIX, tags);

		assertThat(meters.latency(Timer.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-this-method-with-tags." + BasicMeters.Name.LATENCY));
		assertThat(meters.requests(Counter.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-this-method-with-tags." + BasicMeters.Name.REQUEST));
		assertThat(meters.errors(Counter.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-this-method-with-tags." + BasicMeters.Name.ERROR));
		assertThat(meters.retries(Counter.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-this-method-with-tags." + BasicMeters.Name.RETRY));

		assertThat(meters.latency(Timer.class).getId().getTag(TAG_KEY), equalTo(TAG_VALUE));
		assertThat(meters.requests(Counter.class).getId().getTag(TAG_KEY), equalTo(TAG_VALUE));
		assertThat(meters.errors(Counter.class).getId().getTag(TAG_KEY), equalTo(TAG_VALUE));
		assertThat(meters.retries(Counter.class).getId().getTag(TAG_KEY), equalTo(TAG_VALUE));
	}

	@Test
	void shouldSetMetricsToCallerMethodWithTags() {
		Tags tags = Tags.of(TAG_KEY, TAG_VALUE);
		BasicMeters meters = myUtilityMethod(METRICS_PREFIX, tags);

		assertThat(meters.latency(Timer.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-caller-method-with-tags." + BasicMeters.Name.LATENCY));
		assertThat(meters.requests(Counter.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-caller-method-with-tags." + BasicMeters.Name.REQUEST));
		assertThat(meters.errors(Counter.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-caller-method-with-tags." + BasicMeters.Name.ERROR));
		assertThat(meters.retries(Counter.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".should-set-metrics-to-caller-method-with-tags." + BasicMeters.Name.RETRY));

		assertThat(meters.latency(Timer.class).getId().getTag(TAG_KEY), equalTo(TAG_VALUE));
		assertThat(meters.requests(Counter.class).getId().getTag(TAG_KEY), equalTo(TAG_VALUE));
		assertThat(meters.errors(Counter.class).getId().getTag(TAG_KEY), equalTo(TAG_VALUE));
		assertThat(meters.retries(Counter.class).getId().getTag(TAG_KEY), equalTo(TAG_VALUE));
	}

	@Test
	void shouldSetMetricsToOfMethodWhenDepthIsTwo() {
		BasicMeters meters = BasicMeters.onMethod(METRICS_PREFIX, 2);

		assertThat(meters.latency(Timer.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".on-method." + BasicMeters.Name.LATENCY));
		assertThat(meters.requests(Counter.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".on-method." + BasicMeters.Name.REQUEST));
		assertThat(meters.errors(Counter.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".on-method." + BasicMeters.Name.ERROR));
		assertThat(meters.retries(Counter.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".on-method." + BasicMeters.Name.RETRY));
	}

	@Test
	void shouldSetMetricsToOfMethodWhenDepthIsTwoWithTags() {
		Tags tags = Tags.of(TAG_KEY, TAG_VALUE);
		BasicMeters meters = BasicMeters.onMethod(METRICS_PREFIX, tags, 2);

		assertThat(meters.latency(Timer.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".on-method." + BasicMeters.Name.LATENCY));
		assertThat(meters.requests(Counter.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".on-method." + BasicMeters.Name.REQUEST));
		assertThat(meters.errors(Counter.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".on-method." + BasicMeters.Name.ERROR));
		assertThat(meters.retries(Counter.class).getId().getName(),
				equalTo(METRICS_PREFIX + ".on-method." + BasicMeters.Name.RETRY));

		assertThat(meters.latency(Timer.class).getId().getTag(TAG_KEY), equalTo(TAG_VALUE));
		assertThat(meters.requests(Counter.class).getId().getTag(TAG_KEY), equalTo(TAG_VALUE));
		assertThat(meters.errors(Counter.class).getId().getTag(TAG_KEY), equalTo(TAG_VALUE));
		assertThat(meters.retries(Counter.class).getId().getTag(TAG_KEY), equalTo(TAG_VALUE));
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
	void shouldSetMetricsToOfMethodWhenDepthIsTwoWithTagsAndMeterFactory() {
		Tags tags = Tags.of(TAG_KEY, TAG_VALUE);

		MeterFactory factory = mock(MeterFactory.class);

		MeterTimer latency = mock(MeterTimer.class);
		doReturn(latency).when(factory).timer(METRICS_PREFIX + ".on-method", BasicMeters.Name.LATENCY, tags);
		MeterCounter requests = mock(MeterCounter.class);
		doReturn(requests).when(factory).counter(METRICS_PREFIX + ".on-method", BasicMeters.Name.REQUEST, tags);
		MeterCounter errors = mock(MeterCounter.class);
		doReturn(errors).when(factory).counter(METRICS_PREFIX + ".on-method", BasicMeters.Name.ERROR, tags);
		MeterCounter retries = mock(MeterCounter.class);
		doReturn(retries).when(factory).counter(METRICS_PREFIX + ".on-method", BasicMeters.Name.RETRY, tags);

		BasicMeters meters = BasicMeters.onMethod(factory, METRICS_PREFIX, tags, 2);

		assertThat(meters.latency(), notNullValue());
		assertThat(meters.requests(), notNullValue());
		assertThat(meters.errors(), notNullValue());
		assertThat(meters.retries(), notNullValue());
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

	@Test
	void shouldWrapSendMetricsWithoutTagsForGivenMeterFactoryAndRunnable() {
		List<?> tags = Collections.emptyList();

		MeterFactory factory = mock(MeterFactory.class);

		MeterTimer latency = mock(MeterTimer.class);
		doReturn(latency).when(factory).timer(METRICS_PREFIX, BasicMeters.Name.LATENCY, tags);
		MeterCounter requests = mock(MeterCounter.class);
		doReturn(requests).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.REQUEST, tags);
		MeterCounter errors = mock(MeterCounter.class);
		doReturn(errors).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.ERROR, tags);

		BasicMeters.wrap(factory, METRICS_PREFIX, (Runnable) () -> {
			// empty
		});

		verify(requests).increment();
		verifyNoInteractions(errors);
		verify(latency).record(any());
	}

	@Test
	void shouldWrapSendMetricsWithoutTagsForGivenMeterFactoryAndSupplier() {
		List<?> tags = Collections.emptyList();

		MeterFactory factory = mock(MeterFactory.class);

		MeterTimer latency = mock(MeterTimer.class);
		doReturn(latency).when(factory).timer(METRICS_PREFIX, BasicMeters.Name.LATENCY, tags);
		MeterCounter requests = mock(MeterCounter.class);
		doReturn(requests).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.REQUEST, tags);
		MeterCounter errors = mock(MeterCounter.class);
		doReturn(errors).when(factory).counter(METRICS_PREFIX, BasicMeters.Name.ERROR, tags);

		String result = BasicMeters.wrap(factory, METRICS_PREFIX, () -> SOME_STRING);

		assertThat(result, equalTo(SOME_STRING));

		verify(requests).increment();
		verifyNoInteractions(errors);
		verify(latency).record(any());
	}

	@Test
	void shouldWrapButSendMetricsWithTagsWithDefaultMeterFactory() {
		String prefix = METRICS_PREFIX + "." + "some-random-string";
		BasicMeters.wrap(prefix, (Runnable) () -> {
			// empty
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

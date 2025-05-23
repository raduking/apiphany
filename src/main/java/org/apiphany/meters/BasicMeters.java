package org.apiphany.meters;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apiphany.lang.Strings;
import org.apiphany.lang.builder.PropertyNameBuilder;
import org.morphix.lang.Nullables;
import org.morphix.lang.Unchecked;
import org.morphix.lang.function.Runnables;
import org.morphix.reflection.Methods;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

/**
 * A record class for managing basic metrics such as latency, requests, retries, and errors. This class provides methods
 * to wrap code with metrics and record common metrics for a given prefix.
 *
 * @param meterRegistry the meter registry these metrics will be added to
 * @param latency the timer for measuring operation latency.
 * @param requests the counter for tracking the number of requests.
 * @param retries the counter for tracking the number of retries.
 * @param errors the counter for tracking the number of errors.
 *
 * @author Radu Sebastian LAZIN
 */
public record BasicMeters(MeterRegistry meterRegistry, Timer latency, Counter requests, Counter retries, Counter errors) {

	/**
	 * The separator used for building metric names.
	 */
	public static final String SEPARATOR = PropertyNameBuilder.DELIMITER;

	/**
	 * The metric name for latency.
	 */
	public static final String LATENCY_METRIC = "latency";

	/**
	 * The metric name for requests.
	 */
	public static final String REQUEST_METRIC = "request";

	/**
	 * The metric name for retries.
	 */
	public static final String RETRY_METRIC = "retry";

	/**
	 * The metric name for errors.
	 */
	public static final String ERROR_METRIC = "error";

	/**
	 * The default instance of {@link BasicMeters} that does not publish any metrics.
	 */
	public static final BasicMeters DEFAULT = new BasicMeters(
			BasicTimer.of(LATENCY_METRIC),
			BasicCounter.of(REQUEST_METRIC),
			BasicCounter.of(RETRY_METRIC),
			BasicCounter.of(ERROR_METRIC));

	/**
	 * The default depth of the caller to determine the name of the caller. This is used when constructing metric names
	 * based on the method name.
	 */
	private static final int OF_METHOD_CALLER_DEPTH = 3;

	/**
	 * Constructor which uses the global registry to publish metrics.
	 *
	 * @param latency the timer for measuring operation latency.
	 * @param requests the counter for tracking the number of requests.
	 * @param retries the counter for tracking the number of retries.
	 * @param errors the counter for tracking the number of errors.
	 */
	public BasicMeters(final Timer latency, final Counter requests, final Counter retries, final Counter errors) {
		this(Metrics.globalRegistry, latency, requests, retries, errors);
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors.
	 *
	 * @param <T> the return type of the supplier.
	 * @param supplier the code to wrap with metrics.
	 * @param onError the function to handle errors and provide a fallback value.
	 * @return the result of the supplier on success, or the result of the error handler on failure.
	 */
	public <T> T wrap(final Supplier<T> supplier, final Function<? super Exception, T> onError) {
		requests().increment();
		Instant startTime = Instant.now();
		try {
			return supplier.get();
		} catch (Exception e) {
			errors().increment();
			return onError.apply(e);
		} finally {
			latency().record(Duration.between(startTime, Instant.now()));
		}
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the given prefix and tags. This method doesn't call
	 * the {@link #of(MeterRegistry, String, Tags)} because wee need to keep the call-stack the same.
	 *
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters of(final String prefix, final Tags tags) {
		return new BasicMeters(
				Metrics.globalRegistry,
				Metrics.timer(String.join(SEPARATOR, prefix, LATENCY_METRIC), tags),
				Metrics.counter(String.join(SEPARATOR, prefix, REQUEST_METRIC), tags),
				Metrics.counter(String.join(SEPARATOR, prefix, RETRY_METRIC), tags),
				Metrics.counter(String.join(SEPARATOR, prefix, ERROR_METRIC), tags));
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the given prefix and tags.
	 *
	 * @param meterRegistry the meter registry
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters of(final MeterRegistry meterRegistry, final String prefix, final Tags tags) {
		return new BasicMeters(
				meterRegistry,
				meterRegistry.timer(String.join(SEPARATOR, prefix, LATENCY_METRIC), tags),
				meterRegistry.counter(String.join(SEPARATOR, prefix, REQUEST_METRIC), tags),
				meterRegistry.counter(String.join(SEPARATOR, prefix, RETRY_METRIC), tags),
				meterRegistry.counter(String.join(SEPARATOR, prefix, ERROR_METRIC), tags));
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the given prefix and no tags.
	 *
	 * @param prefix the prefix for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters of(final String prefix) {
		return of(prefix, Tags.empty());
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the given prefix and no tags.
	 *
	 * @param meterRegistry the meter registry
	 * @param prefix the prefix for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters of(final MeterRegistry meterRegistry, final String prefix) {
		return of(meterRegistry, prefix, Tags.empty());
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix built by the provided
	 * {@link PropertyNameBuilder}.
	 *
	 * @param prefixBuilder the builder for the metric prefix.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters of(final PropertyNameBuilder prefixBuilder) {
		return of(prefixBuilder.build());
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix built by the provided
	 * {@link PropertyNameBuilder}.
	 *
	 * @param meterRegistry the meter registry
	 * @param prefixBuilder the builder for the metric prefix.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters of(final MeterRegistry meterRegistry, final PropertyNameBuilder prefixBuilder) {
		return of(meterRegistry, prefixBuilder.build());
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the current method name.
	 *
	 * @param prefix the prefix for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters onMethod(final String prefix) {
		return of(buildPrefixWithMethod(prefix, OF_METHOD_CALLER_DEPTH));
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the current method name.
	 *
	 * @param meterRegistry the meter registry
	 * @param prefix the prefix for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters onMethod(final MeterRegistry meterRegistry, final String prefix) {
		return of(meterRegistry, buildPrefixWithMethod(prefix, OF_METHOD_CALLER_DEPTH));
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the current method name and the
	 * provided tags.
	 *
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters onMethod(final String prefix, final Tags tags) {
		return of(buildPrefixWithMethod(prefix, OF_METHOD_CALLER_DEPTH), tags);
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the current method name and the
	 * provided tags.
	 *
	 * @param meterRegistry the meter registry
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters onMethod(final MeterRegistry meterRegistry, final String prefix, final Tags tags) {
		return of(meterRegistry, buildPrefixWithMethod(prefix, OF_METHOD_CALLER_DEPTH), tags);
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the method name at the specified
	 * depth.
	 *
	 * @param prefix the prefix for the metrics.
	 * @param depth the depth in the call stack to determine the method name.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters onMethod(final String prefix, final int depth) {
		return of(buildPrefixWithMethod(prefix, depth));
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the method name at the specified
	 * depth.
	 *
	 * @param meterRegistry the meter registry
	 * @param prefix the prefix for the metrics.
	 * @param depth the depth in the call stack to determine the method name.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters onMethod(final MeterRegistry meterRegistry, final String prefix, final int depth) {
		return of(meterRegistry, buildPrefixWithMethod(prefix, depth));
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the method name at the specified
	 * depth and the provided tags.
	 *
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @param depth the depth in the call stack to determine the method name.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters onMethod(final String prefix, final Tags tags, final int depth) {
		return of(buildPrefixWithMethod(prefix, depth), tags);
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the method name at the specified
	 * depth and the provided tags.
	 *
	 * @param meterRegistry the meter registry
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @param depth the depth in the call stack to determine the method name.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters onMethod(final MeterRegistry meterRegistry, final String prefix, final Tags tags, final int depth) {
		return of(meterRegistry, buildPrefixWithMethod(prefix, depth), tags);
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the caller method name.
	 *
	 * @param prefix the prefix for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters onCallerMethod(final String prefix) {
		return of(buildPrefixWithMethod(prefix, OF_METHOD_CALLER_DEPTH + 1));
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the caller method name.
	 *
	 * @param meterRegistry the meter registry
	 * @param prefix the prefix for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters onCallerMethod(final MeterRegistry meterRegistry, final String prefix) {
		return of(meterRegistry, buildPrefixWithMethod(prefix, OF_METHOD_CALLER_DEPTH + 1));
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the caller method name and the
	 * provided tags.
	 *
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters onCallerMethod(final String prefix, final Tags tags) {
		return of(buildPrefixWithMethod(prefix, OF_METHOD_CALLER_DEPTH + 1), tags);
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the caller method name and the
	 * provided tags.
	 *
	 * @param meterRegistry the meter registry
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters onCallerMethod(final MeterRegistry meterRegistry, final String prefix, final Tags tags) {
		return of(meterRegistry, buildPrefixWithMethod(prefix, OF_METHOD_CALLER_DEPTH + 1), tags);
	}

	/**
	 * Builds a metric prefix by appending the current method name to the given prefix.
	 *
	 * @param prefix the base prefix.
	 * @param depth the depth in the call stack to determine the method name.
	 * @return the constructed prefix.
	 */
	private static String buildPrefixWithMethod(final String prefix, final int depth) {
		String methodName = Methods.getCurrentMethodName(false, depth);
		if (null == methodName) {
			return prefix;
		}
		return PropertyNameBuilder.builder()
				.path(prefix)
				.path(Strings.fromLowerCamelToKebabCase(methodName))
				.build();
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors.
	 *
	 * @param <T> the return type of the supplier.
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param supplier the code to wrap with metrics.
	 * @param onError the function to handle errors and provide a fallback value.
	 * @return the result of the supplier on success, or the result of the error handler on failure.
	 */
	public static <T> T wrap(final String prefix, final Tags tags, final Supplier<T> supplier, final Function<? super Exception, T> onError) {
		return BasicMeters.of(prefix, tags)
				.wrap(supplier, onError);
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors.
	 *
	 * @param <T> the return type of the supplier.
	 * @param meterRegistry the meter registry
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param supplier the code to wrap with metrics.
	 * @param onError the function to handle errors and provide a fallback value.
	 * @return the result of the supplier on success, or the result of the error handler on failure.
	 */
	public static <T> T wrap(final MeterRegistry meterRegistry, final String prefix, final Tags tags, final Supplier<T> supplier,
			final Function<? super Exception, T> onError) {
		return BasicMeters.of(meterRegistry, prefix, tags)
				.wrap(supplier, onError);
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors. Re-throws any exceptions.
	 *
	 * @param <T> the return type of the supplier.
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param supplier the code to wrap with metrics.
	 * @return the result of the supplier.
	 */
	public static <T> T wrap(final String prefix, final Tags tags, final Supplier<T> supplier) {
		return wrap(prefix, tags, supplier, Unchecked.Undeclared::reThrow);
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors. Re-throws any exceptions.
	 *
	 * @param <T> the return type of the supplier.
	 * @param meterRegistry the meter registry
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param supplier the code to wrap with metrics.
	 * @return the result of the supplier.
	 */
	public static <T> T wrap(final MeterRegistry meterRegistry, final String prefix, final Tags tags, final Supplier<T> supplier) {
		return wrap(meterRegistry, prefix, tags, supplier, Unchecked.Undeclared::reThrow);
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors. Uses no tags.
	 *
	 * @param <T> the return type of the supplier.
	 * @param prefix the metric prefix.
	 * @param supplier the code to wrap with metrics.
	 * @return the result of the supplier.
	 */
	public static <T> T wrap(final String prefix, final Supplier<T> supplier) {
		return wrap(prefix, Tags.empty(), supplier);
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors. Uses no tags.
	 *
	 * @param <T> the return type of the supplier.
	 * @param meterRegistry the meter registry
	 * @param prefix the metric prefix.
	 * @param supplier the code to wrap with metrics.
	 * @return the result of the supplier.
	 */
	public static <T> T wrap(final MeterRegistry meterRegistry, final String prefix, final Supplier<T> supplier) {
		return wrap(meterRegistry, prefix, Tags.empty(), supplier);
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors.
	 *
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param runnable the code to wrap with metrics.
	 */
	public static void wrap(final String prefix, final Tags tags, final Runnable runnable) {
		wrap(prefix, tags, Runnables.toSupplier(runnable));
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors.
	 *
	 * @param meterRegistry the meter registry
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param runnable the code to wrap with metrics.
	 */
	public static void wrap(final MeterRegistry meterRegistry, final String prefix, final Tags tags, final Runnable runnable) {
		wrap(meterRegistry, prefix, tags, Runnables.toSupplier(runnable));
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors. Uses no tags.
	 *
	 * @param prefix the metric prefix.
	 * @param runnable the code to wrap with metrics.
	 */
	public static void wrap(final String prefix, final Runnable runnable) {
		wrap(prefix, Tags.empty(), runnable);
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors. Uses no tags.
	 *
	 * @param meterRegistry the meter registry
	 * @param prefix the metric prefix.
	 * @param runnable the code to wrap with metrics.
	 */
	public static void wrap(final MeterRegistry meterRegistry, final String prefix, final Runnable runnable) {
		wrap(meterRegistry, prefix, Tags.empty(), runnable);
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors. Swallows exceptions and provides a
	 * fallback value.
	 *
	 * @param <T> the return type of the supplier.
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param supplier the code to wrap with metrics.
	 * @param onErrorSupplier the supplier for the fallback value in case of an error.
	 * @return the result of the supplier on success, or the fallback value on failure.
	 */
	public static <T> T wrapAndSwallow(final String prefix, final Tags tags, final Supplier<T> supplier, final Supplier<T> onErrorSupplier) {
		return wrap(prefix, tags, supplier, e -> onErrorSupplier.get());
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors. Swallows exceptions and provides a
	 * fallback value.
	 *
	 * @param <T> the return type of the supplier.
	 * @param meterRegistry the meter registry
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param supplier the code to wrap with metrics.
	 * @param onErrorSupplier the supplier for the fallback value in case of an error.
	 * @return the result of the supplier on success, or the fallback value on failure.
	 */
	public static <T> T wrapAndSwallow(final MeterRegistry meterRegistry, final String prefix, final Tags tags, final Supplier<T> supplier, final Supplier<T> onErrorSupplier) {
		return wrap(meterRegistry, prefix, tags, supplier, e -> onErrorSupplier.get());
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors. Swallows exceptions and returns null
	 * on failure.
	 *
	 * @param <T> the return type of the supplier.
	 * @param prefix the metric prefix.
	 * @param supplier the code to wrap with metrics.
	 * @return the result of the supplier on success, or null on failure.
	 */
	public static <T> T wrapAndSwallow(final String prefix, final Supplier<T> supplier) {
		return wrapAndSwallow(prefix, Tags.empty(), supplier, Nullables.supplyNull());
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors. Swallows exceptions and returns null
	 * on failure.
	 *
	 * @param <T> the return type of the supplier.
	 * @param meterRegistry the meter registry
	 * @param prefix the metric prefix.
	 * @param supplier the code to wrap with metrics.
	 * @return the result of the supplier on success, or null on failure.
	 */
	public static <T> T wrapAndSwallow(final MeterRegistry meterRegistry, final String prefix, final Supplier<T> supplier) {
		return wrapAndSwallow(meterRegistry, prefix, Tags.empty(), supplier, Nullables.supplyNull());
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors. The method swallows exceptions.
	 *
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param runnable the code to wrap with metrics.
	 */
	public static void wrapAndSwallow(final String prefix, final Tags tags, final Runnable runnable) {
		wrapAndSwallow(prefix, tags, Runnables.toSupplier(runnable), Nullables.supplyNull());
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors. The method swallows exceptions.
	 *
	 * @param meterRegistry the meter registry
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param runnable the code to wrap with metrics.
	 */
	public static void wrapAndSwallow(final MeterRegistry meterRegistry, final String prefix, final Tags tags, final Runnable runnable) {
		wrapAndSwallow(meterRegistry, prefix, tags, Runnables.toSupplier(runnable), Nullables.supplyNull());
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors. The method swallows exceptions. Uses
	 * no tags.
	 *
	 * @param prefix the metric prefix.
	 * @param runnable the code to wrap with metrics.
	 */
	public static void wrapAndSwallow(final String prefix, final Runnable runnable) {
		wrapAndSwallow(prefix, Tags.empty(), runnable);
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors. The method swallows exceptions. Uses
	 * no tags.
	 *
	 * @param meterRegistry the meter registry
	 * @param prefix the metric prefix.
	 * @param runnable the code to wrap with metrics.
	 */
	public static void wrapAndSwallow(final MeterRegistry meterRegistry, final String prefix, final Runnable runnable) {
		wrapAndSwallow(meterRegistry, prefix, Tags.empty(), runnable);
	}

	/**
	 * Returns the full method name given the class name and method name. This method sanitizes the method name by removing
	 * lambda prefixes and converting it to a kebab-case format.<br/>
	 * Example: <code>a.b.ClassName</code>.
	 * <p>
	 * StackTraceElement.methodName contains the method name, which can sometimes be lambda, for example:
	 * <code>lambda$methodName$1</code>
	 * <p>
	 * This method resides here because it is a metrics specific way of computing the method name.
	 *
	 * @param className the full class name, including the package path.
	 * @param methodName the method name, which may include lambda prefixes.
	 * @return the sanitized class name and method name in the format "ClassName.method-name".
	 */
	public static String toFullMethodName(final String className, final String methodName) {
		PropertyNameBuilder sb = PropertyNameBuilder.builder();
		int lastIndexOfDot = className.lastIndexOf(".");
		sb = sb.path(lastIndexOfDot != -1 ? className.substring(lastIndexOfDot + 1) : className);

		int lastIndexOfDollar = methodName.lastIndexOf("$");
		sb = sb.path(lastIndexOfDollar != -1
				? methodName.substring(0, lastIndexOfDollar).replace("$", "-")
				: methodName);

		return sb.build();
	}
}

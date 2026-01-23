package org.apiphany.meters;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apiphany.lang.Strings;
import org.apiphany.lang.builder.PropertyNameBuilder;
import org.morphix.lang.Nullables;
import org.morphix.lang.Unchecked;
import org.morphix.lang.function.Runnables;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Methods;

/**
 * A record for managing basic metrics such as latency, requests, retries, and errors. This class provides methods to
 * wrap code with metrics and record common metrics for a given prefix.
 *
 * @param factory the meter factory to construct the meters.
 * @param latency the timer for measuring operation latency.
 * @param requests the counter for tracking the number of requests.
 * @param retries the counter for tracking the number of retries.
 * @param errors the counter for tracking the number of errors.
 *
 * @author Radu Sebastian LAZIN
 */
public record BasicMeters(
		MeterFactory factory,
		MeterTimer latency,
		MeterCounter requests,
		MeterCounter retries,
		MeterCounter errors) {

	/**
	 * Namespace for metric names.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Name {

		/**
		 * The metric name for latency.
		 */
		public static final String LATENCY = "latency";

		/**
		 * The metric name for requests.
		 */
		public static final String REQUEST = "request";

		/**
		 * The metric name for retries.
		 */
		public static final String RETRY = "retry";

		/**
		 * The metric name for errors.
		 */
		public static final String ERROR = "error";

		/**
		 * Hide constructor.
		 */
		private Name() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * The default instance of {@link BasicMeters} that does not publish any metrics.
	 */
	public static final BasicMeters DEFAULT = new BasicMeters(
			BasicTimer.of(Name.LATENCY),
			BasicCounter.of(Name.REQUEST),
			BasicCounter.of(Name.RETRY),
			BasicCounter.of(Name.ERROR));

	/**
	 * The default depth of the caller to determine the name of the caller. This is used when constructing metric names
	 * based on the method name.
	 */
	private static final int OF_METHOD_CALLER_DEPTH = 3;

	/**
	 * The meters cache to avoid recreation of the same meters multiple times.<br/>
	 * TODO: implement LRU Cache (Least Recently Used Cache)
	 */
	private static final ConcurrentMap<String, BasicMeters> METERS_CACHE = new ConcurrentHashMap<>();

	/**
	 * Constructor.
	 *
	 * @param latency the timer for measuring operation latency.
	 * @param requests the counter for tracking the number of requests.
	 * @param retries the counter for tracking the number of retries.
	 * @param errors the counter for tracking the number of errors.
	 */
	public BasicMeters(
			final MeterTimer latency,
			final MeterCounter requests,
			final MeterCounter retries,
			final MeterCounter errors) {
		this(MeterFactory.instance(), latency, requests, retries, errors);
	}

	/**
	 * Returns the underlying latency timer object which must have the given type.
	 *
	 * @param <T> type of the underlying timer object
	 * @param cls class of the underlying timer object
	 * @return the underlying latency timer object which must have the given type
	 */
	public <T> T latency(final Class<T> cls) {
		return latency().unwrap(cls);
	}

	/**
	 * Returns the underlying requests counter object which must have the given type.
	 *
	 * @param <T> type of the underlying counter object
	 * @param cls class of the underlying counter object
	 * @return the underlying requests counter object which must have the given type
	 */
	public <T> T requests(final Class<T> cls) {
		return requests().unwrap(cls);
	}

	/**
	 * Returns the underlying retries counter object which must have the given type.
	 *
	 * @param <T> type of the underlying counter object
	 * @param cls class of the underlying counter object
	 * @return the underlying retries counter object which must have the given type
	 */
	public <T> T retries(final Class<T> cls) {
		return retries().unwrap(cls);
	}

	/**
	 * Returns the underlying errors counter object which must have the given type.
	 *
	 * @param <T> type of the underlying counter object
	 * @param cls class of the underlying counter object
	 * @return the underlying errors counter object which must have the given type
	 */
	public <T> T errors(final Class<T> cls) {
		return errors().unwrap(cls);
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
	 * Constructs a {@link BasicMeters} object with all meters having the given prefix and tags.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static <T, U extends Iterable<T>> BasicMeters of(final String prefix, final U tags) {
		return of(MeterFactory.instance(), prefix, tags);
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the given prefix and tags.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param factory the meter factory
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static <T, U extends Iterable<T>> BasicMeters of(final MeterFactory factory, final String prefix, final U tags) {
		MeterFactory meterFactory = Nullables.nonNullOrDefault(factory, MeterFactory.instance());
		Supplier<BasicMeters> basicMetersInstantiator = () -> new BasicMeters(
				meterFactory.timer(prefix, Name.LATENCY, tags),
				meterFactory.counter(prefix, Name.REQUEST, tags),
				meterFactory.counter(prefix, Name.RETRY, tags),
				meterFactory.counter(prefix, Name.ERROR, tags));
		if (meterFactory.isEmpty(tags)) {
			return METERS_CACHE.computeIfAbsent(prefix, key -> basicMetersInstantiator.get());
		}
		return basicMetersInstantiator.get();
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the given prefix and no tags.
	 *
	 * @param prefix the prefix for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters of(final String prefix) {
		return of(prefix, Collections.emptyList());
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the given prefix and no tags.
	 *
	 * @param factory the meter factory
	 * @param prefix the prefix for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters of(final MeterFactory factory, final String prefix) {
		return of(factory, prefix, Collections.emptyList());
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
	 * @param factory the meter factory
	 * @param prefixBuilder the builder for the metric prefix.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters of(final MeterFactory factory, final PropertyNameBuilder prefixBuilder) {
		return of(factory, prefixBuilder.build());
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
	 * @param factory the meter factory
	 * @param prefix the prefix for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters onMethod(final MeterFactory factory, final String prefix) {
		return of(factory, buildPrefixWithMethod(prefix, OF_METHOD_CALLER_DEPTH));
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the current method name and the
	 * provided tags.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static <T, U extends Iterable<T>> BasicMeters onMethod(final String prefix, final U tags) {
		return of(buildPrefixWithMethod(prefix, OF_METHOD_CALLER_DEPTH), tags);
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the current method name and the
	 * provided tags.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param factory the meter factory
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static <T, U extends Iterable<T>> BasicMeters onMethod(final MeterFactory factory, final String prefix, final U tags) {
		return of(factory, buildPrefixWithMethod(prefix, OF_METHOD_CALLER_DEPTH), tags);
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
	 * @param factory the meter factory
	 * @param prefix the prefix for the metrics.
	 * @param depth the depth in the call stack to determine the method name.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters onMethod(final MeterFactory factory, final String prefix, final int depth) {
		return of(factory, buildPrefixWithMethod(prefix, depth));
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the method name at the specified
	 * depth and the provided tags.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @param depth the depth in the call stack to determine the method name.
	 * @return a {@link BasicMeters} instance.
	 */
	public static <T, U extends Iterable<T>> BasicMeters onMethod(final String prefix, final U tags, final int depth) {
		return of(buildPrefixWithMethod(prefix, depth), tags);
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the method name at the specified
	 * depth and the provided tags.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param factory the meter factory
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @param depth the depth in the call stack to determine the method name.
	 * @return a {@link BasicMeters} instance.
	 */
	public static <T, U extends Iterable<T>> BasicMeters onMethod(final MeterFactory factory, final String prefix, final U tags, final int depth) {
		return of(factory, buildPrefixWithMethod(prefix, depth), tags);
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
	 * @param factory the meter factory
	 * @param prefix the prefix for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static BasicMeters onCallerMethod(final MeterFactory factory, final String prefix) {
		return of(factory, buildPrefixWithMethod(prefix, OF_METHOD_CALLER_DEPTH + 1));
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the caller method name and the
	 * provided tags.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static <T, U extends Iterable<T>> BasicMeters onCallerMethod(final String prefix, final U tags) {
		return of(buildPrefixWithMethod(prefix, OF_METHOD_CALLER_DEPTH + 1), tags);
	}

	/**
	 * Constructs a {@link BasicMeters} object with all meters having the prefix based on the caller method name and the
	 * provided tags.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param factory the meter factory
	 * @param prefix the prefix for the metrics.
	 * @param tags the tags for the metrics.
	 * @return a {@link BasicMeters} instance.
	 */
	public static <T, U extends Iterable<T>> BasicMeters onCallerMethod(final MeterFactory factory, final String prefix, final U tags) {
		return of(factory, buildPrefixWithMethod(prefix, OF_METHOD_CALLER_DEPTH + 1), tags);
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
	 * @param <R> the return type of the supplier.
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param supplier the code to wrap with metrics.
	 * @param onError the function to handle errors and provide a fallback value.
	 * @return the result of the supplier on success, or the result of the error handler on failure.
	 */
	public static <R, T, U extends Iterable<T>> R wrap(
			final String prefix, final U tags, final Supplier<R> supplier, final Function<? super Exception, R> onError) {
		return BasicMeters.of(prefix, tags)
				.wrap(supplier, onError);
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors.
	 *
	 * @param <R> the return type of the supplier.
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param factory the meter factory
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param supplier the code to wrap with metrics.
	 * @param onError the function to handle errors and provide a fallback value.
	 * @return the result of the supplier on success, or the result of the error handler on failure.
	 */
	public static <R, T, U extends Iterable<T>> R wrap(
			final MeterFactory factory, final String prefix, final U tags, final Supplier<R> supplier, final Function<? super Exception, R> onError) {
		return BasicMeters.of(factory, prefix, tags)
				.wrap(supplier, onError);
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors. Re-throws any exceptions.
	 *
	 * @param <R> the return type of the supplier.
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param supplier the code to wrap with metrics.
	 * @return the result of the supplier.
	 */
	public static <R, T, U extends Iterable<T>> R wrap(final String prefix, final U tags, final Supplier<R> supplier) {
		return wrap(prefix, tags, supplier, Unchecked.Undeclared::reThrow);
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors. Re-throws any exceptions.
	 *
	 * @param <R> the return type of the supplier.
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param factory the meter factory
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param supplier the code to wrap with metrics.
	 * @return the result of the supplier.
	 */
	public static <R, T, U extends Iterable<T>> R wrap(final MeterFactory factory, final String prefix, final U tags, final Supplier<R> supplier) {
		return wrap(factory, prefix, tags, supplier, Unchecked.Undeclared::reThrow);
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors. Uses no tags.
	 *
	 * @param <R> the return type of the supplier.
	 * @param prefix the metric prefix.
	 * @param supplier the code to wrap with metrics.
	 * @return the result of the supplier.
	 */
	public static <R> R wrap(final String prefix, final Supplier<R> supplier) {
		return wrap(prefix, Collections.emptyList(), supplier);
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors. Uses no tags.
	 *
	 * @param <R> the return type of the supplier.
	 * @param factory the meter factory
	 * @param prefix the metric prefix.
	 * @param supplier the code to wrap with metrics.
	 * @return the result of the supplier.
	 */
	public static <R> R wrap(final MeterFactory factory, final String prefix, final Supplier<R> supplier) {
		return wrap(factory, prefix, Collections.emptyList(), supplier);
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param runnable the code to wrap with metrics.
	 */
	public static <T, U extends Iterable<T>> void wrap(final String prefix, final U tags, final Runnable runnable) {
		wrap(prefix, tags, Runnables.toSupplier(runnable));
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param factory the meter factory
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param runnable the code to wrap with metrics.
	 */
	public static <T, U extends Iterable<T>> void wrap(final MeterFactory factory, final String prefix, final U tags, final Runnable runnable) {
		wrap(factory, prefix, tags, Runnables.toSupplier(runnable));
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors. Uses no tags.
	 *
	 * @param prefix the metric prefix.
	 * @param runnable the code to wrap with metrics.
	 */
	public static void wrap(final String prefix, final Runnable runnable) {
		wrap(prefix, Collections.emptyList(), runnable);
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors. Uses no tags.
	 *
	 * @param factory the meter factory
	 * @param prefix the metric prefix.
	 * @param runnable the code to wrap with metrics.
	 */
	public static void wrap(final MeterFactory factory, final String prefix, final Runnable runnable) {
		wrap(factory, prefix, Collections.emptyList(), runnable);
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors. Swallows exceptions and provides a
	 * fallback value.
	 *
	 * @param <R> the return type of the supplier.
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param supplier the code to wrap with metrics.
	 * @param onErrorSupplier the supplier for the fallback value in case of an error.
	 * @return the result of the supplier on success, or the fallback value on failure.
	 */
	public static <R, T, U extends Iterable<T>> R wrapAndSwallow(
			final String prefix, final U tags, final Supplier<R> supplier, final Supplier<R> onErrorSupplier) {
		return wrap(prefix, tags, supplier, e -> onErrorSupplier.get());
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors. Swallows exceptions and provides a
	 * fallback value.
	 *
	 * @param <R> the return type of the supplier.
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param factory the meter factory
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param supplier the code to wrap with metrics.
	 * @param onErrorSupplier the supplier for the fallback value in case of an error.
	 * @return the result of the supplier on success, or the fallback value on failure.
	 */
	public static <R, T, U extends Iterable<T>> R wrapAndSwallow(
			final MeterFactory factory, final String prefix, final U tags, final Supplier<R> supplier, final Supplier<R> onErrorSupplier) {
		return wrap(factory, prefix, tags, supplier, e -> onErrorSupplier.get());
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
		return wrapAndSwallow(prefix, Collections.emptyList(), supplier, Nullables.supplyNull());
	}

	/**
	 * Wraps the supplier code with metrics, recording latency, requests, and errors. Swallows exceptions and returns null
	 * on failure.
	 *
	 * @param <T> the return type of the supplier.
	 * @param factory the meter factory
	 * @param prefix the metric prefix.
	 * @param supplier the code to wrap with metrics.
	 * @return the result of the supplier on success, or null on failure.
	 */
	public static <T> T wrapAndSwallow(final MeterFactory factory, final String prefix, final Supplier<T> supplier) {
		return wrapAndSwallow(factory, prefix, Collections.emptyList(), supplier, Nullables.supplyNull());
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors. The method swallows exceptions.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param runnable the code to wrap with metrics.
	 */
	public static <T, U extends Iterable<T>> void wrapAndSwallow(final String prefix, final U tags, final Runnable runnable) {
		wrapAndSwallow(prefix, tags, Runnables.toSupplier(runnable), Nullables.supplyNull());
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors. The method swallows exceptions.
	 *
	 * @param <T> the tag element type
	 * @param <U> an iterable of tags
	 *
	 * @param factory the meter factory
	 * @param prefix the metric prefix.
	 * @param tags the metric tags.
	 * @param runnable the code to wrap with metrics.
	 */
	public static <T, U extends Iterable<T>> void wrapAndSwallow(
			final MeterFactory factory, final String prefix, final U tags, final Runnable runnable) {
		wrapAndSwallow(factory, prefix, tags, Runnables.toSupplier(runnable), Nullables.supplyNull());
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors. The method swallows exceptions. Uses
	 * no tags.
	 *
	 * @param prefix the metric prefix.
	 * @param runnable the code to wrap with metrics.
	 */
	public static void wrapAndSwallow(final String prefix, final Runnable runnable) {
		wrapAndSwallow(prefix, Collections.emptyList(), runnable);
	}

	/**
	 * Wraps the runnable code with metrics, recording latency, requests, and errors. The method swallows exceptions. Uses
	 * no tags.
	 *
	 * @param factory the meter factory
	 * @param prefix the metric prefix.
	 * @param runnable the code to wrap with metrics.
	 */
	public static void wrapAndSwallow(final MeterFactory factory, final String prefix, final Runnable runnable) {
		wrapAndSwallow(factory, prefix, Collections.emptyList(), runnable);
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

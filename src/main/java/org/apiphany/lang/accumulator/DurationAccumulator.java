package org.apiphany.lang.accumulator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

import org.apiphany.lang.Strings;
import org.apiphany.lang.Temporals;
import org.morphix.lang.Unchecked;

/**
 * Accumulates durations for operations executed by {@link Runnable} or {@link Supplier} instances. This class provides
 * functionality to measure and analyze the duration of operations, including calculating statistics such as average,
 * maximum, and percentile durations.
 * <p>
 * Before reusing this accumulator, make sure to clear it if new statistics are needed.
 *
 * @author Radu Sebastian LAZIN
 */
public class DurationAccumulator extends Accumulator<Duration> {

	/**
	 * Default value used when no durations are available.
	 */
	private static final double DEFAULT_VALUE = Double.NaN;

	/**
	 * Statistics object for storing calculated metrics.
	 */
	private Statistics statistics;

	/**
	 * Private constructor to enforce the use of the factory method {@link #of()}.
	 */
	private DurationAccumulator() {
		// empty
	}

	/**
	 * Creates a new instance of {@link DurationAccumulator}.
	 *
	 * @return a new {@link DurationAccumulator} instance
	 */
	public static DurationAccumulator of() {
		return new DurationAccumulator();
	}

	/**
	 * Accumulates the duration of the provided {@link Supplier} operation and returns its result. The duration is measured
	 * from the start of the operation until its completion.
	 *
	 * @param <U> the type of the result
	 *
	 * @param supplier the operation to measure
	 * @param defaultReturn the default value to return if the operation fails
	 * @return the result of the operation or the default value if an exception occurs
	 */
	@Override
	public <U> U accumulate(final Supplier<U> supplier, final U defaultReturn) {
		Instant start = Instant.now();
		U result = defaultReturn;
		try {
			result = supplier.get();
		} catch (Exception e) {
			Unchecked.reThrow(e);
		} finally {
			getInformationList().add(Duration.between(start, Instant.now()));
		}
		return result;
	}

	/**
	 * Resets the accumulator. This method does not clear the accumulated durations, allowing statistics to be built from
	 * the existing data.
	 */
	@Override
	public void rest() {
		// don't clear the information list so statistics can be built
	}

	/**
	 * Converts the accumulated durations to a list of doubles representing seconds.
	 *
	 * @return a list of durations in seconds
	 */
	public List<Double> durationsAsDouble() {
		return durationsAsDoubleStream()
				.boxed()
				.toList();
	}

	/**
	 * Calculates the average duration of the accumulated operations in seconds.
	 *
	 * @return the average duration in seconds
	 */
	public double average() {
		return durationsAsDoubleStream()
				.average()
				.orElse(DEFAULT_VALUE);
	}

	/**
	 * Finds the maximum duration of the accumulated operations in seconds.
	 *
	 * @return the maximum duration in seconds
	 */
	public double max() {
		return durationsAsDoubleStream()
				.max()
				.orElse(DEFAULT_VALUE);
	}

	/**
	 * Finds the minimum duration of the accumulated operations in seconds.
	 *
	 * @return the minimum duration in seconds
	 */
	public double min() {
		return durationsAsDoubleStream()
				.min()
				.orElse(DEFAULT_VALUE);
	}

	/**
	 * Returns the median duration of the accumulated operations in seconds.
	 *
	 * @return the median duration in seconds
	 */
	public double median() {
		return percentile(50.0);
	}

	/**
	 * Returns a stream with durations as {@link Double}.
	 *
	 * @return a stream with durations as double
	 */
	private DoubleStream durationsAsDoubleStream() {
		return getInformationList().stream()
				.mapToDouble(Temporals::toSeconds);
	}

	/**
	 * Calculates the specified percentile of the given durations.
	 *
	 * @param durations the list of durations.
	 * @param percentile the percentile to calculate (e.g., 95.0 for the 95th percentile).
	 * @return the percentile value in seconds.
	 */
	public static double percentile(final List<Double> durations, final double percentile) {
		if (durations.isEmpty()) {
			return DEFAULT_VALUE;
		}
		List<Double> sortedDurations = new ArrayList<>(durations);
		Collections.sort(sortedDurations);

		int index = (int) Math.ceil(percentile / 100.0 * durations.size());
		index = Math.clamp(index, 1, durations.size());
		return sortedDurations.get(index - 1);
	}

	/**
	 * Calculates the specified percentile of the accumulated durations.
	 *
	 * @param p the percentile to calculate (e.g., 95.0 for the 95th percentile).
	 * @return the percentile value in seconds.
	 */
	public double percentile(final double p) {
		return percentile(durationsAsDouble(), p);
	}

	/**
	 * Returns a string representation of the statistics if they have been built. Otherwise, returns an empty string.
	 *
	 * @return a string representation of the statistics or an empty string.
	 */
	@Override
	public String toString() {
		if (null != statistics) {
			return statistics.toString();
		}
		return "";
	}

	/**
	 * Builds the statistics for the accumulated durations. This method must be called explicitly to calculate and store
	 * statistics.
	 *
	 * @return returns the built statistics
	 */
	public Statistics buildStatistics() {
		this.statistics = new Statistics(this);
		return getStatistics();
	}

	/**
	 * Returns the statistics object containing calculated metrics.
	 *
	 * @return the statistics object.
	 * @throws IllegalStateException if {@link #buildStatistics()} has not been called.
	 */
	public Statistics getStatistics() {
		if (null == statistics) {
			throw new IllegalStateException("No statistics available. Build statistics by calling buildStatistics() first.");
		}
		return statistics;
	}

	/**
	 * Represents statistics calculated from the accumulated durations.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Statistics {

		/**
		 * The total number of requests measured.
		 */
		private final int requestCount;

		/**
		 * The average request time in seconds.
		 */
		private final double avgRequestTime;

		/**
		 * The maximum request time in seconds.
		 */
		private final double maxRequestTime;

		/**
		 * The 95th percentile request time in seconds.
		 */
		private final double p95RequestTime;

		/**
		 * The 90th percentile request time in seconds.
		 */
		private final double p90RequestTime;

		/**
		 * Constructs a new {@link Statistics} instance using the data from a {@link DurationAccumulator}.
		 *
		 * @param durationAccumulator the accumulator containing the durations to analyze
		 */
		public Statistics(final DurationAccumulator durationAccumulator) {
			requestCount = durationAccumulator.size();
			avgRequestTime = durationAccumulator.average();
			maxRequestTime = durationAccumulator.max();
			p95RequestTime = durationAccumulator.percentile(95.0);
			p90RequestTime = durationAccumulator.percentile(90.0);
		}

		/**
		 * Returns a string representation of the statistics.
		 *
		 * @return a formatted string containing the statistics
		 */
		@Override
		public String toString() {
			return "Statistics" + Strings.EOL
					+ "Count: " + requestCount + Strings.EOL
					+ "Avg time: " + Temporals.formatToSeconds(avgRequestTime) + "s" + Strings.EOL
					+ "p95 time: " + Temporals.formatToSeconds(p95RequestTime) + "s" + Strings.EOL
					+ "p90 time: " + Temporals.formatToSeconds(p90RequestTime) + "s" + Strings.EOL
					+ "Max time: " + Temporals.formatToSeconds(maxRequestTime) + "s";
		}

		/**
		 * Returns the total number of requests measured.
		 *
		 * @return the request count
		 */
		public int getRequestCount() {
			return requestCount;
		}

		/**
		 * Returns the average request time in seconds.
		 *
		 * @return the average request time
		 */
		public double getAvgRequestTime() {
			return avgRequestTime;
		}

		/**
		 * Returns the maximum request time in seconds.
		 *
		 * @return the maximum request time
		 */
		public double getMaxRequestTime() {
			return maxRequestTime;
		}

		/**
		 * Returns the 95th percentile request time in seconds.
		 *
		 * @return the 95th percentile request time
		 */
		public double getP95RequestTime() {
			return p95RequestTime;
		}

		/**
		 * Returns the 90th percentile request time in seconds.
		 *
		 * @return the 90th percentile request time
		 */
		public double getP90RequestTime() {
			return p90RequestTime;
		}
	}
}

package org.apiphany.meters;

import java.time.Duration;

/**
 * A specialized {@link Meter} that measures the duration of events.
 * <p>
 * Timers are used to record how long operations take to complete, typically to analyze latencies, throughput, or
 * performance characteristics of code paths. Each recorded {@link Duration} contributes to a set of statistics such as
 * total time, number of events, and distribution (depending on the underlying implementation).
 *
 * <h2>Usage example:</h2>
 *
 * <pre>{@code
 * MeterTimer requestTimer = registry.timer("http.requests");
 *
 * // record a known duration
 * requestTimer.record(Duration.ofMillis(250));
 *
 * // record the execution of a code block
 * long start = System.nanoTime();
 * try {
 * 	service.processRequest();
 * } finally {
 * 	Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
 * 	requestTimer.record(elapsed);
 * }
 * }</pre>
 *
 * @author Radu Sebastian LAZIN
 */
public interface MeterTimer extends Meter {

	/**
	 * Records a single event of the given {@link Duration}.
	 * <p>
	 * The duration must be non-negative. Passing a negative duration may result in an exception or undefined behavior
	 * depending on the underlying implementation.
	 *
	 * @param duration the duration to record (must not be negative)
	 */
	void record(Duration duration); // NOSONAR

}

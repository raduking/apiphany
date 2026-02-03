package org.apiphany.benchmark.lang;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmark for different hexadecimal formatting approaches.
 *
 * <ul>
 * <li>{@link String#join(String, CharSequence...)} building</li>
 * <li>String concatenation building</li>
 * </ul>
 *
 * <pre>
 * mvn jmh:benchmark -Pbenchmark -Djmh.benchmarks=org.apiphany.benchmark.lang.StringsEnvelopeBenchmark
 * </pre>
 *
 * Must be run from the current module folder (apiphany-core).
 * <p>
 * Note: make sure you run a clean build before executing the benchmarks to avoid any caching effects.
 *
 * <pre>
 * mvn clean test-compile
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
@State(Scope.Thread)
public class StringsEnvelopeBenchmark {

	private static final int LONG_STRING_LENGTH = 200;

	@State(Scope.Benchmark)
	public static class BenchmarkParams {

		@Param({ "short", "medium", "long" })
		public String category;
	}

	@State(Scope.Thread)
	public static class BenchmarkState {

		public String envelope;
		public String payload;

		@Setup
		public void setup(final BenchmarkParams params) {
			envelope = "----";
			switch (params.category) {
				case "short" -> payload = "hi";
				case "medium" -> payload = "Some string to be enveloped";
				case "long" -> payload = "x".repeat(LONG_STRING_LENGTH)
						+ "Some string to be enveloped"
						+ "x".repeat(LONG_STRING_LENGTH);
			}
		}
	}

	@Benchmark
	public String stringJoin(final BenchmarkState state, final Blackhole blackhole) {
		String result = String.join("", state.envelope, state.payload, state.envelope);
		blackhole.consume(result);
		return result;
	}

	@Benchmark
	public String stringConcatenation(final BenchmarkState state, final Blackhole blackhole) {
		String result = state.envelope + state.payload + state.envelope;
		blackhole.consume(result);
		return result;
	}
}

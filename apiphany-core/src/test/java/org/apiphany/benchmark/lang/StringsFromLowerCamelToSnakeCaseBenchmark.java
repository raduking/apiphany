package org.apiphany.benchmark.lang;

import java.util.concurrent.TimeUnit;

import org.morphix.lang.Case;
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
 * <li>{@link String#join(CharSequence, CharSequence...)} building</li>
 * <li>String concatenation building</li>
 * </ul>
 *
 * <pre>
 * mvn jmh:benchmark -Pbenchmark -Djmh.benchmarks=org.apiphany.benchmark.lang.StringsFromLowerCamelToSnakeCaseBenchmark
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
 * Results:
 *
 * <pre>
 * Benchmark                                        (category)  Mode  Cnt      Score     Error  Units
 * StringsFromLowerCamelToSnakeCaseBenchmark.fast        short  avgt    5    152.306 ±   7.504  ns/op
 * StringsFromLowerCamelToSnakeCaseBenchmark.fast       medium  avgt    5     27.880 ±  63.965  ns/op
 * StringsFromLowerCamelToSnakeCaseBenchmark.fast         long  avgt    5   3002.633 ±  92.184  ns/op
 * StringsFromLowerCamelToSnakeCaseBenchmark.regex       short  avgt    5    578.531 ±   3.637  ns/op
 * StringsFromLowerCamelToSnakeCaseBenchmark.regex      medium  avgt    5    259.230 ±  87.250  ns/op
 * StringsFromLowerCamelToSnakeCaseBenchmark.regex        long  avgt    5  10522.619 ± 102.016  ns/op
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
public class StringsFromLowerCamelToSnakeCaseBenchmark {

	private static final int LONG_STRING_LENGTH = 200;

	@State(Scope.Benchmark)
	public static class BenchmarkParams {

		@Param({ "short", "medium", "long" })
		public String category;
	}

	@State(Scope.Thread)
	public static class BenchmarkState {

		public String payload;

		@Setup
		public void setup(final BenchmarkParams params) {
			switch (params.category) {
				case "short" -> payload = "someCamelCase";
				case "medium" -> payload = "som";
				case "long" -> payload = "aB".repeat(LONG_STRING_LENGTH);
				default -> throw new IllegalArgumentException("Unknown category: " + params.category);
			}
		}
	}

	static String fromLowerCamelToSnakeCase(final String str) {
		return str
				.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
				.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
				.toLowerCase();
	}

	@Benchmark
	public String regex(final BenchmarkState state, final Blackhole blackhole) {
		String result = fromLowerCamelToSnakeCase(state.payload);
		blackhole.consume(result);
		return result;
	}

	@Benchmark
	public String fast(final BenchmarkState state, final Blackhole blackhole) {
		String result = Case.SNAKE.convert(state.payload);
		blackhole.consume(result);
		return result;
	}
}

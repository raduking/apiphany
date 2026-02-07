package org.apiphany.benchmark;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apiphany.RequestParameters;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Benchmark for {@link RequestParameters#from(Object)}.
 *
 * <ul>
 * <li>Map request parameters building</li>
 * <li>Object request parameters building</li>
 * </ul>
 *
 * <pre>
 * mvn jmh:benchmark -Pbenchmark -Djmh.benchmarks=org.apiphany.benchmark.RequestParametersFromBenchmark
 * </pre>
 *
 * Must be run from the current module folder (apiphany-core).
 *
 * @author Radu Sebastian LAZIN
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
@State(Scope.Thread)
public class RequestParametersFromBenchmark {

	private Map<A, Integer> mapParams;
	private Params objParams;

	@Setup
	public void setup() {
		mapParams = Map.of(
				new A("param1"), 1,
				new A("param2"), 2,
				new A("param3"), 3,
				new A("param4"), 4,
				new A("param5"), 5,
				new A("param6"), 6,
				new A("param7"), 7,
				new A("param8"), 8,
				new A("param9"), 9,
				new A("param10"), 10);
		objParams = new Params();
	}

	@Benchmark
	public Map<String, List<String>> fromMap() {
		return RequestParameters.from(mapParams);
	}

	@Benchmark
	public Map<String, List<String>> fromObject() {
		return RequestParameters.from(objParams);
	}

	static class A {

		final String name;

		A(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	static class Params {
		Integer param1 = 1;
		Integer param2 = 2;
		Integer param3 = 3;
		Integer param4 = 4;
		Integer param5 = 5;
		Integer param6 = 6;
		Integer param7 = 7;
		Integer param8 = 8;
		Integer param9 = 9;
		Integer param10 = 10;
	}
}

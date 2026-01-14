package org.apiphany.benchmark.lang;

import java.util.concurrent.TimeUnit;

import org.apiphany.lang.Hex;
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
 * Benchmark for different hexadecimal formatting approaches.
 *
 * <ul>
 * <li>char array building</li>
 * <li>SpringBuilder building</li>
 * </ul>
 *
 * <pre>
 * mvn jmh:benchmark -Pbenchmark -Djmh.benchmarks=org.apiphany.benchmark.lang.HexStringBytesBenchmark
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
public class HexStringBytesBenchmark {

	private byte[] bytes;

	@Setup
	public void setup() {
		bytes = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10 };
	}

	@Benchmark
	public String bytesCharArray() {
		return Hex.string(bytes, " ");
	}

	@Benchmark
	public String bytesStringBuilder() {
		return string(bytes, " ");
	}

	static String string(final byte[] bytes, final String separator) {
		if (bytes == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(Hex.string(b, separator));
		}
		return sb.toString().trim();
	}
}

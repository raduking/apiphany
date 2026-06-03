package org.apiphany.http;

import java.util.Map;
import java.util.Objects;

import org.apiphany.header.HeaderName;
import org.morphix.lang.Enums;
import org.morphix.lang.function.ToStringFunction;
import org.morphix.reflection.Constructors;

/**
 * Standard HTTP headers used for distributed tracing across different frameworks and platforms.
 * <p>
 * This enum provides type-safe access to common tracing header names, including:
 * <ul>
 * <li><b>B3 Propagation</b> (used by Spring Cloud Sleuth, Zipkin)</li>
 * <li><b>W3C TraceContext</b> (used by OpenTelemetry)</li>
 * <li><b>AWS X-Ray</b> (used by Amazon Web Services)</li>
 * </ul>
 * Example usage:
 *
 * <pre>{@code
 * String traceIdHeader = TracingHeaders.B3_TRACE_ID.getHeaderName();
 * }</pre>
 *
 * @author Radu Sebastian LAZIN
 */
public enum TracingHeader implements HeaderName {

	/**
	 * B3 propagation header for the trace ID (128-bit or 64-bit hex-encoded).
	 * <p>
	 * Used by:
	 * <ul>
	 * <li>Spring Cloud Sleuth</li>
	 * <li>Zipkin</li>
	 * </ul>
	 * Format: {@code X-B3-TraceId}
	 */
	B3_TRACE_ID(Name.X_B3_TRACE_ID),

	/**
	 * B3 propagation header for the span ID (64-bit hex-encoded).
	 * <p>
	 * Represents the current operation in a trace. Format: {@code X-B3-SpanId}
	 */
	B3_SPAN_ID(Name.X_B3_SPAN_ID),

	/**
	 * B3 propagation header for the parent span ID (64-bit hex-encoded).
	 * <p>
	 * Links child spans to their parent in hierarchical tracing. Format: {@code X-B3-ParentSpanId}
	 */
	B3_PARENT_SPAN_ID(Name.X_B3_PARENT_SPAN_ID),

	/**
	 * B3 propagation header indicating whether the trace is sampled (1 or 0).
	 * <p>
	 * Used for trace sampling decisions downstream. Format: {@code X-B3-Sampled}
	 */
	B3_SAMPLED(Name.X_B3_SAMPLED),

	/**
	 * W3C TraceContext header for trace parent (version-traceId-spanId-flags).
	 * <p>
	 * Used by OpenTelemetry and modern tracing systems. Format: {@code traceparent} Example:
	 * {@code 00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01}
	 */
	W3C_TRACEPARENT(Name.W3C_TRACEPARENT),

	/**
	 * W3C TraceContext header for additional trace state (key-value pairs).
	 * <p>
	 * Used for vendor-specific tracing metadata. Format: {@code tracestate} Example: {@code vendor1=value1,vendor2=value2}
	 */
	W3C_TRACESTATE(Name.W3C_TRACESTATE),

	/**
	 * AWS X-Ray trace ID header (includes trace ID, parent ID, and sampling decision).
	 * <p>
	 * Format: {@code X-Amzn-Trace-Id}
	 * </p>
	 * Example: {@code Root=1-5759e988-bd862e3fe1be46a994272793;Parent=53995c3f42cd8ad8;Sampled=1}
	 */
	AWS_XRAY_TRACE_ID(Name.X_AWS_XRAY_TRACE_ID);

	/**
	 * Namespace for the header name values.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Name {

		/**
		 * The B3 propagation header name for the trace ID (128-bit or 64-bit hex-encoded).
		 */
		public static final String X_B3_TRACE_ID = "X-B3-TraceId";

		/**
		 * The B3 propagation header name for the span ID (64-bit hex-encoded).
		 */
		public static final String X_B3_SPAN_ID = "X-B3-SpanId";

		/**
		 * The B3 propagation header name for the parent span ID (64-bit hex-encoded).
		 */
		public static final String X_B3_PARENT_SPAN_ID = "X-B3-ParentSpanId";

		/**
		 * The B3 propagation header name indicating whether the trace is sampled (1 or 0).
		 */
		public static final String X_B3_SAMPLED = "X-B3-Sampled";

		/**
		 * The W3C TraceContext header name for trace parent (version-traceId-spanId-flags).
		 */
		public static final String W3C_TRACEPARENT = "traceparent";

		/**
		 * The W3C TraceContext header name for additional trace state (key-value pairs).
		 */
		public static final String W3C_TRACESTATE = "tracestate";

		/**
		 * The AWS X-Ray trace ID header name (includes trace ID, parent ID, and sampling decision).
		 */
		public static final String X_AWS_XRAY_TRACE_ID = "X-Amzn-Trace-Id";

		/**
		 * Private constructor to prevent instantiation.
		 */
		private Name() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * The name map for easy from string implementation.
	 */
	private static final Map<String, TracingHeader> NAME_MAP = Enums.buildNameMap(values(), ToStringFunction.toLowerCase());

	/**
	 * The string representation of the header.
	 */
	private final String value;

	/**
	 * Constructs a tracing header enum with the actual HTTP header name.
	 *
	 * @param value The canonical name of the HTTP header (case-insensitive).
	 */
	TracingHeader(final String value) {
		this.value = value;
	}

	/**
	 * Returns a {@link TracingHeader} enum from a {@link String}.
	 *
	 * @param header tracing header as string
	 * @return a tracing header enum
	 */
	public static TracingHeader fromString(final String header) {
		return Enums.fromString(Objects.requireNonNull(header).toLowerCase(), NAME_MAP, values());
	}

	/**
	 * Returns the canonical HTTP header name for this tracing header.
	 *
	 * @return The header name as used in HTTP requests/responses (e.g., "X-B3-TraceId").
	 */
	@Override
	public String value() {
		return value;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return value();
	}
}

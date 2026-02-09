package org.apiphany.openapi;

import java.util.List;
import java.util.Map;

import org.apiphany.ParameterFunction;
import org.apiphany.RequestParameter;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Lists;
import org.morphix.lang.Enums;
import org.morphix.lang.function.ToStringFunction;
import org.morphix.reflection.Constructors;

/**
 * Enumeration representing the different ways of parameter serialization as defined in the OpenAPI Specification.
 *
 * @author Radu Sebastian LAZIN
 */
public enum MultiValueStrategy {

	/**
	 * The {@code "multi"} style allows multiple parameter instances instead of a single parameter with multiple values. For
	 * example, for a query parameter named {@code "color"} with values {@code "red"}, {@code "green"}, and {@code "blue"},
	 * the {@code "multi"} style would serialize it as {@code "color=red&color=green&color=blue"}.
	 */
	MULTI(Value.MULTI, "") {

		@Override
		public <T, U> ParameterFunction apply(final T name, final List<U> elements) {
			if (Lists.isEmpty(elements)) {
				return ParameterFunction.ignored();
			}
			return map -> {
				String parameterName = String.valueOf(name);
				List<String> parameterValues = RequestParameter.toValues(elements);
				ParameterFunction.insertInto(map, parameterName, parameterValues);
			};
		}
	},

	/**
	 * The {@code "csv"} style serializes multiple values as a comma-separated list. For example,
	 * {@code "color=red,green,blue"}.
	 */
	CSV(Value.CSV, ","),

	/**
	 * The {@code "ssv"} style serializes multiple values as a space-separated list. For example,
	 * {@code "color=red green blue"}.
	 */
	SSV(Value.SSV, " "),

	/**
	 * The {@code "tsv"} style serializes multiple values as a tab-separated list. For example,
	 * {@code "color=red\tgreen\tblue"}.
	 */
	TSV(Value.TSV, "\t"),

	/**
	 * The {@code "pipes"} style serializes multiple values as a pipe-separated list. For example,
	 * {@code "color=red|green|blue"}.
	 */
	PIPES(Value.PIPES, "|");

	/**
	 * The default multi-value strategy to use when no specific strategy is provided. This defaults to {@link #MULTI} as it
	 * is the most widely supported and straightforward approach for handling multiple values.
	 */
	public static final MultiValueStrategy DEFAULT = MULTI;

	/**
	 * Name space for the string representations of the multi-value strategies.
	 */
	public static class Value {

		/**
		 * The string representation for the {@code "multi"} style.
		 */
		public static final String MULTI = "multi";

		/**
		 * The string representation for the {@code "csv"} style.
		 */
		public static final String CSV = "csv";

		/**
		 * The string representation for the {@code "ssv"} style.
		 */
		public static final String SSV = "ssv";

		/**
		 * The string representation for the {@code "tsv"} style.
		 */
		public static final String TSV = "tsv";

		/**
		 * The string representation for the {@code "pipes"} style.
		 */
		public static final String PIPES = "pipes";

		/**
		 * Private constructor to prevent instantiation.
		 */
		private Value() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * The name map for easy from string implementation.
	 */
	private static final Map<String, MultiValueStrategy> NAME_MAP = Enums.buildNameMap(values(), ToStringFunction.toLowerCase());

	/**
	 * The string representation of the parameter style.
	 */
	private final String style;

	/**
	 * The separator used for this encoding style. For {@code "multi"}, the separator is empty string since it uses multiple
	 * instances.
	 */
	private final String separator;

	/**
	 * Constructs a {@link MultiValueStrategy} with the specified string representation.
	 *
	 * @param style the string representation of the parameter style
	 * @param separator the separator used for this encoding style
	 */
	MultiValueStrategy(final String style, final String separator) {
		this.style = style;
		this.separator = separator;
	}

	/**
	 * Returns the string representation of the parameter style.
	 *
	 * @return the string representation of the parameter style
	 */
	@Override
	public String toString() {
		return style();
	}

	/**
	 * Returns the string representation of the parameter style.
	 *
	 * @return the string representation of the parameter style
	 */
	public String style() {
		return style;
	}

	/**
	 * Returns the separator used for this encoding style.
	 *
	 * @return the separator used for this encoding style
	 */
	public String separator() {
		return separator;
	}

	/**
	 * Applies the multi-value encoding to the given parameter name and list of elements, returning a
	 * {@link ParameterFunction} that represents the encoded parameter.
	 *
	 * @param <T> the type of the parameter name
	 * @param <U> the type of the elements in the list
	 *
	 * @param name the parameter name
	 * @param elements the list of elements to encode
	 * @return a {@link ParameterFunction} representing the encoded parameter
	 */
	public <T, U> ParameterFunction apply(final T name, final List<U> elements) {
		if (Lists.isEmpty(elements)) {
			return ParameterFunction.ignored();
		}
		return map -> {
			String parameterName = String.valueOf(name);
			String parameterValue = String.join(separator, RequestParameter.toValues(elements));
			ParameterFunction.insertInto(map, parameterName, parameterValue);
		};
	}

	/**
	 * Determines the appropriate {@link MultiValueStrategy} based on the provided {@link QueryParam} annotation. If the
	 * annotation is null, defaults to {@link #MULTI}.
	 *
	 * @param queryParam the {@link QueryParam} annotation
	 * @return the corresponding {@link MultiValueStrategy}
	 */
	public static MultiValueStrategy from(final QueryParam queryParam) {
		if (null == queryParam) {
			return DEFAULT;
		}
		String strategy = queryParam.strategy();
		if (Strings.isNotBlank(strategy)) {
			return from(strategy);
		}
		return switch (queryParam.style()) {
			case FORM -> queryParam.mode() == ParameterMode.EXPLODE
					? MultiValueStrategy.MULTI
					: MultiValueStrategy.CSV;

			case SPACE_DELIMITED -> MultiValueStrategy.SSV;
			case PIPE_DELIMITED -> MultiValueStrategy.PIPES;

			default -> throw new UnsupportedOperationException(
					"Unsupported query parameter style: " + queryParam.style());
		};
	}

	/**
	 * Determines the appropriate {@link MultiValueStrategy} based on the provided string representation. The string is
	 * matched against the known styles in a case-insensitive manner. If the string does not match any known style it
	 * defaults to {@link #MULTI}.
	 *
	 * @param style the string representation of the parameter style
	 * @return the corresponding {@link MultiValueStrategy}
	 */
	public static MultiValueStrategy from(final String style) {
		return Enums.from(style.toLowerCase(), NAME_MAP, () -> MULTI);
	}
}

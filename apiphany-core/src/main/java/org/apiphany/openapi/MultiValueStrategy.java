package org.apiphany.openapi;

import java.util.List;

import org.apiphany.ParameterFunction;
import org.apiphany.RequestParameter;

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
	MULTI("multi", "") {

		@Override
		public <T, U> ParameterFunction apply(final T name, final List<U> elements) {
			return ParameterFunction.parameter(name, elements);
		}
	},

	/**
	 * The {@code "csv"} style serializes multiple values as a comma-separated list. For example,
	 * {@code "color=red,green,blue"}.
	 */
	CSV("csv", ","),

	/**
	 * The {@code "ssv"} style serializes multiple values as a space-separated list. For example,
	 * {@code "color=red green blue"}.
	 */
	SSV("ssv", " "),

	/**
	 * The {@code "tsv"} style serializes multiple values as a tab-separated list. For example,
	 * {@code "color=red\tgreen\tblue"}.
	 */
	TSV("tsv", "\t"),

	/**
	 * The {@code "pipes"} style serializes multiple values as a pipe-separated list. For example,
	 * {@code "color=red|green|blue"}.
	 */
	PIPES("pipes", "|");

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
		String value = String.join(separator, RequestParameter.toValues(elements));
		return ParameterFunction.parameter(String.valueOf(name), value);
	}
}

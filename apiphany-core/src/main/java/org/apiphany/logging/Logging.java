package org.apiphany.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import org.apiphany.lang.Strings;
import org.apiphany.security.MessageDigestAlgorithm;
import org.morphix.reflection.Constructors;

/**
 * Provides utilities for building safe diagnostic descriptions of an input.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Logging {

	/**
	 * Defines the logging modes for building safe diagnostic descriptions.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public enum Mode {

		/**
		 * Do not log input content.
		 */
		NONE,

		/**
		 * Log safe metadata (type + length + hash) instead of full input.
		 */
		METADATA,

		/**
		 * Log the full input as-is.
		 */
		FULL;
	}

	/**
	 * Defines the types of information that can be included in a diagnostic description of an input.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public enum Include {

		/**
		 * Includes the length of the input value in the diagnostic description. For strings, this is the number of characters;
		 * for byte arrays, this is the number of bytes. For other types, this information is unavailable.
		 */
		LENGTH {
			@Override
			public String getValue(final Object obj) {
				return String.valueOf(switch (obj) {
					case String string -> string.length();
					case byte[] bytes -> bytes.length;
					default -> Default.UNAVAILABLE;
				});
			}
		},

		/**
		 * Includes a hash of the input value in the diagnostic description. For strings and byte arrays, this is a truncated
		 * SHA-256 hash. For other types, this is the identity hash code of the object.
		 */
		HASH {
			@Override
			public String getValue(final Object obj) {
				MessageDigestAlgorithm sha256 = MessageDigestAlgorithm.SHA256;
				return switch (obj) {
					case String string -> sha256.hash(string, Default.HASH_BYTES);
					case byte[] bytes -> sha256.hash(bytes, Default.HASH_BYTES);
					default -> Integer.toHexString(System.identityHashCode(obj));
				};
			}
		},

		/**
		 * Includes a preview of the input value in the diagnostic description. For strings and byte arrays, this is a truncated
		 * version of the value itself. For other types, this information is unavailable.
		 */
		PREVIEW {
			@Override
			public String getValue(final Object obj) {
				return switch (obj) {
					case String string -> Strings.preview(string, Default.PREVIEW_LENGTH);
					case byte[] bytes -> Strings.preview(bytes, Default.PREVIEW_LENGTH);
					default -> Default.UNAVAILABLE;
				};
			}
		};

		/**
		 * Constants for special values used in the logging utilities.
		 */
		public static class Default {

			/**
			 * Unavailable information placeholder. Used when a specific type of information cannot be extracted from the input
			 * value.
			 */
			public static final String UNAVAILABLE = "unavailable";

			/**
			 * The number of bytes to include in the hash representation of the input value. This is a truncated version of the full
			 * hash to balance between uniqueness and readability in diagnostic descriptions.
			 */
			public static final int HASH_BYTES = 8;

			/**
			 * The number of characters or bytes to include in the preview representation of the input value. This is a truncated
			 * version of the full value to balance between informativeness and readability in diagnostic descriptions.
			 */
			public static final int PREVIEW_LENGTH = 512;

			/**
			 * Private constructor to prevent instantiation of the utility class.
			 */
			private Default() {
				throw Constructors.unsupportedOperationException();
			}
		}

		/**
		 * Extracts the specified type of information from the input value and formats it as a string for inclusion in the
		 * diagnostic description.
		 *
		 * @param obj the input value from which to extract the specified type of information
		 * @return a string representation of the specified type of information extracted from the input value, formatted for
		 * inclusion in the diagnostic description
		 */
		public abstract String getValue(Object obj);

		/**
		 * The starting string for the diagnostic information, which is the lowercase name of the enum constant followed by an
		 * equals sign.
		 *
		 * @return the starting string for the diagnostic information
		 */
		protected String prefix() {
			return name().toLowerCase();
		}

		/**
		 * Creates a list of non-null Include instances from the provided varargs array. This method filters out any null values
		 * from the input array and returns a list containing only the valid Include instances.
		 *
		 * @param args the varargs array of Include instances, which may contain null values
		 * @return a list of non-null Include instances extracted from the input array
		 * @throws NullPointerException if the input array is null
		 */
		public static List<Include> of(final Include... args) {
			Objects.requireNonNull(args, "args must not be null");
			int size = args.length;
			List<Include> includes = new ArrayList<>(size);
			int i = 0;
			while (i < size) {
				Include arg = args[i];
				if (null != arg) {
					includes.add(arg);
				}
				++i;
			}
			return includes;
		}

		/**
		 * Conditionally includes an Include instance based on the result of a BooleanSupplier. If the supplier returns true,
		 * the specified Include instance is returned; otherwise, null is returned. This allows for dynamic inclusion of
		 * diagnostic information based on runtime conditions.
		 *
		 * @param includeOrNull a BooleanSupplier that determines whether to include the specified Include instance
		 * @param include the Include instance to conditionally include in the diagnostic description
		 * @return the specified Include instance if the supplier returns true, or null if the supplier returns false
		 */
		public static Include when(final BooleanSupplier includeOrNull, final Include include) {
			return includeOrNull.getAsBoolean() ? include : null;
		}
	}

	/**
	 * Builds a safe diagnostic description for an input value.
	 *
	 * @param obj the input value to describe
	 * @param format the logging format
	 * @param includes the types of information to include in the description
	 * @return a safe diagnostic description of the input value
	 */
	static String describeInput(final Object obj, final LoggingFormat format, final Include... includes) {
		return describeInput(obj, format, Include.of(includes));
	}

	/**
	 * Builds a safe diagnostic description for an input value.
	 *
	 * @param obj the input object to describe
	 * @param format the logging format
	 * @param includes the list of Include instances specifying the types of information to include in the description
	 * @return a safe diagnostic description of the input value
	 */
	static String describeInput(final Object obj, final LoggingFormat format, final List<Include> includes) {
		// currently only custom format is supported, but this allows for future extension to other formats if needed
		return switch (format) {
			default -> Custom.describeInput(obj, includes);
		};
	}

	/**
	 * Provides a custom implementation for building safe diagnostic descriptions of input values. This implementation
	 * constructs a description that includes the class name of the input value and the specified types of information
	 * (length, hash, preview) based on the provided Include instances.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	interface Custom {

		/**
		 * Builds a safe diagnostic description for an input value.
		 *
		 * @param obj the input object to describe
		 * @param includes the list of Include instances specifying the types of information to include in the description
		 * @return a safe diagnostic description of the input value
		 */
		static String describeInput(final Object obj, final List<Include> includes) {
			if (null == obj) {
				return Objects.toString(obj);
			}
			StringBuilder sb = new StringBuilder();
			sb.append(obj.getClass().getTypeName());
			sb.append("(");
			boolean first = true;
			int length = includes.size();
			for (int i = 0; i < length; ++i) {
				Include include = includes.get(i);
				String includeValue = include.getValue(obj);
				if (Include.Default.UNAVAILABLE.equals(includeValue)) {
					continue;
				}
				if (!first) {
					sb.append(", ");
				} else {
					first = false;
				}
				sb.append(include.prefix());
				sb.append("=");
				sb.append(includeValue);
			}
			sb.append(")");
			return sb.toString();
		}
	}
}

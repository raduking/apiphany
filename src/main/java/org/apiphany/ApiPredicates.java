package org.apiphany;

import java.util.List;
import java.util.function.Predicate;

import org.morphix.reflection.Constructors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Predicates for request queries.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiPredicates {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiPredicates.class);

	/**
	 * Message logged for list predicates.
	 */
	private static final String LOG_EXPECTED_NON_EMPTY_LIST = "Expected non empty list.";

	/**
	 * Predicate to be used only response must be a {@link java.util.List} and must not be empty
	 *
	 * @param <T> type of object to test
	 * @return true predicate
	 */
	public static <T> Predicate<T> responseListIsNotEmpty() {
		return responseList -> {
			boolean condition = List.class.isAssignableFrom(responseList.getClass()) &&
					!((List<?>) responseList).isEmpty();
			if (!condition) {
				LOGGER.info(LOG_EXPECTED_NON_EMPTY_LIST);
			}
			return condition;
		};
	}

	/**
	 * Predicate to be used only response must be a {@link java.util.List} and must have the given size
	 *
	 * @param <T> type of object to test
	 *
	 * @param listSize the list size
	 * @return true predicate
	 */
	public static <T> Predicate<T> responseListHasSize(final int listSize) {
		return responseList -> {
			boolean condition = List.class.isAssignableFrom(responseList.getClass()) &&
					((List<?>) responseList).size() == listSize;
			if (!condition) {
				LOGGER.info(LOG_EXPECTED_NON_EMPTY_LIST);
			}
			return condition;
		};
	}

	/**
	 * Predicate to be used only when the response is a {@link ApiPage}. The pagination content must have the given size
	 *
	 * @param <T> type of object to test
	 *
	 * @param pageSize the page size
	 * @return true predicate
	 */
	public static <T> Predicate<T> responsePageHasSizeGreaterThan(final int pageSize) {
		return responsePage -> {
			boolean condition = ApiPage.class.isAssignableFrom(responsePage.getClass()) &&
					((ApiPage<?>) responsePage).getContent().size() >= pageSize;
			if (!condition) {
				LOGGER.info("Expected pagination with size >= {}", pageSize);
			}
			return condition;
		};
	}

	/**
	 * Returns a predicate that tests non-null object.
	 *
	 * @param <T> object type which should be non-null
	 * @return predicate that tests non-null object
	 */
	public static <T> Predicate<T> nonNullResponse() {
		return response -> {
			boolean condition = null != response;
			if (!condition) {
				LOGGER.info("Expected non null response.");
			}
			return condition;
		};
	}

	/**
	 * Predicate to be used only when a response is expected.
	 *
	 * @param <T> type of object to test
	 * @return true predicate
	 */
	public static <T> Predicate<T> hasResponse() {
		return t -> true;
	}

	/**
	 * Predicate to be used only response must be a {@link java.util.List} and must not be empty
	 *
	 * @param <T> type of object to test
	 * @return true predicate
	 */
	public static <T> Predicate<T> responsePageIsNotEmpty() {
		return responsePage -> {
			boolean condition = ApiPage.class.isAssignableFrom(responsePage.getClass()) &&
					!((ApiPage<?>) responsePage).getContent().isEmpty();
			if (!condition) {
				LOGGER.info(LOG_EXPECTED_NON_EMPTY_LIST);
			}
			return condition;
		};
	}

	/**
	 * Private constructor.
	 */
	private ApiPredicates() {
		throw Constructors.unsupportedOperationException();
	}

}

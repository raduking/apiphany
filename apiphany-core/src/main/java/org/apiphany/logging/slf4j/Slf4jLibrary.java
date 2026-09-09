package org.apiphany.logging.slf4j;

import org.apiphany.logging.DiagnosticContext;
import org.morphix.reflection.Constructors;
import org.morphix.runtime.OptionalLibrary;

/**
 * Utility class for SLF4J library related operations.
 * <p>
 * This class provides information about the presence of the SLF4J library in the classpath and should not have any
 * SLF4J-specific dependencies itself.
 *
 * @author Radu Sebastian LAZIN
 */
public class Slf4jLibrary {

	/**
	 * The SLF4J MDC class name.
	 */
	private static final String SLF4J_MDC_CLASS_NAME = "org.slf4j.MDC";

	/**
	 * A {@link OptionalLibrary} indicating whether the SLF4J library is present on the classpath, along with the
	 * {@link DiagnosticContext} implementation class to use if it is available.
	 * <p>
	 * The {@link OptionalLibrary#isPresent()} value is {@code true} if SLF4J is detected, {@code false} otherwise.
	 */
	public static final OptionalLibrary<? extends DiagnosticContext> DESCRIPTOR =
			OptionalLibrary.of(
					SLF4J_MDC_CLASS_NAME,
					Slf4jDiagnosticContext.class);

	/**
	 * Checks if the SLF4J library is present in the classpath.
	 *
	 * @return {@code true} if the SLF4J library is present, {@code false} otherwise
	 */
	public static boolean isPresent() {
		return DESCRIPTOR.isPresent();
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Slf4jLibrary() {
		throw Constructors.unsupportedOperationException();
	}
}

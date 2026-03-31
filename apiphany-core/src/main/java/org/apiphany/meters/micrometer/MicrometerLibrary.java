package org.apiphany.meters.micrometer;

import org.apiphany.meters.MeterFactory;
import org.morphix.reflection.Constructors;
import org.morphix.runtime.OptionalLibrary;

/**
 * Utility class for Micrometer library related operations.
 * <p>
 * This class provides information about the presence of the Micrometer library in the classpath and should not have any
 * Micrometer-specific dependencies itself.
 *
 * @author Radu Sebastian LAZIN
 */
public class MicrometerLibrary {

	/**
	 * The Micrometer meter class name.
	 */
	private static final String MICROMETER_METER_CLASS_NAME = "io.micrometer.core.instrument.Meter";

	/**
	 * A {@link OptionalLibrary} indicating whether the Micrometer library is present on the classpath, along with the
	 * {@link MeterFactory} implementation class to use if it is available.
	 * <p>
	 * The {@link OptionalLibrary#isPresent()} value is {@code true} if Micrometer is detected, {@code false} otherwise.
	 */
	public static final OptionalLibrary<? extends MeterFactory> DESCRIPTOR =
			OptionalLibrary.of(
					MICROMETER_METER_CLASS_NAME,
					MicrometerFactory.class);

	/**
	 * Private constructor to prevent instantiation.
	 */
	private MicrometerLibrary() {
		throw Constructors.unsupportedOperationException();
	}
}

package org.apiphany.security.http;

import org.apiphany.security.HeaderSensitivity;
import org.apiphany.security.ParameterSensitivity;

/**
 * Interface for defining HTTP sensitivity rules.
 *
 * @author Radu Sebastian LAZIN
 */
public interface HttpSensitivity extends HeaderSensitivity, ParameterSensitivity {

	// empty interface to combine both header and parameter sensitivity rules
}

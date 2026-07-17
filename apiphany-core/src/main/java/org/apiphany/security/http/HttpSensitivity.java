package org.apiphany.security.http;

import org.apiphany.security.BodySensitivity;
import org.apiphany.security.HeaderSensitivity;
import org.apiphany.security.ParameterSensitivity;

/**
 * Interface for defining HTTP sensitivity rules.
 *
 * @author Radu Sebastian LAZIN
 */
public interface HttpSensitivity extends HeaderSensitivity, ParameterSensitivity, BodySensitivity {

	// empty interface to combine header, parameter, and body sensitivity rules
}

package org.apiphany.client;

/**
 * Represents the customization options for a client.
 * <p>
 * The available customization options are:
 * <ul>
 * <li>{@link #DEFAULT}: Use the default customizations provided by the client implementation.</li>
 * <li>{@link #NONE}: No customization applied to the client.</li>
 * </ul>
 * </p>
 * Currently only these two options are supported and are only used in advanced scenarios.
 *
 * @author Radu Sebastian LAZIN
 */
public enum ClientCustomization {

	/**
	 * Use the default customizations provided by the client implementation.
	 */
	DEFAULT,

	/**
	 * No customization applied to the client.
	 */
	NONE;
}

package org.apiphany.security.ssl;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;

/**
 * Certificate store information class to configure Java key/trust stores. This class holds information about
 * certificate store location, type, password, and loading configuration.
 *
 * @author Radu Sebastian LAZIN
 */
public class StoreInfo {

	/**
	 * Constant representing an unknown certificate store location.
	 */
	public static final String UNKNOWN_LOCATION = "<unknown-location>";

	/**
	 * The file system path or URL to the certificate store.
	 */
	private String location;

	/**
	 * The type of the certificate store (e.g., "JKS", "PKCS12").
	 */
	private String type;

	/**
	 * The key store factory algorithm. If this value is not set, the default algorithm will be used.
	 */
	private String algorithm;

	/**
	 * The password to access the certificate store.
	 */
	private char[] password;

	/**
	 * Flag indicating whether the SSL certificate should be loaded from an external source (true) or from the jar file
	 * (false).
	 */
	private Boolean external;

	/**
	 * Default constructor.
	 */
	public StoreInfo() {
		// empty
	}

	/**
	 * Returns a JSON string representation of this object.
	 *
	 * @return a JSON string representation of this object
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the password for accessing the certificate store.
	 *
	 * @return the password as a character array
	 */
	public char[] getPassword() {
		return password;
	}

	/**
	 * Sets the password for accessing the certificate store.
	 *
	 * @param password the password as a character array
	 */
	public void setPassword(final char[] password) {
		this.password = password;
	}

	/**
	 * Returns the location of the certificate store.
	 *
	 * @return the file system path or URL to the certificate store
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Returns the display location of the certificate store. Returns {@link #UNKNOWN_LOCATION} if the location is not set.
	 *
	 * @return the display location string
	 */
	public String getDisplayLocation() {
		return Strings.isNotEmpty(getLocation()) ? getLocation() : UNKNOWN_LOCATION;
	}

	/**
	 * Sets the location of the certificate store.
	 *
	 * @param path the file system path or URL to the certificate store
	 */
	public void setLocation(final String path) {
		this.location = path;
	}

	/**
	 * Returns the type of the certificate store.
	 *
	 * @return the certificate store type (e.g., "JKS", "PKCS12")
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type of the certificate store.
	 *
	 * @param type the certificate store type (e.g., "JKS", "PKCS12")
	 */
	public void setType(final String type) {
		this.type = type;
	}

	/**
	 * Returns the algorithm.
	 *
	 * @return the algorithm
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * Sets the algorithm, if this value is not set, the default algorithm will be used.
	 *
	 * @param algorithm the algorithm to set
	 */
	public void setAlgorithm(final String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * Checks if the certificate should be loaded from an external source.
	 *
	 * @return true if certificates should be loaded from an external source, false if they should be loaded from the jar file
	 */
	public boolean isExternal() {
		return Boolean.TRUE.equals(external);
	}

	/**
	 * Returns the external source loading configuration.
	 *
	 * @return Boolean.TRUE if loading from an external source, Boolean.FALSE if loading from the jar file, null if not specified
	 */
	public Boolean getExternal() {
		return external;
	}

	/**
	 * Sets whether certificates should be loaded from an external source.
	 *
	 * @param external Boolean.TRUE to load from external source, Boolean.FALSE to load from jar file, null to leave
	 *     unspecified
	 */
	public void setExternal(final Boolean external) {
		this.external = external;
	}
}

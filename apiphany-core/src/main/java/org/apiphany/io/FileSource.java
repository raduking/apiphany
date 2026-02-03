package org.apiphany.io;

/**
 * Enumeration representing the possible locations / sources from which a resource can be loaded.
 *
 * @author Radu Sebastian LAZIN
 */
public enum FileSource {

	/**
	 * The resource is packaged inside the classpath (JAR, WAR, module path…).
	 */
	CLASS_PATH,

	/**
	 * The resource is located on the local file system (absolute or relative path).
	 */
	FILE_SYSTEM;

	/**
	 * Checks if the file source is external (i.e., file system).
	 *
	 * @return true if the source is FILE_SYSTEM, false otherwise
	 */
	public boolean isExternal() {
		return this == FILE_SYSTEM;
	}
}

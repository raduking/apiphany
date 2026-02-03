package org.apiphany.io;

/**
 * Enumeration representing different file sources.
 *
 * @author Radu Sebastian LAZIN
 */
public enum FileSource {

	/**
	 * Indicates that the file is located in the class path.
	 */
	CLASS_PATH,

	/**
	 * Indicates that the file is located in the file system.
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

package org.apiphany.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Enumeration representing the possible locations / sources from which a resource can be loaded.
 *
 * @author Radu Sebastian LAZIN
 */
public enum ResourceLocation {

	/**
	 * The resource is packaged inside the classpath (JAR, WAR, module path…).
	 */
	CLASS_PATH() {

		/**
		 * Opens an input stream to the resource located in the classpath.
		 *
		 * @param location the location of the resource within the classpath
		 * @return an InputStream to read the resource
		 * @throws IOException if an I/O error occurs while opening the stream
		 */
		@Override
		public InputStream open(final String location) throws IOException {
			return Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
		}
	},

	/**
	 * The resource is located on the local file system (absolute or relative path).
	 */
	FILE_SYSTEM() {

		/**
		 * Opens an input stream to the resource located on the file system.
		 *
		 * @param location the file system path of the resource
		 * @return an InputStream to read the resource
		 * @throws IOException if an I/O error occurs while opening the stream
		 */
		@Override
		public InputStream open(final String location) throws IOException {
			return new FileInputStream(location);
		}
	};

	/**
	 * Opens an input stream to the resource at the specified location.
	 *
	 * @param location the location of the resource
	 * @return an InputStream to read the resource
	 * @throws IOException if an I/O error occurs while opening the stream
	 */
	public abstract InputStream open(String location) throws IOException;
}

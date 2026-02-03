package org.apiphany.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

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
			InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
			if (null == inputStream) {
				throw new FileNotFoundException("Classpath resource not found: " + location);
			}
			return inputStream;
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

	/**
	 * Determines the ResourceLocation based on the given Path.
	 *
	 * @param path the path to evaluate
	 * @return FILE_SYSTEM if the path is absolute, otherwise CLASS_PATH
	 */
	public static ResourceLocation ofPath(final Path path) {
		return path.isAbsolute() ? FILE_SYSTEM : CLASS_PATH;
	}

	/**
	 * Determines the ResourceLocation based on the given Path as a {@link String}.
	 *
	 * @param path the path to evaluate
	 * @return FILE_SYSTEM if the path is absolute, otherwise CLASS_PATH
	 */
	public static ResourceLocation ofPath(final String path) {
		return ofPath(Path.of(path));
	}
}

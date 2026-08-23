package org.apiphany.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.morphix.reflection.Constructors;

/**
 * Internal helper for resolving classpath resources to real filesystem paths.
 *
 * @author Radu Sebastian LAZIN
 */
final class TestResources {

	/**
	 * Cache of classpath resources extracted from jars so they can be used as real files.
	 */
	private static final Map<String, Path> EXTRACTED_RESOURCES = new ConcurrentHashMap<>();

	/**
	 * Private constructor.
	 */
	private TestResources() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Resolves a classpath resource to a real filesystem path. When the resource is packaged inside a jar it is extracted
	 * once to a temporary location since some APIs need an actual file on the default filesystem.
	 *
	 * @param context the class whose classloader resolves the resource
	 * @param name the classpath resource name, with or without a leading slash
	 * @return the filesystem path of the resource
	 */
	static Path path(final Class<?> context, final String name) {
		URL url = context.getResource("/" + stripLeadingSlash(name));
		if (null == url) {
			throw new IllegalStateException("Resource not found: " + name);
		}
		try {
			URI uri = url.toURI();
			if ("file".equals(uri.getScheme())) {
				return Paths.get(uri);
			}
			return EXTRACTED_RESOURCES.computeIfAbsent(stripLeadingSlash(name), n -> extractToTempFile(context, n));
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Invalid resource URI: " + url, e);
		}
	}

	/**
	 * Extracts the given classpath resource to a temporary file.
	 *
	 * @param context the class whose classloader resolves the resource
	 * @param name the normalized classpath resource name
	 * @return the temporary file containing the resource content
	 */
	private static Path extractToTempFile(final Class<?> context, final String name) {
		try (InputStream input = context.getResourceAsStream("/" + name)) {
			if (null == input) {
				throw new IllegalStateException("Resource not found: " + name);
			}
			Path tempFile = Files.createTempFile("apiphany-", "-" + Paths.get(name).getFileName(), secureFileAttributes());
			Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
			tempFile.toFile().deleteOnExit();
			return tempFile;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Returns owner-only read/write file attributes for newly created temporary files. On non-POSIX filesystems it returns
	 * no attributes since the default permissions are already restrictive.
	 *
	 * @return the file attributes for secure temporary file creation
	 */
	private static FileAttribute<?>[] secureFileAttributes() {
		try {
			return new FileAttribute<?>[] {
					PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))
			};
		} catch (UnsupportedOperationException e) {
			return new FileAttribute<?>[0];
		}
	}

	/**
	 * Strips the leading slash from a resource name if present.
	 *
	 * @param name the resource name
	 * @return the resource name without a leading slash
	 */
	private static String stripLeadingSlash(final String name) {
		return name.startsWith("/") ? name.substring(1) : name;
	}
}

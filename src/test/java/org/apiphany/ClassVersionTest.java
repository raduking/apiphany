package org.apiphany;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Classes;

/**
 * Test class to verify that all compiled class files have the expected class file version.
 *
 * @author Radu Sebastian LAZIN
 */
class ClassVersionTest {

	private static final String TARGET_CLASSES = "target/classes";

	private static final int JAVA_VERSION = 21;

	private static final int MAGIC = 0xCAFEBABE;
	private static final int MAGIC_VERSION = 44;

	private static int javaVersionToMajor(final int javaVersion) {
		// class file major = 44 + java version
		return MAGIC_VERSION + javaVersion;
	}

	@Test
	void shouldHaveTheCorrectClassVersion() throws Exception {
		Path classesDir = Path.of(TARGET_CLASSES); // compiled classes output
		int expectedMajor = javaVersionToMajor(JAVA_VERSION);

		try (Stream<Path> paths = Files.walk(classesDir)) {
			paths.filter(path -> path.toString().endsWith(Classes.Scan.CLASS_FILE_EXTENSION))
					.forEach(path -> {
						try (DataInputStream in = new DataInputStream(new FileInputStream(path.toFile()))) {
							int magic = in.readInt();
							if (magic != MAGIC) {
								throw new IllegalStateException(path + " is not a valid class file");
							}
							@SuppressWarnings("unused")
							int minor = in.readUnsignedShort();
							int major = in.readUnsignedShort();
							assertEquals(expectedMajor, major, path + " has wrong classfile version");
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					});
		}
	}
}

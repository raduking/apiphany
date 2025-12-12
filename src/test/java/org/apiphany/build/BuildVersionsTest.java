package org.apiphany.build;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.Stream;

import org.apiphany.utils.Tests;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.JavaClassFile;

/**
 * Test class to verify that all compiled class files have the expected class file version.
 *
 * @author Radu Sebastian LAZIN
 */
class BuildVersionsTest {

	private static final String TARGET = "target";
	private static final String TARGET_CLASSES = TARGET + "/classes";

	private static final String PROPERTY_JAVA_VERSION = "java.version";
	private static final String PROPERTY_MAVEN_COMPILER_TARGET = "maven.compiler.target";

	private static final String MAVEN_PROPERTIES = TARGET + "/maven.properties";

	private static final Properties PROPERTIES = Tests.loadProperties(MAVEN_PROPERTIES);

	@Test
	void shouldHaveMavenCompilerTargetProperty() {
		String target = PROPERTIES.getProperty(PROPERTY_MAVEN_COMPILER_TARGET);

		assertThat(target, not(emptyOrNullString()));
	}

	@Test
	void shouldHaveTheSameJavaVersionAsMavenCompilerTarget() {
		String javaVersion = PROPERTIES.getProperty(PROPERTY_JAVA_VERSION);
		String target = PROPERTIES.getProperty(PROPERTY_MAVEN_COMPILER_TARGET);

		assertEquals(target, javaVersion.startsWith("1.") ? javaVersion.substring(2, 3) : javaVersion.split("\\.")[0]);
	}

	@Test
	void shouldHaveTheCorrectClassVersion() throws Exception {
		int javaVersion = Integer.parseInt(PROPERTIES.getProperty(PROPERTY_MAVEN_COMPILER_TARGET));

		Path classesDir = Path.of(TARGET_CLASSES);
		int expectedMajor = JavaClassFile.Version.fromJavaVersion(javaVersion).major();

		try (Stream<Path> paths = Files.walk(classesDir)) {
			paths.filter(path -> path.toString().endsWith(JavaClassFile.EXTENSION))
					.forEach(path -> {
						try (DataInputStream in = new DataInputStream(new FileInputStream(path.toFile()))) {
							int magic = in.readInt();
							if (magic != JavaClassFile.CAFEBABE) {
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

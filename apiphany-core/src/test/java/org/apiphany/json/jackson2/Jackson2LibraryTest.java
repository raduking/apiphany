package org.apiphany.json.jackson2;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apiphany.json.JsonBuilder;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link Jackson2Library}.
 *
 * @author Radu Sebastian LAZIN
 */
class Jackson2LibraryTest {

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(Jackson2Library.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldHaveDescriptor() {
		// because both Jackson 2 and Jackson 3 libraries are present in the classpath, the descriptor should be present
		assertThat(Jackson2Library.isPresent(), equalTo(true));
	}

	@Test
	void shouldDetectJdk8ModuleWhenPresent() {
		assertThat(Jackson2Library.isJdk8ModulePresent(), equalTo(true));
	}

	@Test
	void shouldCreateJdk8ModuleByName() {
		Object module = Jackson2Library.newJdk8Module();

		assertThat(module.getClass().getName(), equalTo(Jackson2Library.jdk8ModuleClassName()));
	}

	@Test
	void shouldNotReferenceJdk8ModuleAsAClassDependency() throws IOException {
		String classResource = Jackson2Library.class.getName().replace('.', '/') + ".class";
		try (InputStream inputStream = Jackson2Library.class.getClassLoader().getResourceAsStream(classResource)) {
			String classBytes = new String(inputStream.readAllBytes(), StandardCharsets.ISO_8859_1);

			assertThat(classBytes.contains("com/fasterxml/jackson/datatype/jdk8/Jdk8Module"), equalTo(false));
		}
	}

	@Test
	void shouldReturnSingletonInstanceOfJsonBuilder() {
		JsonBuilder jsonBuilder1 = Jackson2Library.DESCRIPTOR.getSpecificInstance();
		JsonBuilder jsonBuilder2 = Jackson2JsonBuilder.instance();

		assertThat(jsonBuilder1, equalTo(jsonBuilder2));
	}
}

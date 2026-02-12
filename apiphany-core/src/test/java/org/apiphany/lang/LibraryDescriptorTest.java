package org.apiphany.lang;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link LibraryDescriptor}.
 *
 * @author Radu Sebastian LAZIN
 */
class LibraryDescriptorTest {

	static class TestType {
		// empty
	}

	@Test
	void shouldIndicateLibraryIsPresent() {
		LibraryDescriptor<TestType> descriptor = LibraryDescriptor.present(TestType.class);

		assertThat(descriptor.isLibraryPresent(), is(true));
	}

	@Test
	void shouldIndicateLibraryIsNotPresent() {
		LibraryDescriptor<TestType> descriptor = LibraryDescriptor.notPresent(TestType.class);

		assertThat(descriptor.isLibraryPresent(), is(false));
	}

	@Test
	void shouldReturnSpecificClass() {
		LibraryDescriptor<TestType> descriptor = LibraryDescriptor.present(TestType.class);

		assertThat(descriptor.getSpecificClass(), sameInstance(TestType.class));
	}

	@Test
	void shouldFailWhenSpecificClassIsNull() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> LibraryDescriptor.present(null));

		assertThat(exception.getMessage(), is("specificClass must not be null"));
	}

	@Test
	void shouldReturnTrueForPresentClassDescriptor() {
		LibraryDescriptor<TestType> descriptor = LibraryDescriptor.of(TestType.class.getName(), TestType.class);

		assertThat(descriptor.isLibraryPresent(), is(true));
	}

	@Test
	void shouldReturnFalseForNotPresentClassDescriptor() {
		LibraryDescriptor<TestType> descriptor = LibraryDescriptor.of(TestType.class.getName() + "$NonExistentClass", TestType.class);

		assertThat(descriptor.isLibraryPresent(), is(false));
	}
}

package org.apiphany.lang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link LibraryInitializer}.
 *
 * @author Radu Sebastian LAZIN
 */
class LibraryInitializerTest {

	static class DefaultType {
		// empty
	}

	static class FirstLibraryType extends DefaultType {
		// empty
	}

	static class SecondLibraryType extends DefaultType {
		// empty
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldReturnInstanceOfFirstPresentLibrary() {
		LibraryDescriptor<FirstLibraryType> first =
				LibraryDescriptor.of(true, FirstLibraryType.class);

		LibraryDescriptor<SecondLibraryType> second =
				LibraryDescriptor.of(true, SecondLibraryType.class);

		Supplier<DefaultType> fallback = mock(Supplier.class);

		DefaultType result = LibraryInitializer.instance(fallback, first, second);

		assertThat(result, instanceOf(FirstLibraryType.class));
		verifyNoInteractions(fallback);
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldSkipLibrariesThatAreNotPresent() {
		LibraryDescriptor<FirstLibraryType> notPresent =
				LibraryDescriptor.of(false, FirstLibraryType.class);

		LibraryDescriptor<SecondLibraryType> present =
				LibraryDescriptor.of(true, SecondLibraryType.class);

		Supplier<DefaultType> fallback = mock(Supplier.class);

		DefaultType result = LibraryInitializer.instance(fallback, notPresent, present);

		assertThat(result, instanceOf(SecondLibraryType.class));
		verifyNoInteractions(fallback);
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldReturnFallbackWhenNoLibrariesArePresent() {
		LibraryDescriptor<FirstLibraryType> first =
				LibraryDescriptor.of(false, FirstLibraryType.class);

		LibraryDescriptor<SecondLibraryType> second =
				LibraryDescriptor.of(false, SecondLibraryType.class);

		DefaultType fallbackInstance = new DefaultType();
		Supplier<DefaultType> fallback = mock(Supplier.class);
		when(fallback.get()).thenReturn(fallbackInstance);

		DefaultType result = LibraryInitializer.instance(fallback, first, second);

		assertThat(result, sameInstance(fallbackInstance));
		verify(fallback).get();
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldReturnFallbackWhenNoLibrariesAreProvided() {
		DefaultType fallbackInstance = new DefaultType();
		Supplier<DefaultType> fallback = mock(Supplier.class);
		when(fallback.get()).thenReturn(fallbackInstance);

		DefaultType result = LibraryInitializer.instance(fallback);

		assertThat(result, sameInstance(fallbackInstance));
		verify(fallback).get();
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldReturnFallbackWhenLibrariesArrayIsNull() {
		DefaultType fallbackInstance = new DefaultType();
		Supplier<DefaultType> fallback = mock(Supplier.class);
		when(fallback.get()).thenReturn(fallbackInstance);

		DefaultType result = LibraryInitializer.instance(fallback, (LibraryDescriptor<DefaultType>[]) null);

		assertThat(result, sameInstance(fallbackInstance));
		verify(fallback).get();
	}
}
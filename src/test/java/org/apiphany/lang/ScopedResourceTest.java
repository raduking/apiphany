package org.apiphany.lang;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ScopedResource}.
 *
 * @author Radu Sebastian LAZIN
 */
class ScopedResourceTest {

	@SuppressWarnings("resource")
	@Test
	void shouldCreateAManagedResourceWithConstructor() {
		TestResource resource = new TestResource();

		ScopedResource<TestResource> scopedResource = new ScopedResource<>(resource);

		TestResource retrievedResource = scopedResource.unwrap();

		assertThat(resource, equalTo(retrievedResource));
		assertTrue(scopedResource.isManaged());
		assertFalse(scopedResource.isNotManaged());
	}

	@SuppressWarnings("resource")
	@Test
	void shouldCreateAnUnmanagedResourceWithConstructor() {
		TestResource resource = new TestResource();

		ScopedResource<TestResource> scopedResource = new ScopedResource<>(resource, false);

		TestResource retrievedResource = scopedResource.unwrap();

		assertThat(resource, equalTo(retrievedResource));
		assertTrue(scopedResource.isNotManaged());
		assertFalse(scopedResource.isManaged());
	}

	@SuppressWarnings("resource")
	@Test
	void shouldCreateAManagedResourceWithFactoryMethod() {
		TestResource resource = new TestResource();

		ScopedResource<TestResource> scopedResource = ScopedResource.managed(resource);

		TestResource retrievedResource = scopedResource.unwrap();

		assertThat(resource, equalTo(retrievedResource));
		assertTrue(scopedResource.isManaged());
		assertFalse(scopedResource.isNotManaged());
	}

	@SuppressWarnings("resource")
	@Test
	void shouldCreateAnUnmanagedResourceWithFactoryMethod() {
		TestResource resource = new TestResource();

		ScopedResource<TestResource> scopedResource = ScopedResource.unmanaged(resource);

		TestResource retrievedResource = scopedResource.unwrap();

		assertThat(resource, equalTo(retrievedResource));
		assertTrue(scopedResource.isNotManaged());
		assertFalse(scopedResource.isManaged());
	}

	@SuppressWarnings("resource")
	@Test
	void shouldCloseManagedResource() throws Exception {
		TestResource resource = new TestResource();

		ScopedResource<TestResource> scopedResource = ScopedResource.managed(resource);

		scopedResource.closeIfManaged();

		assertTrue(resource.isClosed());
	}

	@Test
	void shouldNotCloseUnmanagedResource() throws Exception {
		try (TestResource resource = new TestResource()) {
			ScopedResource<TestResource> scopedResource = ScopedResource.unmanaged(resource);

			scopedResource.closeIfManaged();

			assertFalse(resource.isClosed());
		}
	}

	@SuppressWarnings("resource")
	@Test
	void shouldCloseManagedResourceWithExceptionHandler() {
		TestResource resource = new TestResource();

		ScopedResource<TestResource> scopedResource = ScopedResource.managed(resource);

		assertDoesNotThrow(() -> scopedResource.closeIfManaged(e -> {
			throw new RuntimeException("Should not reach here!", e);
		}));

		assertTrue(resource.isClosed());
	}

	@Test
	void shouldNotCloseUnmanagedResourceWithExceptionHandler() {
		try (TestResource resource = new TestResource()) {
			ScopedResource<TestResource> scopedResource = ScopedResource.unmanaged(resource);

			assertDoesNotThrow(() -> scopedResource.closeIfManaged(e -> {
				throw new RuntimeException("Should not reach here!", e);
			}));

			assertFalse(resource.isClosed());
		}
	}

	@SuppressWarnings("resource")
	@Test
	void shouldCallExceptionHandlerWhenClosingManagedResourceFails() {
		ScopedResource<TestResource> scopedResource = ScopedResource.managed(new TestResource() {
			@Override
			public void close() {
				throw new RuntimeException("Close failed!");
			}
		});

		AtomicBoolean handlerCalled = new AtomicBoolean(false);

		scopedResource.closeIfManaged(e -> handlerCalled.set(true));

		assertTrue(handlerCalled.get());
	}

	@SuppressWarnings("resource")
	@Test
	void shouldTransformToMapAManagedResource() {
		TestResource resource = new TestResource();

		ScopedResource<TestResource> scopedResource = ScopedResource.managed(resource);

		var map = scopedResource.toMap();

		assertThat(map.size(), equalTo(1));
		assertThat(map.get(resource), equalTo(Boolean.TRUE));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldTransformToMapAnUnmanagedResource() {
		TestResource resource = new TestResource();

		ScopedResource<TestResource> scopedResource = ScopedResource.unmanaged(resource);

		var map = scopedResource.toMap();

		assertThat(map.size(), equalTo(1));
		assertThat(map.get(resource), equalTo(Boolean.FALSE));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldReturnUnmanagedWhenBothManagedAndSameResource() {
		TestResource c = new TestResource();

		ScopedResource<TestResource> a = ScopedResource.managed(c);
		ScopedResource<TestResource> b = ScopedResource.managed(c);

		ScopedResource<TestResource> result = ScopedResource.checked(a, b);

		assertSame(c, result.unwrap());
		assertTrue(result.isNotManaged());
	}

	@SuppressWarnings("resource")
	@Test
	void shouldReturnOriginalWhenResourcesAreDifferent() {
		ScopedResource<TestResource> a = ScopedResource.managed(new TestResource());
		ScopedResource<TestResource> b = ScopedResource.managed(new TestResource());

		ScopedResource<TestResource> result = ScopedResource.checked(a, b);

		assertSame(a, result);
		assertTrue(result.isManaged());
	}

	@SuppressWarnings("resource")
	@Test
	void shouldReturnOriginalWhenFirstIsAlreadyUnmanaged() {
		ScopedResource<TestResource> a = ScopedResource.unmanaged(new TestResource());
		ScopedResource<TestResource> b = ScopedResource.managed(new TestResource());

		ScopedResource<TestResource> result = ScopedResource.checked(a, b);

		assertSame(a, result);
		assertTrue(result.isNotManaged());
	}

	@SuppressWarnings("resource")
	@Test
	void shouldReturnOriginalWhenSecondIsAlreadyUnmanaged() {
		ScopedResource<TestResource> a = ScopedResource.managed(new TestResource());
		ScopedResource<TestResource> b = ScopedResource.unmanaged(new TestResource());

		ScopedResource<TestResource> result = ScopedResource.checked(a, b);

		assertSame(a, result);
		assertTrue(a.isManaged());
	}

	static class TestResource implements AutoCloseable {

		private boolean closed = false;

		@Override
		public void close() {
			closed = true;
		}

		public boolean isClosed() {
			return closed;
		}
	}

}

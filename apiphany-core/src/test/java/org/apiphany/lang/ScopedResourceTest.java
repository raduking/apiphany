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

	@Test
	@SuppressWarnings("resource")
	void shouldCreateAManagedResourceWithConstructor() {
		TestResource resource = new TestResource();

		ScopedResource<TestResource> scopedResource = new ScopedResource<>(resource);

		TestResource retrievedResource = scopedResource.unwrap();

		assertThat(resource, equalTo(retrievedResource));
		assertTrue(scopedResource.isManaged());
		assertFalse(scopedResource.isNotManaged());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCreateAnUnmanagedResourceWithConstructor() {
		TestResource resource = new TestResource();

		ScopedResource<TestResource> scopedResource = new ScopedResource<>(resource, Lifecycle.UNMANAGED);

		TestResource retrievedResource = scopedResource.unwrap();

		assertThat(resource, equalTo(retrievedResource));
		assertTrue(scopedResource.isNotManaged());
		assertFalse(scopedResource.isManaged());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCreateAManagedResourceWithFactoryMethod() {
		TestResource resource = new TestResource();

		ScopedResource<TestResource> scopedResource = ScopedResource.managed(resource);

		TestResource retrievedResource = scopedResource.unwrap();

		assertThat(resource, equalTo(retrievedResource));
		assertTrue(scopedResource.isManaged());
		assertFalse(scopedResource.isNotManaged());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCreateAnUnmanagedResourceWithFactoryMethod() {
		TestResource resource = new TestResource();

		ScopedResource<TestResource> scopedResource = ScopedResource.unmanaged(resource);

		TestResource retrievedResource = scopedResource.unwrap();

		assertThat(resource, equalTo(retrievedResource));
		assertTrue(scopedResource.isNotManaged());
		assertFalse(scopedResource.isManaged());
	}

	@Test
	@SuppressWarnings("resource")
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

	@Test
	@SuppressWarnings("resource")
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

	@Test
	@SuppressWarnings("resource")
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

	@Test
	@SuppressWarnings("resource")
	void shouldTransformToMapAManagedResource() {
		TestResource resource = new TestResource();

		ScopedResource<TestResource> scopedResource = ScopedResource.managed(resource);

		var map = scopedResource.toMap();

		assertThat(map.size(), equalTo(1));
		assertThat(map.get(resource), equalTo(Boolean.TRUE));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldTransformToMapAnUnmanagedResource() {
		TestResource resource = new TestResource();

		ScopedResource<TestResource> scopedResource = ScopedResource.unmanaged(resource);

		var map = scopedResource.toMap();

		assertThat(map.size(), equalTo(1));
		assertThat(map.get(resource), equalTo(Boolean.FALSE));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnUnmanagedWhenBothManagedAndSameResource() {
		TestResource c = new TestResource();

		ScopedResource<TestResource> a = ScopedResource.managed(c);
		ScopedResource<TestResource> b = ScopedResource.managed(c);

		ScopedResource<TestResource> result = ScopedResource.ensureSingleManager(a, b);

		assertSame(c, result.unwrap());
		assertTrue(result.isNotManaged());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnOriginalWhenResourcesAreDifferent() {
		ScopedResource<TestResource> a = ScopedResource.managed(new TestResource());
		ScopedResource<TestResource> b = ScopedResource.managed(new TestResource());

		ScopedResource<TestResource> result = ScopedResource.ensureSingleManager(a, b);

		assertSame(a, result);
		assertTrue(result.isManaged());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnOriginalWhenFirstIsAlreadyUnmanaged() {
		ScopedResource<TestResource> a = ScopedResource.unmanaged(new TestResource());
		ScopedResource<TestResource> b = ScopedResource.managed(new TestResource());

		ScopedResource<TestResource> result = ScopedResource.ensureSingleManager(a, b);

		assertSame(a, result);
		assertTrue(result.isNotManaged());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnOriginalWhenSecondIsAlreadyUnmanaged() {
		ScopedResource<TestResource> a = ScopedResource.managed(new TestResource());
		ScopedResource<TestResource> b = ScopedResource.unmanaged(new TestResource());

		ScopedResource<TestResource> result = ScopedResource.ensureSingleManager(a, b);

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

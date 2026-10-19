package org.apiphany.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apiphany.test.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link ApacheHC5Entities}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApacheHC5EntitiesTest {

	private static final byte[] DATA = "entity data".getBytes(StandardCharsets.UTF_8);
	private static final byte[] EMPTY = new byte[0];

	@Nested
	class ToInputStreamFromHttpEntityTests {

		@Test
		@SuppressWarnings("resource")
		void shouldReturnNullInputStreamWhenEntityIsNull() throws Exception {
			InputStream stream = ApacheHC5Entities.toInputStream((HttpEntity) null);

			assertThat(stream, is(notNullValue()));
			assertThat(stream.readAllBytes(), is(equalTo(EMPTY)));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnTheEntityContent() throws Exception {
			HttpEntity entity = mock(HttpEntity.class);
			doReturn(new ByteArrayInputStream(DATA)).when(entity).getContent();

			InputStream stream = ApacheHC5Entities.toInputStream(entity);

			assertThat(stream.readAllBytes(), is(equalTo(DATA)));
		}
	}

	@Nested
	class ToInputStreamFromResponseTests {

		@Test
		@SuppressWarnings("resource")
		void shouldReturnNullInputStreamWhenResponseIsNull() throws Exception {
			InputStream stream = ApacheHC5Entities.toInputStream((ClassicHttpResponse) null);

			assertThat(stream, is(notNullValue()));
			assertThat(stream.readAllBytes(), is(equalTo(EMPTY)));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnNullInputStreamWhenResponseEntityIsNull() throws Exception {
			ClassicHttpResponse response = mock(ClassicHttpResponse.class);
			doReturn(null).when(response).getEntity();

			InputStream stream = ApacheHC5Entities.toInputStream(response);

			assertThat(stream, is(notNullValue()));
			assertThat(stream.readAllBytes(), is(equalTo(EMPTY)));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnStreamBoundToTheResponse() throws Exception {
			ClassicHttpResponse response = mock(ClassicHttpResponse.class);
			HttpEntity entity = mock(HttpEntity.class);
			doReturn(entity).when(response).getEntity();
			doReturn(new ByteArrayInputStream(DATA)).when(entity).getContent();

			InputStream stream = ApacheHC5Entities.toInputStream(response);

			assertThat(stream.readAllBytes(), is(equalTo(DATA)));

			stream.close();

			verify(response).close();
		}
	}

	@Nested
	class ToByteArrayFromHttpEntityTests {

		@Test
		void shouldReturnEmptyArrayWhenEntityIsNull() {
			byte[] result = ApacheHC5Entities.toByteArray((HttpEntity) null, 1024);

			assertArrayEquals(EMPTY, result);
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnTheEntityContentAsByteArray() throws Exception {
			HttpEntity entity = mock(HttpEntity.class);
			doReturn(new ByteArrayInputStream(DATA)).when(entity).getContent();

			byte[] result = ApacheHC5Entities.toByteArray(entity, 1024);

			assertArrayEquals(DATA, result);
		}

		@Test
		@SuppressWarnings("resource")
		void shouldUseTheMaximumBufferSizeByDefault() throws Exception {
			HttpEntity entity = mock(HttpEntity.class);
			doReturn(new ByteArrayInputStream(DATA)).when(entity).getContent();

			byte[] result = ApacheHC5Entities.toByteArray(entity);

			assertArrayEquals(DATA, result);
		}

		@Test
		@SuppressWarnings("resource")
		void shouldWrapExceptionsWhenReadingTheContentFails() throws Exception {
			HttpEntity entity = mock(HttpEntity.class);
			doReturn(failingInputStream()).when(entity).getContent();

			IllegalStateException exception = assertThrows(IllegalStateException.class,
					() -> ApacheHC5Entities.toByteArray(entity));

			assertThat(exception.getMessage(),
					is(equalTo("Failed to read HTTP entity content as byte array: read failure")));
			assertThat(exception.getCause(), is(instanceOf(IOException.class)));
			assertThat(exception.getCause().getMessage(), is(equalTo("read failure")));
		}
	}

	@Nested
	class ToByteArrayFromResponseTests {

		@Test
		void shouldReturnEmptyArrayWhenResponseIsNull() {
			byte[] result = ApacheHC5Entities.toByteArray((ClassicHttpResponse) null, 1024);

			assertArrayEquals(EMPTY, result);
		}

		@Test
		@SuppressWarnings("resource")
		void shouldReturnTheResponseEntityContentAsByteArray() throws Exception {
			ClassicHttpResponse response = mock(ClassicHttpResponse.class);
			HttpEntity entity = mock(HttpEntity.class);
			doReturn(entity).when(response).getEntity();
			doReturn(new ByteArrayInputStream(DATA)).when(entity).getContent();

			byte[] result = ApacheHC5Entities.toByteArray(response, 1024);

			assertArrayEquals(DATA, result);
		}
	}

	@Nested
	class CreateTests {

		@Test
		void shouldCreateOneShotEntity() throws Exception {
			try (HttpEntity entity = ApacheHC5Entities.create(new ByteArrayInputStream(DATA), ContentType.TEXT_PLAIN)) {
				assertThat(entity, is(instanceOf(OneShotHttpEntity.class)));
				assertThat(entity.isRepeatable(), is(false));
			}
		}
	}

	@Nested
	class ConstructorTests {

		@Test
		void shouldThrowExceptionOnInstantiating() {
			UnsupportedOperationException exception = Assertions.assertDefaultConstructorThrows(ApacheHC5Entities.class);

			assertThat(exception.getMessage(), is(equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED)));
		}
	}

	private static InputStream failingInputStream() {
		return new InputStream() {

			@Override
			public int read() throws IOException {
				throw new IOException("read failure");
			}
		};
	}
}

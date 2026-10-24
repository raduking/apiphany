package org.apiphany.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link OneShotHttpEntity}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class OneShotHttpEntityTest {

	private static final byte[] DATA = "one shot".getBytes(StandardCharsets.UTF_8);

	@Test
	@SuppressWarnings("resource")
	void shouldNotBeRepeatable() {
		OneShotHttpEntity entity = new OneShotHttpEntity(new ByteArrayInputStream(DATA), ContentType.TEXT_PLAIN);

		assertThat(entity.isRepeatable(), is(false));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldBeStreaming() {
		OneShotHttpEntity entity = new OneShotHttpEntity(new ByteArrayInputStream(DATA), ContentType.TEXT_PLAIN);

		assertThat(entity.isStreaming(), is(true));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnUnknownContentLength() {
		OneShotHttpEntity entity = new OneShotHttpEntity(new ByteArrayInputStream(DATA), ContentType.TEXT_PLAIN);

		assertEquals(-1L, entity.getContentLength());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldExposeTheContentType() {
		OneShotHttpEntity entity = new OneShotHttpEntity(new ByteArrayInputStream(DATA), ContentType.APPLICATION_JSON);

		assertThat(entity.getContentType(), is(equalTo(ContentType.APPLICATION_JSON.toString())));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnTheOriginalStreamOnFirstGetContent() throws Exception {
		OneShotHttpEntity entity = new OneShotHttpEntity(new ByteArrayInputStream(DATA), ContentType.TEXT_PLAIN);

		InputStream content = entity.getContent();

		assertThat(content, is(notNullValue()));
		assertThat(content.readAllBytes(), is(equalTo(DATA)));

		entity.close();
	}

	@Test
	@SuppressWarnings("resource")
	void shouldThrowIllegalStateExceptionWhenContentIsConsumedTwice() throws Exception {
		OneShotHttpEntity entity = new OneShotHttpEntity(new ByteArrayInputStream(DATA), ContentType.TEXT_PLAIN);

		assertThat(entity.getContent().readAllBytes(), is(equalTo(DATA)));

		IllegalStateException exception = assertThrows(IllegalStateException.class, entity::getContent);
		assertThat(exception.getMessage(), is(equalTo("Stream is not repeatable and has already been consumed.")));

		entity.close();
	}

	@Test
	void shouldWriteContentToTheGivenOutputStream() throws Exception {
		OneShotHttpEntity entity = new OneShotHttpEntity(new ByteArrayInputStream(DATA), ContentType.TEXT_PLAIN);
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		entity.writeTo(output);

		assertThat(output.toByteArray(), is(equalTo(DATA)));
		assertThrows(IllegalStateException.class, entity::getContent);

		entity.close();
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCloseTheOriginalStream() throws Exception {
		InputStream original = mock(InputStream.class);
		OneShotHttpEntity entity = new OneShotHttpEntity(original, ContentType.TEXT_PLAIN);

		entity.close();

		verify(original).close();
	}

	@Test
	@SuppressWarnings("resource")
	void shouldWrapTheIOExceptionWhenClosingTheOriginalStreamFails() throws Exception {
		InputStream original = mock(InputStream.class);
		IOException cause = new IOException("close failure");
		doThrow(cause).when(original).close();
		OneShotHttpEntity entity = new OneShotHttpEntity(original, ContentType.TEXT_PLAIN);

		IOException exception = assertThrows(IOException.class, entity::close);

		assertThat(exception.getMessage(), is(equalTo("Failed to close original stream")));
		assertThat(exception.getCause(), is(equalTo(cause)));
	}

	@Test
	void shouldThrowNullPointerExceptionWhenOriginalStreamIsNull() {
		assertThrows(NullPointerException.class, () -> new OneShotHttpEntity(null, ContentType.TEXT_PLAIN));
	}
}

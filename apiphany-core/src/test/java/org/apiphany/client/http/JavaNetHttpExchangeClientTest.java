package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.http.HttpRequest.BodyPublisher;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import org.apiphany.ApiClient;
import org.apiphany.ApiClientFluentAdapter;
import org.apiphany.test.http.ByteBufferBodySubscriber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link JavaNetHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class JavaNetHttpExchangeClientTest {

	private static final String STRING = "Juju";
	private static final byte[] BYTES = new byte[] { 0x01, 0x02, 0x03 };

	@Mock
	private ApiClient apiClient;

	@Test
	void shouldConvertByteArrayToByteArrayBodyPublisher() {
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.body(BYTES);

		BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

		assertThat(bodyPublisher.contentLength(), equalTo((long) BYTES.length));

		ByteBufferBodySubscriber subscriber = new ByteBufferBodySubscriber();
		bodyPublisher.subscribe(subscriber);
		subscriber.awaitCompletion();

		assertTrue(subscriber.isCompleted());
		assertNull(subscriber.getError());
		assertThat(BYTES, equalTo(subscriber.getReceivedBytes()));
	}

	@Test
	void shouldConvertInputStreamToInputStreamBodyPublisher() {
		ByteArrayInputStream bis = new ByteArrayInputStream(BYTES);

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.body(bis);

		BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

		ByteBufferBodySubscriber subscriber = new ByteBufferBodySubscriber();
		bodyPublisher.subscribe(subscriber);
		subscriber.awaitCompletion();

		assertTrue(subscriber.isCompleted());
		assertNull(subscriber.getError());
		assertThat(BYTES, equalTo(subscriber.getReceivedBytes()));
	}

	@Test
	void shouldConvertInputStreamSupplierToInputStreamBodyPublisher() {
		ByteArrayInputStream bis = new ByteArrayInputStream(BYTES);
		Supplier<? extends InputStream> supplier = () -> bis;

		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.body(supplier);

		BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

		ByteBufferBodySubscriber subscriber = new ByteBufferBodySubscriber();
		bodyPublisher.subscribe(subscriber);
		subscriber.awaitCompletion();

		assertTrue(subscriber.isCompleted());
		assertNull(subscriber.getError());
		assertThat(BYTES, equalTo(subscriber.getReceivedBytes()));
	}

	@Test
	void shouldConvertStringToStringBodyPublisher() {
		ApiClientFluentAdapter request = ApiClientFluentAdapter.of(apiClient)
				.body(STRING);

		BodyPublisher bodyPublisher = JavaNetHttpExchangeClient.toBodyPublisher(request);

		assertThat(bodyPublisher.contentLength(), equalTo((long) STRING.length()));

		ByteBufferBodySubscriber subscriber = new ByteBufferBodySubscriber();
		bodyPublisher.subscribe(subscriber);
		subscriber.awaitCompletion();

		assertTrue(subscriber.isCompleted());
		assertNull(subscriber.getError());
		assertThat(STRING.getBytes(StandardCharsets.UTF_8), equalTo(subscriber.getReceivedBytes()));
	}
}

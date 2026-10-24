package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.Duration;
import java.util.Objects;

import org.apiphany.http.OkHttpLibrary;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link OkHttpProperties}.
 *
 * @author Radu Sebastian LAZIN
 */
class OkHttpPropertiesTest {

	private static OkHttpProperties newProperties() {
		return OkHttpProperties.withDefaults();
	}

	@Test
	void shouldUseOkHttpClientNameAsRootPropertyPrefix() {
		assertThat(OkHttpProperties.ROOT, equalTo(OkHttpLibrary.CLIENT_NAME));
	}

	@Test
	void shouldCreatePropertiesWithDefaults() {
		OkHttpProperties properties = newProperties();

		assertThat(properties.getConnection().getKeepAliveDuration(), equalTo(OkHttpProperties.Connection.Default.KEEP_ALIVE_DURATION));
		assertThat(properties.getConnection().getMaxIdleConnections(), equalTo(OkHttpProperties.Connection.Default.MAX_IDLE_CONNECTIONS));
		assertThat(properties.getConnection().isFollowRedirects(), equalTo(OkHttpProperties.Connection.Default.FOLLOW_REDIRECTS));
		assertThat(properties.getRequest().getCallTimeout(), equalTo(Duration.ZERO));
		assertThat(properties.getConnect().getTimeout(), equalTo(Duration.ofSeconds(10)));
		assertThat(properties.getRead().getTimeout(), equalTo(Duration.ofSeconds(10)));
		assertThat(properties.getWrite().getTimeout(), equalTo(Duration.ofSeconds(10)));
	}

	@Test
	void shouldSetAndGetAllProperties() {
		OkHttpProperties properties = new OkHttpProperties();
		OkHttpProperties.Connection connection = new OkHttpProperties.Connection();
		OkHttpProperties.Request request = new OkHttpProperties.Request();
		OkHttpProperties.Connect connect = new OkHttpProperties.Connect();
		OkHttpProperties.Read read = new OkHttpProperties.Read();
		OkHttpProperties.Write write = new OkHttpProperties.Write();

		properties.setConnection(connection);
		properties.setRequest(request);
		properties.setConnect(connect);
		properties.setRead(read);
		properties.setWrite(write);

		assertThat(properties.getConnection(), equalTo(connection));
		assertThat(properties.getRequest(), equalTo(request));
		assertThat(properties.getConnect(), equalTo(connect));
		assertThat(properties.getRead(), equalTo(read));
		assertThat(properties.getWrite(), equalTo(write));
	}

	@Test
	void shouldSerializeWithTheGlobalJsonBuilder() {
		assertThat(newProperties().toString(), notNullValue());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		OkHttpProperties properties1 = newProperties();
		OkHttpProperties properties2 = newProperties();

		assertEquals(properties1, properties1);
		assertEquals(properties1, properties2);
		assertEquals(properties2, properties1);
		assertEquals(properties1.hashCode(), properties2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		OkHttpProperties properties = newProperties();

		assertThat(properties, not(equalTo(null)));
		assertThat(properties, not(equalTo("not-an-okhttp-properties")));
	}

	@Test
	void shouldNotEqualIfConnectionDiffers() {
		OkHttpProperties properties1 = newProperties();
		OkHttpProperties properties2 = newProperties();
		properties2.getConnection().setMaxIdleConnections(6);

		assertNotEquals(properties1, properties2);
	}

	@Test
	void shouldNotEqualIfRequestDiffers() {
		OkHttpProperties properties1 = newProperties();
		OkHttpProperties properties2 = newProperties();
		properties2.getRequest().setCallTimeout(Duration.ofSeconds(1));

		assertNotEquals(properties1, properties2);
	}

	@Test
	void shouldNotEqualIfConnectDiffers() {
		OkHttpProperties properties1 = newProperties();
		OkHttpProperties properties2 = newProperties();
		properties2.getConnect().setTimeout(Duration.ofSeconds(1));

		assertNotEquals(properties1, properties2);
	}

	@Test
	void shouldNotEqualIfReadDiffers() {
		OkHttpProperties properties1 = newProperties();
		OkHttpProperties properties2 = newProperties();
		properties2.getRead().setTimeout(Duration.ofSeconds(1));

		assertNotEquals(properties1, properties2);
	}

	@Test
	void shouldNotEqualIfWriteDiffers() {
		OkHttpProperties properties1 = newProperties();
		OkHttpProperties properties2 = newProperties();
		properties2.getWrite().setTimeout(Duration.ofSeconds(1));

		assertNotEquals(properties1, properties2);
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		OkHttpProperties properties = newProperties();

		int expectedHash = Objects.hash(
				properties.getConnection(),
				properties.getRequest(),
				properties.getConnect(),
				properties.getRead(),
				properties.getWrite());

		assertEquals(expectedHash, properties.hashCode());
	}

	/**
	 * Test class for {@link OkHttpProperties.Connection}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@Nested
	class ConnectionTest {

		@Test
		void shouldEqualSameValuesAndSameReference() {
			OkHttpProperties.Connection connection1 = newProperties().getConnection();
			OkHttpProperties.Connection connection2 = newProperties().getConnection();

			assertEquals(connection1, connection1);
			assertEquals(connection1, connection2);
			assertEquals(connection2, connection1);
			assertEquals(connection1.hashCode(), connection2.hashCode());
		}

		@Test
		void shouldNotEqualIfDifferentObjects() {
			OkHttpProperties.Connection connection = newProperties().getConnection();

			assertThat(connection, not(equalTo(null)));
			assertThat(connection, not(equalTo("not-a-connection")));
		}

		@Test
		void shouldNotEqualIfKeepAliveDurationDiffers() {
			OkHttpProperties.Connection connection1 = newProperties().getConnection();
			OkHttpProperties.Connection connection2 = newProperties().getConnection();
			connection2.setKeepAliveDuration(Duration.ofSeconds(5));

			assertNotEquals(connection1, connection2);
		}

		@Test
		void shouldNotEqualIfMaxIdleConnectionsDiffers() {
			OkHttpProperties.Connection connection1 = newProperties().getConnection();
			OkHttpProperties.Connection connection2 = newProperties().getConnection();
			connection2.setMaxIdleConnections(6);

			assertNotEquals(connection1, connection2);
		}

		@Test
		void shouldNotEqualIfFollowRedirectsDiffers() {
			OkHttpProperties.Connection connection1 = newProperties().getConnection();
			OkHttpProperties.Connection connection2 = newProperties().getConnection();
			connection2.setFollowRedirects(true);

			assertNotEquals(connection1, connection2);
		}

		@Test
		void shouldBuildHashcodeWithAllFields() {
			OkHttpProperties.Connection connection = newProperties().getConnection();

			int expectedHash = Objects.hash(connection.getKeepAliveDuration(), connection.getMaxIdleConnections(),
					connection.isFollowRedirects());

			assertEquals(expectedHash, connection.hashCode());
		}

		@Test
		void shouldSetAndGetAllProperties() {
			OkHttpProperties.Connection connection = new OkHttpProperties.Connection();

			connection.setKeepAliveDuration(Duration.ofSeconds(5));
			connection.setMaxIdleConnections(7);
			connection.setFollowRedirects(true);

			assertThat(connection.getKeepAliveDuration(), equalTo(Duration.ofSeconds(5)));
			assertThat(connection.getMaxIdleConnections(), equalTo(7));
			assertThat(connection.isFollowRedirects(), equalTo(true));
		}

		@Test
		void shouldSerializeToJson() {
			assertThat(newProperties().getConnection().toString(), notNullValue());
		}
	}

	/**
	 * Test class for {@link OkHttpProperties.Request}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@Nested
	class RequestTest {

		@Test
		void shouldEqualSameValuesAndSameReference() {
			OkHttpProperties.Request request1 = newProperties().getRequest();
			OkHttpProperties.Request request2 = newProperties().getRequest();

			assertEquals(request1, request1);
			assertEquals(request1, request2);
			assertEquals(request2, request1);
			assertEquals(request1.hashCode(), request2.hashCode());
		}

		@Test
		void shouldNotEqualIfDifferentObjects() {
			OkHttpProperties.Request request = newProperties().getRequest();

			assertThat(request, not(equalTo(null)));
			assertThat(request, not(equalTo("not-a-request")));
		}

		@Test
		void shouldNotEqualIfCallTimeoutDiffers() {
			OkHttpProperties.Request request1 = newProperties().getRequest();
			OkHttpProperties.Request request2 = newProperties().getRequest();
			request2.setCallTimeout(Duration.ofSeconds(5));

			assertNotEquals(request1, request2);
		}

		@Test
		void shouldBuildHashcodeWithAllFields() {
			OkHttpProperties.Request request = newProperties().getRequest();

			assertEquals(Objects.hash(request.getCallTimeout()), request.hashCode());
		}

		@Test
		void shouldSetAndGetProperties() {
			OkHttpProperties.Request request = new OkHttpProperties.Request();
			request.setCallTimeout(Duration.ofSeconds(5));

			assertThat(request.getCallTimeout(), equalTo(Duration.ofSeconds(5)));
		}

		@Test
		void shouldSerializeToJson() {
			assertThat(newProperties().getRequest().toString(), notNullValue());
		}
	}

	/**
	 * Test class for the OkHttp timeout property types.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@Nested
	class TimeoutPropertiesTest {

		@Test
		void shouldCompareConnectProperties() {
			OkHttpProperties.Connect connect1 = new OkHttpProperties.Connect();
			OkHttpProperties.Connect connect2 = new OkHttpProperties.Connect();

			assertEquals(connect1, connect1); // NOSONAR same instance equals
			assertEquals(connect1, connect2);
			assertThat(connect1, not(equalTo(null)));
			assertThat(connect1, not(equalTo("not-a-connect")));
			connect2.setTimeout(Duration.ofSeconds(5));
			assertNotEquals(connect1, connect2);
			assertEquals(Objects.hash(connect1.getTimeout()), connect1.hashCode());
			connect1.setTimeout(Duration.ofSeconds(6));
			assertThat(connect1.getTimeout(), equalTo(Duration.ofSeconds(6)));
			assertThat(connect1.toString(), notNullValue());
		}

		@Test
		void shouldCompareReadProperties() {
			OkHttpProperties.Read read1 = new OkHttpProperties.Read();
			OkHttpProperties.Read read2 = new OkHttpProperties.Read();

			assertEquals(read1, read1); // NOSONAR same instance equals
			assertEquals(read1, read2);
			assertThat(read1, not(equalTo(null)));
			assertThat(read1, not(equalTo("not-a-read")));
			read2.setTimeout(Duration.ofSeconds(5));
			assertNotEquals(read1, read2);
			assertEquals(Objects.hash(read1.getTimeout()), read1.hashCode());
			read1.setTimeout(Duration.ofSeconds(6));
			assertThat(read1.getTimeout(), equalTo(Duration.ofSeconds(6)));
			assertThat(read1.toString(), notNullValue());
		}

		@Test
		void shouldCompareWriteProperties() {
			OkHttpProperties.Write write1 = new OkHttpProperties.Write();
			OkHttpProperties.Write write2 = new OkHttpProperties.Write();

			assertEquals(write1, write1); // NOSONAR same instance equals
			assertEquals(write1, write2);
			assertThat(write1, not(equalTo(null)));
			assertThat(write1, not(equalTo("not-a-write")));
			write2.setTimeout(Duration.ofSeconds(5));
			assertNotEquals(write1, write2);
			assertEquals(Objects.hash(write1.getTimeout()), write1.hashCode());
			write1.setTimeout(Duration.ofSeconds(6));
			assertThat(write1.getTimeout(), equalTo(Duration.ofSeconds(6)));
			assertThat(write1.toString(), notNullValue());
		}
	}
}

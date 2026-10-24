package org.apiphany.client.http;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Objects;

import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.util.Timeout;
import org.apiphany.http.ApacheHC5Library;
import org.apiphany.json.JsonBuilder;
import org.apiphany.json.jackson2.ApiphanyHC5Jackson2Module;
import org.apiphany.json.jackson2.Jackson2JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Test class for {@link ApacheHC5Properties}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApacheHC5PropertiesTest {

	private static final String PROPERTIES_FILE = "apache-hc5-properties.yaml";

	private static Jackson2JsonBuilder jsonBuilder;

	@BeforeAll
	static void setup() {
		jsonBuilder = Jackson2JsonBuilder.custom(new YAMLFactory());
		jsonBuilder.getObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
		jsonBuilder.getObjectMapper().registerModule(ApiphanyHC5Jackson2Module.instance());
	}

	private static <T> T fromYaml(final String yaml, final Class<T> valueType) {
		return JsonBuilder.with(jsonBuilder, () -> JsonBuilder.fromJson(yaml, valueType));
	}

	private static ApacheHC5Properties newProperties() {
		return fromYaml(Strings.fromFile(PROPERTIES_FILE), ApplicationProperties.class).getHttpClient5();
	}

	static class ApplicationProperties {

		private ApacheHC5Properties httpClient5;

		public ApplicationProperties() {
			// Default constructor for Jackson
		}

		public ApacheHC5Properties getHttpClient5() {
			return httpClient5;
		}

		public void setHttpClient5(final ApacheHC5Properties httpClient5) {
			this.httpClient5 = httpClient5;
		}
	}

	@Test
	void shouldUseApacheHC5ClientNameAsRootPropertyPrefix() {
		assertThat(ApacheHC5Properties.ROOT, equalTo(ApacheHC5Library.CLIENT_NAME));
	}

	@Test
	void shouldLoadPropertiesFromFile() throws ParseException {
		ApacheHC5Properties httpClient5Properties = newProperties();

		assertThat(httpClient5Properties.getConnection().getTimeToLive(), equalTo(Timeout.ofSeconds(30)));
		assertThat(httpClient5Properties.getConnection().getMaxTotal(), equalTo(100));
		assertThat(httpClient5Properties.getConnection().getMaxPerRoute(), equalTo(50));

		assertThat(httpClient5Properties.getRequest().getHttpProtocolVersion(), equalTo(ProtocolVersion.parse("HTTP/1.1")));
		assertThat(httpClient5Properties.getRequest().isProtocolUpgradeEnabled(), equalTo(true));

		assertThat(httpClient5Properties.getConnectionRequest().getTimeout(), equalTo(Timeout.INFINITE));

		assertThat(httpClient5Properties.getConnect().getTimeout(), equalTo(Timeout.ofMinutes(1)));

		assertThat(httpClient5Properties.getSocket().getTimeout(), equalTo(Timeout.ofHours(1)));
	}

	@Test
	void shouldCreatePropertiesWithDefaults() {
		ApacheHC5Properties properties = ApacheHC5Properties.withDefaults();

		assertThat(properties.getConnection().getTimeToLive(), equalTo(ApacheHC5Properties.Connection.Default.TIME_TO_LIVE));
		assertThat(properties.getConnection().getMaxTotal(), equalTo(ApacheHC5Properties.Connection.Default.MAX_TOTAL));
		assertThat(properties.getConnection().getMaxPerRoute(), equalTo(ApacheHC5Properties.Connection.Default.MAX_PER_ROUTE));
		assertThat(properties.getConnection().isFollowRedirects(), equalTo(ApacheHC5Properties.Connection.Default.FOLLOW_REDIRECTS));
		assertThat(properties.getRequest().getRequestTimeout(), equalTo(Timeout.INFINITE));
		assertThat(properties.getConnectionRequest().getTimeout(), equalTo(Timeout.INFINITE));
		assertThat(properties.getConnect().getTimeout(), equalTo(Timeout.INFINITE));
		assertThat(properties.getSocket().getTimeout(), equalTo(Timeout.INFINITE));
	}

	@Test
	void shouldExposeTimeoutValueConstants() {
		assertThat(ApacheHC5Properties.TimeoutValue.DISABLED, equalTo("disabled"));
		assertThat(ApacheHC5Properties.TimeoutValue.INFINITE, equalTo("infinite"));
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateTimeoutValue() {
		UnsupportedOperationException e = assertDefaultConstructorThrows(ApacheHC5Properties.TimeoutValue.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldSetAndGetAllProperties() {
		ApacheHC5Properties properties = new ApacheHC5Properties();
		ApacheHC5Properties.Connection connection = new ApacheHC5Properties.Connection();
		ApacheHC5Properties.Request request = new ApacheHC5Properties.Request();
		ApacheHC5Properties.ConnectionRequest connectionRequest = new ApacheHC5Properties.ConnectionRequest();
		ApacheHC5Properties.Connect connect = new ApacheHC5Properties.Connect();
		ApacheHC5Properties.Socket socket = new ApacheHC5Properties.Socket();

		properties.setConnection(connection);
		properties.setRequest(request);
		properties.setConnectionRequest(connectionRequest);
		properties.setConnect(connect);
		properties.setSocket(socket);

		assertThat(properties.getConnection(), equalTo(connection));
		assertThat(properties.getRequest(), equalTo(request));
		assertThat(properties.getConnectionRequest(), equalTo(connectionRequest));
		assertThat(properties.getConnect(), equalTo(connect));
		assertThat(properties.getSocket(), equalTo(socket));
	}

	@Test
	void shouldSerializeWithTheGlobalJsonBuilder() {
		String json = newProperties().toString();

		assertThat(json, containsString("connection"));
		assertThat(json, containsString("request"));
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		ApacheHC5Properties properties1 = newProperties();
		ApacheHC5Properties properties2 = newProperties();

		// same reference
		assertEquals(properties1, properties1);

		// different instance, same values
		assertEquals(properties1, properties2);
		assertEquals(properties2, properties1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(properties1.hashCode(), properties2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		ApacheHC5Properties properties = newProperties();

		assertThat(properties, not(equalTo(null)));
		assertThat(properties, not(equalTo("not-an-apache-hc5-properties")));
	}

	@Test
	void shouldNotEqualIfConnectionDiffers() {
		ApacheHC5Properties properties1 = newProperties();
		ApacheHC5Properties properties2 = newProperties();
		properties2.getConnection().setMaxTotal(5);

		assertNotEquals(properties1, properties2);
	}

	@Test
	void shouldNotEqualIfRequestDiffers() {
		ApacheHC5Properties properties1 = newProperties();
		ApacheHC5Properties properties2 = newProperties();
		properties2.getRequest().setProtocolVersion("HTTP/2");

		assertNotEquals(properties1, properties2);
	}

	@Test
	void shouldNotEqualIfConnectionRequestDiffers() {
		ApacheHC5Properties properties1 = newProperties();
		ApacheHC5Properties properties2 = newProperties();
		properties2.getConnectionRequest().setTimeout(Timeout.ofSeconds(5));

		assertNotEquals(properties1, properties2);
	}

	@Test
	void shouldNotEqualIfConnectDiffers() {
		ApacheHC5Properties properties1 = newProperties();
		ApacheHC5Properties properties2 = newProperties();
		properties2.getConnect().setTimeout(Timeout.ofSeconds(5));

		assertNotEquals(properties1, properties2);
	}

	@Test
	void shouldNotEqualIfSocketDiffers() {
		ApacheHC5Properties properties1 = newProperties();
		ApacheHC5Properties properties2 = newProperties();
		properties2.getSocket().setTimeout(Timeout.ofSeconds(5));

		assertNotEquals(properties1, properties2);
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		ApacheHC5Properties properties = newProperties();

		int expectedHash = Objects.hash(
				properties.getConnection(),
				properties.getRequest(),
				properties.getConnectionRequest(),
				properties.getConnect(),
				properties.getSocket());

		assertEquals(expectedHash, properties.hashCode());
	}

	/**
	 * Test class for {@link ApacheHC5Properties.Connection}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@Nested
	class ConnectionTest {

		@Test
		void shouldEqualSameValuesAndSameReference() {
			ApacheHC5Properties.Connection connection1 = newProperties().getConnection();
			ApacheHC5Properties.Connection connection2 = newProperties().getConnection();

			assertEquals(connection1, connection1);
			assertEquals(connection1, connection2);
			assertEquals(connection2, connection1);
			assertEquals(connection1.hashCode(), connection2.hashCode());
		}

		@Test
		void shouldNotEqualIfDifferentObjects() {
			ApacheHC5Properties.Connection connection = newProperties().getConnection();

			assertThat(connection, not(equalTo(null)));
			assertThat(connection, not(equalTo("not-a-connection")));
		}

		@Test
		void shouldNotEqualIfTimeToLiveDiffers() {
			ApacheHC5Properties.Connection connection1 = newProperties().getConnection();
			ApacheHC5Properties.Connection connection2 = newProperties().getConnection();
			connection2.setTimeToLive(Timeout.ofSeconds(5));

			assertNotEquals(connection1, connection2);
		}

		@Test
		void shouldNotEqualIfMaxTotalDiffers() {
			ApacheHC5Properties.Connection connection1 = newProperties().getConnection();
			ApacheHC5Properties.Connection connection2 = newProperties().getConnection();
			connection2.setMaxTotal(5);

			assertNotEquals(connection1, connection2);
		}

		@Test
		void shouldNotEqualIfMaxPerRouteDiffers() {
			ApacheHC5Properties.Connection connection1 = newProperties().getConnection();
			ApacheHC5Properties.Connection connection2 = newProperties().getConnection();
			connection2.setMaxPerRoute(5);

			assertNotEquals(connection1, connection2);
		}

		@Test
		void shouldBuildHashcodeWithAllFields() {
			ApacheHC5Properties.Connection connection = newProperties().getConnection();

			int expectedHash = Objects.hash(connection.getTimeToLive(), connection.getMaxTotal(), connection.getMaxPerRoute());

			assertEquals(expectedHash, connection.hashCode());
		}

		@Test
		void shouldSetAndGetAllProperties() {
			ApacheHC5Properties.Connection connection = new ApacheHC5Properties.Connection();

			connection.setTimeToLive(Timeout.ofSeconds(5));
			connection.setMaxTotal(7);
			connection.setMaxPerRoute(3);
			connection.setFollowRedirects(true);

			assertThat(connection.getTimeToLive(), equalTo(Timeout.ofSeconds(5)));
			assertThat(connection.getMaxTotal(), equalTo(7));
			assertThat(connection.getMaxPerRoute(), equalTo(3));
			assertThat(connection.isFollowRedirects(), equalTo(true));
		}

		@Test
		void shouldSerializeToJson() {
			assertThat(newProperties().getConnection().toString(), containsString("maxTotal"));
		}
	}

	/**
	 * Test class for {@link ApacheHC5Properties.Request}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@Nested
	class RequestTest {

		@Test
		void shouldEqualSameValuesAndSameReference() {
			ApacheHC5Properties.Request request1 = newProperties().getRequest();
			ApacheHC5Properties.Request request2 = newProperties().getRequest();

			assertEquals(request1, request1);
			assertEquals(request1, request2);
			assertEquals(request2, request1);
			assertEquals(request1.hashCode(), request2.hashCode());
		}

		@Test
		void shouldNotEqualIfDifferentObjects() {
			ApacheHC5Properties.Request request = newProperties().getRequest();

			assertThat(request, not(equalTo(null)));
			assertThat(request, not(equalTo("not-a-request")));
		}

		@Test
		void shouldNotEqualIfProtocolUpgradeEnabledDiffers() {
			ApacheHC5Properties.Request request1 = newProperties().getRequest();
			ApacheHC5Properties.Request request2 = newProperties().getRequest();
			request2.setProtocolUpgradeEnabled(false);

			assertNotEquals(request1, request2);
		}

		@Test
		void shouldNotEqualIfProtocolVersionDiffers() {
			ApacheHC5Properties.Request request1 = newProperties().getRequest();
			ApacheHC5Properties.Request request2 = newProperties().getRequest();
			request2.setProtocolVersion("HTTP/2");

			assertNotEquals(request1, request2);
		}

		@Test
		void shouldNotEqualIfRequestTimeoutDiffers() {
			ApacheHC5Properties.Request request1 = newProperties().getRequest();
			ApacheHC5Properties.Request request2 = newProperties().getRequest();
			request2.setRequestTimeout(Timeout.ofSeconds(5));

			assertNotEquals(request1, request2);
		}

		@Test
		void shouldBuildHashcodeWithAllFields() {
			ApacheHC5Properties.Request request = newProperties().getRequest();

			int expectedHash = Objects.hash(request.isProtocolUpgradeEnabled(), request.getProtocolVersion(), request.getRequestTimeout());

			assertEquals(expectedHash, request.hashCode());
		}

		@Test
		void shouldSetAndGetAllProperties() {
			ApacheHC5Properties.Request request = new ApacheHC5Properties.Request();

			request.setProtocolUpgradeEnabled(true);
			request.setProtocolVersion("HTTP/2");
			request.setRequestTimeout(Timeout.ofSeconds(5));

			assertThat(request.isProtocolUpgradeEnabled(), equalTo(true));
			assertThat(request.getProtocolVersion(), equalTo("HTTP/2"));
			assertThat(request.getRequestTimeout(), equalTo(Timeout.ofSeconds(5)));
		}

		@Test
		void shouldReturnDefaultHttpVersionIfTheProtocolVersionIsInvalid() {
			ApacheHC5Properties.Request request = new ApacheHC5Properties.Request();
			request.setProtocolVersion("not-a-protocol-version");

			assertThat(request.getHttpProtocolVersion(), equalTo(HttpVersion.DEFAULT));
		}

		@Test
		void shouldSerializeToJson() {
			assertThat(newProperties().getRequest().toString(), containsString("protocolVersion"));
		}
	}

	/**
	 * Test class for {@link ApacheHC5Properties.Connect}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@Nested
	class ConnectTest {

		@Test
		void shouldEqualSameValuesAndSameReference() {
			ApacheHC5Properties.Connect connect1 = newProperties().getConnect();
			ApacheHC5Properties.Connect connect2 = newProperties().getConnect();

			assertEquals(connect1, connect1);
			assertEquals(connect1, connect2);
			assertEquals(connect2, connect1);
			assertEquals(connect1.hashCode(), connect2.hashCode());
		}

		@Test
		void shouldNotEqualIfDifferentObjects() {
			ApacheHC5Properties.Connect connect = newProperties().getConnect();

			assertThat(connect, not(equalTo(null)));
			assertThat(connect, not(equalTo("not-a-connect")));
		}

		@Test
		void shouldNotEqualIfTimeoutDiffers() {
			ApacheHC5Properties.Connect connect1 = newProperties().getConnect();
			ApacheHC5Properties.Connect connect2 = newProperties().getConnect();
			connect2.setTimeout(Timeout.ofSeconds(5));

			assertNotEquals(connect1, connect2);
		}

		@Test
		void shouldBuildHashcodeWithAllFields() {
			ApacheHC5Properties.Connect connect = newProperties().getConnect();

			int expectedHash = Objects.hash(connect.getTimeout());

			assertEquals(expectedHash, connect.hashCode());
		}

		@Test
		void shouldSetAndGetAllProperties() {
			ApacheHC5Properties.Connect connect = new ApacheHC5Properties.Connect();

			connect.setTimeout(Timeout.ofSeconds(5));

			assertThat(connect.getTimeout(), equalTo(Timeout.ofSeconds(5)));
		}

		@Test
		void shouldSerializeToJson() {
			assertThat(newProperties().getConnect().toString(), containsString("timeout"));
		}
	}

	/**
	 * Test class for {@link ApacheHC5Properties.ConnectionRequest}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@Nested
	class ConnectionRequestTest {

		@Test
		void shouldEqualSameValuesAndSameReference() {
			ApacheHC5Properties.ConnectionRequest connectionRequest1 = newProperties().getConnectionRequest();
			ApacheHC5Properties.ConnectionRequest connectionRequest2 = newProperties().getConnectionRequest();

			assertEquals(connectionRequest1, connectionRequest1);
			assertEquals(connectionRequest1, connectionRequest2);
			assertEquals(connectionRequest2, connectionRequest1);
			assertEquals(connectionRequest1.hashCode(), connectionRequest2.hashCode());
		}

		@Test
		void shouldNotEqualIfDifferentObjects() {
			ApacheHC5Properties.ConnectionRequest connectionRequest = newProperties().getConnectionRequest();

			assertThat(connectionRequest, not(equalTo(null)));
			assertThat(connectionRequest, not(equalTo("not-a-connection-request")));
		}

		@Test
		void shouldNotEqualIfTimeoutDiffers() {
			ApacheHC5Properties.ConnectionRequest connectionRequest1 = newProperties().getConnectionRequest();
			ApacheHC5Properties.ConnectionRequest connectionRequest2 = newProperties().getConnectionRequest();
			connectionRequest2.setTimeout(Timeout.ofSeconds(5));

			assertNotEquals(connectionRequest1, connectionRequest2);
		}

		@Test
		void shouldBuildHashcodeWithAllFields() {
			ApacheHC5Properties.ConnectionRequest connectionRequest = newProperties().getConnectionRequest();

			int expectedHash = Objects.hash(connectionRequest.getTimeout());

			assertEquals(expectedHash, connectionRequest.hashCode());
		}

		@Test
		void shouldSetAndGetAllProperties() {
			ApacheHC5Properties.ConnectionRequest connectionRequest = new ApacheHC5Properties.ConnectionRequest();

			connectionRequest.setTimeout(Timeout.ofSeconds(5));

			assertThat(connectionRequest.getTimeout(), equalTo(Timeout.ofSeconds(5)));
		}

		@Test
		void shouldSerializeToJson() {
			assertThat(newProperties().getConnectionRequest().toString(), containsString("timeout"));
		}
	}

	/**
	 * Test class for {@link ApacheHC5Properties.Socket}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@Nested
	class SocketTest {

		@Test
		void shouldEqualSameValuesAndSameReference() {
			ApacheHC5Properties.Socket socket1 = newProperties().getSocket();
			ApacheHC5Properties.Socket socket2 = newProperties().getSocket();

			assertEquals(socket1, socket1);
			assertEquals(socket1, socket2);
			assertEquals(socket2, socket1);
			assertEquals(socket1.hashCode(), socket2.hashCode());
		}

		@Test
		void shouldNotEqualIfDifferentObjects() {
			ApacheHC5Properties.Socket socket = newProperties().getSocket();

			assertThat(socket, not(equalTo(null)));
			assertThat(socket, not(equalTo("not-a-socket")));
		}

		@Test
		void shouldNotEqualIfTimeoutDiffers() {
			ApacheHC5Properties.Socket socket1 = newProperties().getSocket();
			ApacheHC5Properties.Socket socket2 = newProperties().getSocket();
			socket2.setTimeout(Timeout.ofSeconds(5));

			assertNotEquals(socket1, socket2);
		}

		@Test
		void shouldBuildHashcodeWithAllFields() {
			ApacheHC5Properties.Socket socket = newProperties().getSocket();

			int expectedHash = Objects.hash(socket.getTimeout());

			assertEquals(expectedHash, socket.hashCode());
		}

		@Test
		void shouldSetAndGetAllProperties() {
			ApacheHC5Properties.Socket socket = new ApacheHC5Properties.Socket();

			socket.setTimeout(Timeout.ofSeconds(5));

			assertThat(socket.getTimeout(), equalTo(Timeout.ofSeconds(5)));
		}

		@Test
		void shouldSerializeToJson() {
			assertThat(newProperties().getSocket().toString(), containsString("timeout"));
		}
	}
}

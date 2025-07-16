package org.apiphany.security.ssl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.net.ssl.SSLHandshakeException;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Hex;
import org.apiphany.lang.Strings;
import org.apiphany.net.Sockets;
import org.apiphany.security.ssl.client.Bytes;
import org.apiphany.security.ssl.client.CipherSuite;
import org.apiphany.security.ssl.client.ClientHello;
import org.apiphany.security.ssl.client.CurveName;
import org.apiphany.security.ssl.client.MinimalTLSClient;
import org.apiphany.security.ssl.client.PseudoRandomFunction;
import org.apiphany.security.ssl.client.TLSRecord;
import org.apiphany.security.ssl.server.SimpleHttpsServer;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for {@link SSLProperties}.
 *
 * @author Radu Sebastian LAZIN
 */
class SSLPropertiesTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(SSLPropertiesTest.class);

	private static final String SSL = "ssl";

	private static final Duration PORT_CHECK_TIMEOUT = Duration.ofMillis(500);
	private static final String LOCALHOST = "localhost";
	private static final String SERVER_HOST = LOCALHOST;
	private static final int SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);
	private static final String SERVER_URL = "https://" + SERVER_HOST + ":" + SERVER_PORT;

	private static final KeyPair CLIENT_KEY_PAIR = Keys.loadKeyPairFromResources();
	private static final SSLProperties SSL_PROPERTIES =
			JsonBuilder.fromJson(Strings.fromFile("/security/ssl/ssl-properties.json"), SSLProperties.class);
	private static final ClientProperties CLIENT_PROPERTIES = new ClientProperties();
	static {
		CLIENT_PROPERTIES.setCustomProperties(SSL, SSL_PROPERTIES);
	}

	private static final SimpleHttpsServer SERVER = new SimpleHttpsServer(SERVER_PORT, SSL_PROPERTIES);

	@AfterAll
	static void tearDownAll() throws Exception {
		SERVER.close();
	}

	@Test
	void shouldHaveSSLAsRootProperty() {
		assertThat(SSLProperties.ROOT, equalTo(SSL));
	}

	@Test
	void shouldNotGetNameIfSSLIsNotInitialized() throws Exception {
		int port = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);
		SimpleHttpsServer server = new SimpleHttpsServer(port, new SSLProperties());

		Exception exception = null;
		try (SimpleApiClient api = new SimpleApiClient("https://localhost:" + port)) {
			api.getName();
		} catch (Exception e) {
			exception = e;
		} finally {
			server.close();
		}

		assertNotNull(exception);
		assertThat(exception.getClass(), equalTo(SSLHandshakeException.class));
	}

	@Test
	void shouldReturnNameOnGetNameWithInitializedSSL() throws Exception {
		try (SimpleSSLApiClient api = new SimpleSSLApiClient()) {
			String name = api.getName();

			assertThat(name, equalTo(SimpleHttpsServer.NAME));
		}
	}

	@Test
	void shouldPerformTLS_V_1_2_SSLHandshake() throws Exception {
		int port = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);
		SSLProperties sslProperties = JsonBuilder.fromJson(Strings.fromFile("/security/ssl/ssl-properties.json"), SSLProperties.class);
		sslProperties.setProtocol(SSLProtocol.TLS_1_2);
		SimpleHttpsServer server = new SimpleHttpsServer(port, sslProperties);

		byte[] serverFinished = null;
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, CLIENT_KEY_PAIR)) {
			serverFinished = client.performHandshake();
		} catch (Exception e) {
			LOGGER.error("Error performing SSL handshake", e);
		} finally {
			server.close();
		}
		assertNotNull(serverFinished);
	}

	@Test
	void shouldPerformTLS_V_1_2_SSLHandshakeWithCloseNotify() throws Exception {
		int port = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);
		SSLProperties sslProperties = JsonBuilder.fromJson(Strings.fromFile("/security/ssl/ssl-properties.json"), SSLProperties.class);
		sslProperties.setProtocol(SSLProtocol.TLS_1_2);
		SimpleHttpsServer server = new SimpleHttpsServer(port, sslProperties);

		byte[] closeNotify = null;
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, CLIENT_KEY_PAIR)) {
			client.performHandshake();
			closeNotify = client.closeNotify();
		} catch (Exception e) {
			LOGGER.error("Error performing SSL handshake", e);
		} finally {
			server.close();
		}
		assertNotNull(closeNotify);
	}

	@Test
	void shouldPerformTLS_V_1_2_SSLHandshakeAndGetName() throws Exception {
		int port = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);
		SSLProperties sslProperties = JsonBuilder.fromJson(Strings.fromFile("/security/ssl/ssl-properties.json"), SSLProperties.class);
		sslProperties.setProtocol(SSLProtocol.TLS_1_2);
		SimpleHttpsServer server = new SimpleHttpsServer(port, sslProperties);

		String response = null;
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, CLIENT_KEY_PAIR)) {
			client.performHandshake();
			response = client.get("/" + ApiClient.API + "/name");
		} catch (Exception e) {
			LOGGER.error("Error performing SSL handshake", e);
		} finally {
			server.close();
		}
		assertThat(response, equalTo(SimpleHttpsServer.NAME));
	}

	@Ignore("Run only when OpenSSL is running on port 4433")
	@Test
	void shouldPerformTLS_V_1_2_SSLHandshakeOpenSSL() throws Exception {
		int port = 4433;

		byte[] serverFinished = null;
		try (MinimalTLSClient client = new MinimalTLSClient(LOCALHOST, port, CLIENT_KEY_PAIR)) {
			serverFinished = client.performHandshake();
			client.closeNotify();
		} catch (Exception e) {
			LOGGER.error("Error performing SSL handshake", e);
		}
		assertNotNull(serverFinished);
	}

	@Test
	void shouldBuildClientHello() {
		// these bytes are from: https://tls12.xargs.org/#client-hello
		final byte[] byteArray = Bytes.fromHex("""
				16 03 01 00 A5 01 00 00  A1 03 03 00 01 02 03 04
				05 06 07 08 09 0A 0B 0C  0D 0E 0F 10 11 12 13 14
				15 16 17 18 19 1A 1B 1C  1D 1E 1F 00 00 20 CC A8
				CC A9 C0 2F C0 30 C0 2B  C0 2C C0 13 C0 09 C0 14
				C0 0A 00 9C 00 9D 00 2F  00 35 C0 12 00 0A 01 00
				00 58 00 00 00 18 00 16  00 00 13 65 78 61 6D 70
				6C 65 2E 75 6C 66 68 65  69 6D 2E 6E 65 74 00 05
				00 05 01 00 00 00 00 00  0A 00 0A 00 08 00 1D 00
				17 00 18 00 19 00 0B 00  02 01 00 00 0D 00 12 00
				10 04 01 04 03 05 01 05  03 06 01 06 03 02 01 02
				03 FF 01 00 01 00 00 12  00 00
		""");
		LOGGER.info("\n{}", Hex.dump(byteArray));
		final CipherSuite[] cypherSuitesArray = {
				CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,
				CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
				CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
				CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
				CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
				CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
				CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
				CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
				CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
				CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
				CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
				CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
				CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
				CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
				CipherSuite.TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA,
				CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA
		};
		final CurveName[] curveNamesArray = {
				CurveName.X25519,
				CurveName.SECP256R1,
				CurveName.SECP384R1,
				CurveName.SECP521R1
		};
		List<CipherSuite> cypherSuites = List.of(cypherSuitesArray);
		List<CurveName> curveNames = List.of(curveNamesArray);

		TLSRecord clientHello = new TLSRecord(SSLProtocol.TLS_1_0, new ClientHello(List.of("example.ulfheim.net"), cypherSuites, curveNames));
		LOGGER.info("Client Hello: {}", clientHello);

		byte[] bytes = clientHello.toByteArray();

		LOGGER.info("Expected: {}", Hex.string(byteArray));
		LOGGER.info("Received: {}", Hex.string(bytes));

		assertThat(bytes, equalTo(byteArray));
	}

	@Test
	void shouldReadOpenSSLClientHelloWithTLSRecordAPI() throws IOException {
		// TODO: handle invalid SNI
		// string obtained with command:
		// openssl s_client -connect localhost:<port> -tls1_2 -servername localhost -msg -debug
		String openSSLClientHelloHexString = """
				16 03 01 00 c9
				01 00 00 c5 03 03 df a2 7a 5a 1d b7 c5 92 51 84
				71 f1 e2 f3 7a f3 80 d7 7a 63 66 59 e5 a5 bc bd
				f7 17 71 b8 19 a0 00 00 38 c0 2c c0 30 00 9f cc
				a9 cc a8 cc aa c0 2b c0 2f 00 9e c0 24 c0 28 00
				6b c0 23 c0 27 00 67 c0 0a c0 14 00 39 c0 09 c0
				13 00 33 00 9d 00 9c 00 3d 00 3c 00 35 00 2f 00
				ff 01 00 00 64 00 00 00 0e 00 0c 00 00 09 6c 6f
				63 61 6c 68 6f 73 74 00 0b 00 04 03 00 01 02 00
				0a 00 0c 00 0a 00 1d 00 17 00 1e 00 19 00 18 00
				23 00 00 00 16 00 00 00 17 00 00 00 0d 00 2a 00
				28 04 03 05 03 06 03 08 07 08 08 08 09 08 0a 08
				0b 08 04 08 05 08 06 04 01 05 01 06 01 03 03 03
				01 03 02 04 02 05 02 06 02
					""";
		byte[] expected = Bytes.fromHex(openSSLClientHelloHexString);
		ByteArrayInputStream bis = new ByteArrayInputStream(expected);

		TLSRecord tlsRecord = TLSRecord.from(bis);
		LOGGER.info("Parsed Client Hello: {}", tlsRecord.getHandshake(0).getBody());

		byte[] result = tlsRecord.toByteArray();

		LOGGER.info("Expected bytes:\n{}", Hex.dump(expected));
		LOGGER.info("Received bytes:\n{}", Hex.dump(result));

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReadServerHelloReceivedByOpenSSLWithTLSRecordAPI() throws IOException {
		String openSSLReceivedServerHello = """
						16 03 03 05 1d
						02 00 00 55 03 03 f4 01 7a 7c 90 04 0e 3e fe 5d
						7b 8e 61 43 c1 d1 b5 c4 ab da 61 5d cf c2 44 4f
						57 4e 47 52 44 01 20 4f bf c2 e3 8b 53 92 15 32
						f0 b7 84 83 b2 a7 70 cf 5c 84 6c c1 08 2b 5f 2d
						fd b1 12 dc 98 9a c7 c0 30 00 00 0d 00 17 00 00
						00 23 00 00 ff 01 00 01 00
						0b 00 03 90 00 03 8d 00 03 8a 30 82 03 86 30 82
						02 6e a0 03 02 01 02 02 09 00 ec 0e af a1 8f 9f
						57 b0 30 0d 06 09 2a 86 48 86 f7 0d 01 01 0c 05
						00 30 70 31 0b 30 09 06 03 55 04 06 13 02 55 53
						31 11 30 0f 06 03 55 04 08 13 08 4e 65 77 20 59
						6f 72 6b 31 11 30 0f 06 03 55 04 07 13 08 4e 65
						77 20 59 6f 72 6b 31 11 30 0f 06 03 55 04 0a 13
						08 52 61 64 75 4b 69 6e 67 31 14 30 12 06 03 55
						04 0b 13 0b 44 65 76 65 6c 6f 70 6d 65 6e 74 31
						12 30 10 06 03 55 04 03 13 09 6c 6f 63 61 6c 68
						6f 73 74 30 20 17 0d 32 35 30 36 33 30 31 37 33
						38 33 36 5a 18 0f 32 31 32 35 30 36 30 36 31 37
						33 38 33 36 5a 30 70 31 0b 30 09 06 03 55 04 06
						13 02 55 53 31 11 30 0f 06 03 55 04 08 13 08 4e
						65 77 20 59 6f 72 6b 31 11 30 0f 06 03 55 04 07
						13 08 4e 65 77 20 59 6f 72 6b 31 11 30 0f 06 03
						55 04 0a 13 08 52 61 64 75 4b 69 6e 67 31 14 30
						12 06 03 55 04 0b 13 0b 44 65 76 65 6c 6f 70 6d
						65 6e 74 31 12 30 10 06 03 55 04 03 13 09 6c 6f
						63 61 6c 68 6f 73 74 30 82 01 22 30 0d 06 09 2a
						86 48 86 f7 0d 01 01 01 05 00 03 82 01 0f 00 30
						82 01 0a 02 82 01 01 00 bc 1a 96 e2 81 f3 1e 1c
						77 e1 92 c5 81 ca e9 9c 67 7d 91 a3 da 9a 72 f4
						ad bd 3c 66 d8 f9 78 e2 84 b3 68 88 b2 e8 58 e6
						22 48 c7 1e 29 b1 58 fa 1e 81 3d 81 f8 90 85 ab
						6e dd aa 94 bb ec a4 c0 29 3d 90 cb 60 9f dc 8a
						03 c4 cb ae f6 63 e1 41 4e 58 84 74 c9 20 fe 31
						d8 b4 63 0f 5c 83 99 68 0f 6f c9 39 fa 18 77 dc
						06 2a 68 18 08 69 2d ee e4 54 2c 74 d9 43 0f 1e
						84 4e 61 c8 42 2c db 1e 27 6b be a9 48 22 09 09
						34 71 4a 7d ea 92 48 0e 78 b6 35 e0 0b fe 28 be
						98 ed 00 82 b4 0d d3 89 b0 00 ca 99 85 75 b6 b2
						a7 eb 50 c2 54 1c 9e 7a 2f dc 88 fb b7 87 dc 57
						3d ab 29 71 c3 8c de c4 aa 02 6e 14 1e 20 ff 55
						ce e4 f0 5b 69 a7 65 d2 86 2a ec 3b 27 9e 11 18
						9c 8a 9d 6c c9 91 e9 37 5e 3a 8a 01 cc b8 1d 6f
						46 06 dd fd e9 51 9b ac 85 d5 dc 7d 72 95 ee f8
						e7 ff af c4 95 37 76 fb 02 03 01 00 01 a3 21 30
						1f 30 1d 06 03 55 1d 0e 04 16 04 14 35 48 00 1b
						3d 90 1e 28 47 c3 e8 2a f9 bb 23 2b 4e c5 73 99
						30 0d 06 09 2a 86 48 86 f7 0d 01 01 0c 05 00 03
						82 01 01 00 6d 98 4e 7e 7e d6 4c 14 44 99 d1 33
						48 34 f8 cf 24 25 ee 3b 18 7a 01 f2 9b 2a 35 84
						05 8c 42 eb 23 1d 6a dc de 94 f2 1f 12 ac b8 16
						c2 b1 37 f4 c2 40 82 e2 65 19 85 5e f7 f0 c7 14
						b9 1f 09 ec 8c 21 12 f6 9e 1d c9 8f f1 fb 19 09
						38 10 57 92 ea 8f 3c 30 b8 3d 40 75 d5 f3 cf 7e
						06 9c 10 ba 9d 4f 71 f0 a3 1f 12 bc 18 b1 ce 00
						66 2a c3 c2 39 50 f6 46 5e 52 97 1c c0 13 3e 91
						23 1c c8 0d a5 6a 2b b2 01 e6 1d ca c1 52 9b 81
						59 16 97 58 57 c1 d3 3b 57 4e c9 5e 00 ed 9d 83
						6e 6f 2d 86 66 a5 e2 61 b3 65 af 70 88 2c 67 d0
						62 13 24 20 3e c0 90 ae 34 b2 8f 2f 34 a8 49 34
						68 bc 3b 2c 11 97 99 5f 3e b8 1d 2b fa e2 d3 60
						4b 6b 70 3d 35 6b df dc 5a cc 3f b8 a3 bf 4c 74
						97 9a a5 d1 ae 88 e7 ee 20 cf 6c 80 4a 6a bb 4b
						a4 b6 39 7b 39 67 56 ae 1b ac 4f 81 ab ed 28 5d
						73 c1 78 8a
						0c 00 01 28 03 00 1d 20 1f 35 7e 7a 39 c2 b7 e7
						ee f5 5d 2c ea d5 7d 51 cb 9d f2 61 0a cc e0 97
						ef 7d d3 c6 0e 48 63 7d 08 04 01 00 b0 57 17 e6
						6b f1 bd a6 c2 3c 44 b6 f5 0e 7c 83 7a 86 13 2f
						d4 ac b8 c1 19 d0 89 86 83 2e af a8 af 09 ab 01
						9a e4 1d b6 c3 4c 35 59 3e d2 e5 4d 59 32 51 73
						37 fb 65 b0 ec 71 1a 25 47 61 ab 8b 2c 39 9c 1f
						a3 3d 7b 93 08 5c ac cd 62 84 a5 90 80 c5 80 e2
						32 71 eb 09 9f bc 2f 22 d0 0a c5 43 44 c9 64 67
						4f a6 a8 a8 46 42 72 75 ff 4b ff 70 6c 98 a4 68
						0b 61 64 53 26 2b f0 8b 45 68 bf 87 ea b0 7f 43
						76 1b bc 5a 8b 32 51 4c 7d 21 39 ec 9b 9e a1 99
						44 9a 4a 9f c2 25 55 91 66 67 59 86 af 28 3b 02
						84 83 de ba e5 8a 12 6f 47 97 67 21 30 bc b0 ca
						7a 7e 4d d7 d3 b2 8e 1b 58 3c 51 86 a7 f5 fd 49
						4a 45 9a 7f 71 f8 e3 74 b5 29 83 8d c2 46 fa 7e
						bd 96 8d b4 68 47 47 32 2a da 49 af 1b 4d 97 84
						53 d8 63 91 8d 1f 6a 12 4a 78 d5 00 e0 05 17 58
						ce b6 3d 1f 33 b1 24 e1 02 17 01 55
						0e 00 00 00
				""";
		byte[] expected = Bytes.fromHex(openSSLReceivedServerHello);
		ByteArrayInputStream bis = new ByteArrayInputStream(expected);

		TLSRecord serverHello = TLSRecord.from(bis);

		LOGGER.info("Parsed Server Hello: {}", serverHello);

		byte[] result = serverHello.toByteArray();

		LOGGER.info("Expected bytes:\n{}", Hex.dump(expected));
		LOGGER.info("Received bytes:\n{}", Hex.dump(result));

		assertThat(result, equalTo(expected));
	}

	@ParameterizedTest
	@MethodSource("providePRFArguments")
	void shouldApplyTheCorrectPseudoRandomFunction(final String label, final String seed, final int length, final String expected)
			throws Exception {
		byte[] secret = new byte[48];
		Arrays.fill(secret, (byte) 0x0b);

		byte[] seedBytes = seed.getBytes(StandardCharsets.US_ASCII);
		byte[] output = PseudoRandomFunction.apply(secret, label, seedBytes, length);

		String hexOutput = Hex.string(output).toLowerCase().trim();

		LOGGER.info("PRF Output: {}", hexOutput);
		LOGGER.info("PRF Expect: {}", expected);

		assertThat(hexOutput, equalTo(expected));
	}

	private static Stream<Arguments> providePRFArguments() {
		return Stream.of(
				Arguments.of(
						"test label",
						"test seed",
						32,
						"cc 3a 20 27 3a 70 78 6a 85 65 6d 30 c0 ad 0c 7b "
								+ "e2 0b fd 51 d5 d1 5c 43 82 25 d8 fb 6a 94 82 f1"),
				Arguments.of(
						"another label",
						"different seed",
						48,
						"e1 1d fa 82 07 7c 04 77 2f b6 4a 1a 1a e5 40 59 "
								+ "cc 3b 19 9d f6 4b 50 23 28 50 08 7f ee 74 b8 68 "
								+ "63 90 53 de bf 4e 6f ce a8 a8 a3 34 7e f2 9d 7b"),
				Arguments.of(
						"label3",
						"seed3",
						64,
						"91 56 02 f6 ba 3f 29 ae 16 6c d7 26 e9 aa e5 16 "
								+ "ec 6b e9 01 29 e5 a9 11 2f ed f3 f1 bd 09 b2 a2 "
								+ "67 a1 ee 35 f5 ab da 72 97 4e 6d 41 87 ca d1 84 "
								+ "c5 c7 73 75 4f 8e 03 61 83 1e 26 c3 09 5c f9 2a"),
				Arguments.of(
						"test label",
						"test seed",
						16,
						"cc 3a 20 27 3a 70 78 6a 85 65 6d 30 c0 ad 0c 7b"));
	}

	static class SimpleApiClient extends ApiClient {

		public SimpleApiClient(final String url) {
			super(url, with(JavaNetHttpExchangeClient.class));
			setBleedExceptions(true);
		}

		public String getName() {
			return client()
					.http()
					.get()
					.path(API, "name")
					.retrieve(String.class)
					.orNull();
		}
	}

	static class SimpleSSLApiClient extends ApiClient {

		public SimpleSSLApiClient() {
			super(SERVER_URL, with(JavaNetHttpExchangeClient.class).properties(CLIENT_PROPERTIES));
		}

		public String getName() {
			return client()
					.http()
					.get()
					.path(API, "name")
					.retrieve(String.class)
					.orNull();
		}
	}

}

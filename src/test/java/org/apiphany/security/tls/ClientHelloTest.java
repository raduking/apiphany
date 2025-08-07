package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apiphany.lang.Bytes;
import org.apiphany.lang.Hex;
import org.apiphany.security.ssl.DeterministicSecureRandom;
import org.apiphany.security.ssl.SSLProtocol;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for {@link ClientHello}.
 *
 * @author Radu Sebastian LAZIN
 */
class ClientHelloTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientHelloTest.class);

	@Test
	void shouldBuildClientHello() {
		// these bytes are from: https://tls12.xargs.org/#client-hello
		final byte[] expected = Bytes.fromHex("""
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
		LOGGER.info("\n{}", Hex.dump(expected));
		final List<CipherSuite> cypherSuites = List.of(
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
		);
		final List<NamedCurve> namedCurves = List.of(
				NamedCurve.X25519,
				NamedCurve.SECP256R1,
				NamedCurve.SECP384R1,
				NamedCurve.SECP521R1
		);
		@SuppressWarnings("deprecation")
		final List<SignatureAlgorithm> signatureAlgorithms = List.of(
				SignatureAlgorithm.RSA_PKCS1_SHA256,
				SignatureAlgorithm.ECDSA_SECP256R1_SHA256,
				SignatureAlgorithm.RSA_PKCS1_SHA384,
				SignatureAlgorithm.ECDSA_SECP384R1_SHA384,
				SignatureAlgorithm.RSA_PKCS1_SHA512,
				SignatureAlgorithm.ECDSA_SECP521R1_SHA512,
				SignatureAlgorithm.RSA_PKCS1_SHA1,
				SignatureAlgorithm.ECDSA_SHA1
		);

		Record clientHello = new Record(SSLProtocol.TLS_1_0,
				new ClientHello(new DeterministicSecureRandom(), cypherSuites, List.of("example.ulfheim.net"), namedCurves, signatureAlgorithms));
		LOGGER.info("Client Hello: {}", clientHello);

		byte[] result = clientHello.toByteArray();

		LOGGER.info("Expected: {}", Hex.string(expected));
		LOGGER.info("Received: {}", Hex.string(result));

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReadOpenSSLClientHelloWithRecordAPI() throws IOException {
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

		Record tlsRecord = Record.from(bis);
		LOGGER.info("Parsed Client Hello: {}", tlsRecord.getHandshake(0).getBody());

		byte[] result = tlsRecord.toByteArray();

		LOGGER.info("Expected bytes:\n{}", Hex.dump(expected));
		LOGGER.info("Received bytes:\n{}", Hex.dump(result));

		assertThat(result, equalTo(expected));
	}

}

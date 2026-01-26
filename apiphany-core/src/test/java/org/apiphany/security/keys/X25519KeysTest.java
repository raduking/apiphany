package org.apiphany.security.keys;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.XECPrivateKey;
import java.security.interfaces.XECPublicKey;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

import org.apiphany.io.BytesOrder;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link X25519Keys}.
 *
 * @author Radu Sebastian LAZIN
 */
class X25519KeysTest {

	@Test
	void shouldHaveCanonicalInstance() {
		assertThat(X25519Keys.INSTANCE, is(notNullValue()));
		assertThat(X25519Keys.INSTANCE, is(instanceOf(X25519Keys.class)));
	}

	@Test
	void shouldHaveCorrectAlgorithmConstant() {
		assertThat(X25519Keys.ALGORITHM, is("XDH"));
	}

	@Test
	void shouldHaveCorrectCurveConstant() {
		assertThat(X25519Keys.CURVE, is(NamedParameterSpec.X25519));
	}

	@Test
	void shouldHaveCorrectPublicKeySize() {
		assertThat(X25519Keys.PUBLIC_KEY_SIZE, is(32));
	}

	@Test
	void shouldInstantiateKeyFactory() {
		X25519Keys keys = new X25519Keys();

		assertThat(keys.getKeyFactory(), is(notNullValue()));
	}

	@Test
	void shouldGenerateValidKeyPair() {
		KeyPair keyPair = X25519Keys.INSTANCE.generateKeyPair();

		assertThat(keyPair, is(notNullValue()));
		assertThat(keyPair.getPublic(), is(instanceOf(XECPublicKey.class)));
		assertThat(keyPair.getPrivate(), is(instanceOf(XECPrivateKey.class)));
	}

	@Test
	void shouldConvertLittleEndianBytesToPublicKey() {
		byte[] testPublicKeyBytes = new byte[32];
		Arrays.fill(testPublicKeyBytes, (byte) 0x42);

		PublicKey publicKey = X25519Keys.INSTANCE.publicKeyFrom(testPublicKeyBytes, BytesOrder.LITTLE_ENDIAN);

		assertThat(publicKey, is(notNullValue()));
		assertThat(publicKey, is(instanceOf(XECPublicKey.class)));
	}

	@Test
	void shouldConvertBigEndianBytesToPublicKey() {
		byte[] testPublicKeyBytes = new byte[32];
		Arrays.fill(testPublicKeyBytes, (byte) 0x42);

		PublicKey publicKey = X25519Keys.INSTANCE.publicKeyFrom(testPublicKeyBytes, BytesOrder.BIG_ENDIAN);

		assertThat(publicKey, is(notNullValue()));
		assertThat(publicKey, is(instanceOf(XECPublicKey.class)));
	}
}

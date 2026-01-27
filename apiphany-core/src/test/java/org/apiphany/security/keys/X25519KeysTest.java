package org.apiphany.security.keys;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.math.BigInteger;
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

	@Test
	void shouldCorrectlyConvertFromLittleEndian() throws Exception {
		byte[] littleEndianBytes = new byte[32];
		littleEndianBytes[0] = 0x01;
		littleEndianBytes[31] = 0x02;

		PublicKey result = X25519Keys.INSTANCE.fromLittleEndian(littleEndianBytes);

		assertThat(result, is(notNullValue()));
		assertThat(result, is(instanceOf(XECPublicKey.class)));
	}

	@Test
	void shouldCorrectlyConvertFromBigEndian() throws Exception {
		byte[] bigEndianBytes = new byte[32];
		bigEndianBytes[0] = 0x01;
		bigEndianBytes[31] = 0x02;

		PublicKey result = X25519Keys.INSTANCE.fromBigEndian(bigEndianBytes);

		assertThat(result, is(notNullValue()));
		assertThat(result, is(instanceOf(XECPublicKey.class)));
	}

	@Test
	void shouldGenerateSharedSecretBetweenKeyPair() {
		KeyPair keyPair = X25519Keys.INSTANCE.generateKeyPair();

		byte[] sharedSecret = X25519Keys.INSTANCE.getSharedSecret(keyPair.getPrivate(), keyPair.getPublic());

		assertThat(sharedSecret, is(notNullValue()));
		assertThat(sharedSecret.length, is(greaterThan(0)));
	}

	@Test
	void shouldConvertPublicKeyToLittleEndianByteArray() {
		KeyPair keyPair = X25519Keys.INSTANCE.generateKeyPair();

		byte[] bytes = X25519Keys.INSTANCE.toByteArray(keyPair.getPublic(), BytesOrder.LITTLE_ENDIAN);

		assertThat(bytes, is(notNullValue()));
		assertThat(bytes.length, is(32));
	}

	@Test
	void shouldConvertPublicKeyToBigEndianByteArray() {
		KeyPair keyPair = X25519Keys.INSTANCE.generateKeyPair();

		byte[] bytes = X25519Keys.INSTANCE.toByteArray(keyPair.getPublic(), BytesOrder.BIG_ENDIAN);

		assertThat(bytes, is(notNullValue()));
		assertThat(bytes.length, is(32));
	}

	@Test
	void shouldCorrectlyConvertPublicKeyToBigEndianByteArrayInternally() {
		KeyPair keyPair = X25519Keys.INSTANCE.generateKeyPair();

		byte[] bytes = X25519Keys.INSTANCE.toByteArrayBigEndian(keyPair.getPublic());

		assertThat(bytes, is(notNullValue()));
		assertThat(bytes.length, is(32));
	}

	@Test
	void shouldCorrectlyConvertPublicKeyToBigEndianByteArrayWhenByteArrayIsSigned() {
		byte[] bigEndianBytes = new byte[33];
		bigEndianBytes[0] = 0x00;
		bigEndianBytes[1] = (byte) 0x80; // binary: 10000000 (bit 7 = 1)
		bigEndianBytes[32] = 0x02;

		BigInteger u = new BigInteger(bigEndianBytes);

		XECPublicKey publicKey = mock(XECPublicKey.class);
		doReturn(u).when(publicKey).getU();

		byte[] bytes = X25519Keys.INSTANCE.toByteArrayBigEndian(publicKey);

		assertThat(bytes, is(notNullValue()));
		assertThat(bytes.length, is(32));
	}

	@Test
	void shouldThrowExceptionWhenConvertingPublicKeyToBigEndianByteArrayWhenByteArrayIsSignedAndFirstByteIsNotZero() {
		byte[] bigEndianBytes = new byte[33];
		bigEndianBytes[0] = 0x01;
		bigEndianBytes[1] = (byte) 0x80; // binary: 10000000 (bit 7 = 1)
		bigEndianBytes[32] = 0x02;

		BigInteger u = new BigInteger(bigEndianBytes);

		XECPublicKey publicKey = mock(XECPublicKey.class);
		doReturn(u).when(publicKey).getU();

		SecurityException e = assertThrows(SecurityException.class, () -> X25519Keys.INSTANCE.toByteArrayBigEndian(publicKey));

		assertThat(e.getMessage(), is("Error converting public key to big-endian byte array, bigInteger too large: " + bigEndianBytes.length));
	}

	@Test
	void shouldThrowExceptionWhenConvertingPublicKeyToBigEndianByteArrayWhenByteArrayIsSignedAndIsTooBig() {
		byte[] bigEndianBytes = new byte[34];
		bigEndianBytes[0] = 0x00;
		bigEndianBytes[1] = (byte) 0x80; // binary: 10000000 (bit 7 = 1)
		bigEndianBytes[33] = 0x02;

		BigInteger u = new BigInteger(bigEndianBytes);

		XECPublicKey publicKey = mock(XECPublicKey.class);
		doReturn(u).when(publicKey).getU();

		SecurityException e = assertThrows(SecurityException.class, () -> X25519Keys.INSTANCE.toByteArrayBigEndian(publicKey));

		assertThat(e.getMessage(), is("Error converting public key to big-endian byte array, bigInteger too large: " + bigEndianBytes.length));
	}

	@Test
	void shouldCorrectlyConvertPublicKeyToLittleEndianByteArrayInternally() {
		KeyPair keyPair = X25519Keys.INSTANCE.generateKeyPair();

		byte[] bytes = X25519Keys.INSTANCE.toByteArrayLittleEndian(keyPair.getPublic());

		assertThat(bytes, is(notNullValue()));
		assertThat(bytes.length, is(32));
	}

	@Test
	void shouldConvertPrivateKeyToLittleEndianByteArray() {
		KeyPair keyPair = X25519Keys.INSTANCE.generateKeyPair();

		byte[] bytes = X25519Keys.INSTANCE.toByteArray(keyPair.getPrivate(), BytesOrder.LITTLE_ENDIAN);

		assertThat(bytes, is(notNullValue()));
		assertThat(bytes.length, is(32));
	}

	@Test
	void shouldConvertPrivateKeyToBigEndianByteArray() {
		KeyPair keyPair = X25519Keys.INSTANCE.generateKeyPair();

		byte[] bytes = X25519Keys.INSTANCE.toByteArray(keyPair.getPrivate(), BytesOrder.BIG_ENDIAN);

		assertThat(bytes, is(notNullValue()));
		assertThat(bytes.length, is(32));
	}

	@Test
	void shouldCorrectlyConvertPrivateKeyToLittleEndianByteArrayInternally() {
		KeyPair keyPair = X25519Keys.INSTANCE.generateKeyPair();

		byte[] bytes = X25519Keys.INSTANCE.toByteArrayLittleEndian(keyPair.getPrivate());

		assertThat(bytes, is(notNullValue()));
		assertThat(bytes.length, is(32));
	}

	@Test
	void shouldCorrectlyConvertPrivateKeyToBigEndianByteArrayInternally() {
		KeyPair keyPair = X25519Keys.INSTANCE.generateKeyPair();

		byte[] bytes = X25519Keys.INSTANCE.toByteArrayBigEndian(keyPair.getPrivate());

		assertThat(bytes, is(notNullValue()));
		assertThat(bytes.length, is(32));
	}

	@Test
	void shouldReturnTrueWhenKeysMatch() {
		KeyPair keyPair = X25519Keys.INSTANCE.generateKeyPair();
		byte[] publicKeyBytes = X25519Keys.INSTANCE.toByteArray(keyPair.getPublic(), BytesOrder.BIG_ENDIAN);

		boolean result = X25519Keys.INSTANCE.verifyKeyMatch(publicKeyBytes, BytesOrder.BIG_ENDIAN, keyPair.getPublic());

		assertThat(result, is(true));
	}

	@Test
	void shouldReturnFalseWhenKeysDontMatch() {
		KeyPair keyPair = X25519Keys.INSTANCE.generateKeyPair();
		byte[] differentBytes = new byte[32];
		Arrays.fill(differentBytes, (byte) 0x99);

		boolean result = X25519Keys.INSTANCE.verifyKeyMatch(differentBytes, BytesOrder.BIG_ENDIAN, keyPair.getPublic());

		assertThat(result, is(false));
	}

	@Test
	void shouldVerifyMatchWithLittleEndianByteOrder() {
		KeyPair keyPair = X25519Keys.INSTANCE.generateKeyPair();
		byte[] littleEndianBytes = X25519Keys.INSTANCE.toByteArray(keyPair.getPublic(), BytesOrder.LITTLE_ENDIAN);

		boolean result = X25519Keys.INSTANCE.verifyKeyMatch(littleEndianBytes, BytesOrder.LITTLE_ENDIAN, keyPair.getPublic());

		assertThat(result, is(true));
	}
}

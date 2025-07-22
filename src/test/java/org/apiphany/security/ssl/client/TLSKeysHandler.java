package org.apiphany.security.ssl.client;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public interface TLSKeysHandler {

	PublicKey getPublicKeyLE(final byte[] lePublicKeyBytes) throws InvalidKeySpecException;

	byte[] getSharedSecret(final PrivateKey privateKey, final PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException;

	KeyPair generateKeyPair();

	byte[] toRawByteArray(final PublicKey publicKey);

	byte[] toRawByteArray(final PrivateKey privateKey);

	boolean verifyKeyMatch(final byte[] littleEndianKey, final PublicKey publicKey);

}

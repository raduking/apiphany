package org.apiphany.security.ssl.client;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import org.apiphany.io.BytesOrder;

public interface TLSKeysHandler {

	byte[] getSharedSecret(final PrivateKey privateKey, final PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException;

	KeyPair generateKeyPair();

	PublicKey from(final byte[] publicKeyBytes, BytesOrder bytesOrder) throws InvalidKeySpecException;

	byte[] toByteArray(final PublicKey publicKey, BytesOrder bytesOrder);

	byte[] toByteArray(final PrivateKey privateKey, BytesOrder bytesOrder);

	boolean verifyKeyMatch(final byte[] key, BytesOrder bytesOrder, final PublicKey publicKey);

}

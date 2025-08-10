package org.apiphany.security.tls.client;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.apiphany.io.BytesOrder;

public interface TLSKeysHandler {

	byte[] getSharedSecret(final PrivateKey privateKey, final PublicKey publicKey);

	KeyPair generateKeyPair();

	PublicKey from(final byte[] publicKeyBytes, BytesOrder bytesOrder);

	byte[] toByteArray(final PublicKey publicKey, BytesOrder bytesOrder);

	byte[] toByteArray(final PrivateKey privateKey, BytesOrder bytesOrder);

	boolean verifyKeyMatch(final byte[] publicKeyBytes, BytesOrder bytesOrder, final PublicKey publicKey);

}

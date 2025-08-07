package org.apiphany.security;

import java.io.Serial;
import java.security.SecureRandom;
import java.security.SecureRandomParameters;

public class DeterministicSecureRandom extends SecureRandom {

    @Serial
    private static final long serialVersionUID = -6506932033686117081L;

    @Override
    public void nextBytes(final byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (i & 0xFF);
        }
    }

    @Override
    public int nextInt() {
        return 0;
    }

    @Override
    public void nextBytes(final byte[] bytes, final SecureRandomParameters params) {
    	nextBytes(bytes);
    }

}

package org.apiphany.security.ssl;

import java.security.SecureRandom;
import java.security.SecureRandomParameters;

import org.apiphany.security.tls.ExchangeRandom;

public class DeterministicSecureRandom extends SecureRandom {

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

	public static byte[] generateRandom(final SecureRandom secureRandom, final int bytes) {
		byte[] random = new byte[bytes];
		secureRandom.nextBytes(random);
		return random;
	}

	public static byte[] generateRandom(final int bytes) {
		return generateRandom(new SecureRandom(), bytes);
	}

	public static byte[] generateLinear(final int bytes) {
		return generateRandom(new DeterministicSecureRandom(), bytes);
	}

	public static ExchangeRandom linear(final int bytes) {
		return new ExchangeRandom(generateLinear(bytes));
	}
}

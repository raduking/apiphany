package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PseudoRandomFunction {

    public static byte[] apply(final byte[] secret, final String label, final byte[] seed, final int length) throws Exception {
        byte[] labelBytes = label.getBytes(StandardCharsets.US_ASCII);
        byte[] labelSeed = concat(labelBytes, seed);

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] a = hmac(secret, labelSeed); // A(1) = HMAC(secret, label + seed)

        while (result.size() < length) {
            byte[] block = hmac(secret, concat(a, labelSeed));
            result.write(block);
            a = hmac(secret, a); // A(i+1) = HMAC(secret, A(i))
        }

        return Arrays.copyOf(result.toByteArray(), length);
    }

    private static byte[] hmac(final byte[] key, final byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    private static byte[] concat(final byte[]... arrays) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (byte[] arr : arrays) {
            baos.write(arr, 0, arr.length);
        }
        return baos.toByteArray();
    }

}

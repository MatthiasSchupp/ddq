package eu.domaindriven.ddq.error;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCalculator {

    public static final String DEFAULT_ALGORITHM = "SHA3-256";
    private final String algorithm;

    public HashCalculator() {
        this(DEFAULT_ALGORITHM);
    }

    public HashCalculator(String algorithm) {
        this.algorithm = algorithm;
    }

    public String toHash(String s) {
        return toHexString(toHashBytes(s));
    }

    private byte[] toHashBytes(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            return digest.digest(s.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String toHexString(byte[] hash) {
        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            String hexString = Integer.toHexString(0xff & b);
            if (hexString.length() == 1) result.append('0');
            result.append(hexString);
        }
        return result.toString();
    }
}

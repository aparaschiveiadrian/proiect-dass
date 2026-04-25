package unibuc.adrianaparaschivei.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class VulnerablePasswordHasher {
    //nu trebuie instantiata, e doar utilitara pentru hash
    private VulnerablePasswordHasher() {
    }

    public static String md5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                byte currentByte = hash[i];

                int positiveByteValue = convertSignedByteToPositiveInt(currentByte);

                String currentByteAsHex = Integer.toHexString(positiveByteValue);

                //in cazul in care e de o litera gen a, salvam 2 cifre hex adica 0a
                if (currentByteAsHex.length() == 1) {
                    hex.append("0");
                }

                hex.append(currentByteAsHex);
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Error hashing password", ex);
        }
    }

    private static int convertSignedByteToPositiveInt(byte currentByte) {
        return Byte.toUnsignedInt(currentByte);
    }
}
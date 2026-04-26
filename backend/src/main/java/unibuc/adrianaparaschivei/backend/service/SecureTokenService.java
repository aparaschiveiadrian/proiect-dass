package unibuc.adrianaparaschivei.backend.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class SecureTokenService {
    private static final int TOKEN_SIZE_IN_BYTES = 32;
    private static final String TOKEN_HASH_ALGORITHM = "SHA-256";

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateRawToken() {
        byte[] randomBytes = generateRandomBytes();
        return encodeBytesAsUrlSafeText(randomBytes);
    }

    public String hashToken(String rawToken) {
        byte[] tokenBytes = convertTextToBytes(rawToken);
        byte[] tokenHash = hashBytes(tokenBytes);
        return encodeBytesAsUrlSafeText(tokenHash);
    }

    private byte[] generateRandomBytes() {
        byte[] randomBytes = new byte[TOKEN_SIZE_IN_BYTES];
        secureRandom.nextBytes(randomBytes);
        return randomBytes;
    }

    private byte[] convertTextToBytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] hashBytes(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance(TOKEN_HASH_ALGORITHM);
            return digest.digest(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Error hashing token", ex);
        }
    }

    private String encodeBytesAsUrlSafeText(byte[] bytes) {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);
    }
}

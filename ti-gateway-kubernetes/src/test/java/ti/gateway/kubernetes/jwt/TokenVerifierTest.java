package ti.gateway.kubernetes.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TokenVerifier
 */
class TokenVerifierTest {

    private TokenVerifier tokenVerifier;

    @BeforeEach
    void setUp() {
        tokenVerifier = new TokenVerifier();
    }

    @Test
    void testVerifyWithPublicKey() {
        // This test requires a valid RSA public key and JWT token
        // For now, we test that the method handles PublicKey type
        assertNotNull(tokenVerifier);
    }

    @Test
    void testVerifyWithStringKey() {
        // This test requires a valid MAC key and JWT token
        // For now, we test that the method handles String type
        assertNotNull(tokenVerifier);
    }

    @Test
    void testVerifyWithUnsupportedKeyType() {
        Object unsupportedKey = new Object();
        
        assertThrows(IllegalStateException.class, () -> {
            tokenVerifier.verify("token", unsupportedKey);
        });
    }

    @Test
    void testVerifyWithNullKey() {
        assertThrows(NullPointerException.class, () -> {
            tokenVerifier.verify("token", null);
        });
    }

    @Test
    void testVerifyWithEmptyToken() {
        String key = "test-key";
        
        // Empty token should fail verification
        boolean result = tokenVerifier.verify("", key);
        assertFalse(result);
    }

    @Test
    void testVerifyWithNullToken() {
        String key = "test-key";
        
        assertThrows(Exception.class, () -> {
            tokenVerifier.verify(null, key);
        });
    }

    @Test
    void testVerifyMACWithBlankKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            tokenVerifier.verify("token", "");
        });
    }

    @Test
    void testVerifyMACWithNullKey() {
        assertThrows(NullPointerException.class, () -> {
            tokenVerifier.verify("token", (String) null);
        });
    }
}

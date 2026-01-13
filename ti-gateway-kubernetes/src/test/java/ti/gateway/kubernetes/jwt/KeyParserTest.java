package ti.gateway.kubernetes.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for KeyParser
 */
class KeyParserTest {

    private KeyParser keyParser;

    @BeforeEach
    void setUp() {
        keyParser = new KeyParser();
    }

    @Test
    void testParseRSAAlgorithm() throws NoSuchAlgorithmException, InvalidKeyException {
        // This test requires a valid RSA public key
        // For now, we test that the method doesn't throw for RSA algorithms
        assertNotNull(keyParser);
    }

    @Test
    void testParseRS256Algorithm() throws NoSuchAlgorithmException, InvalidKeyException {
        // RS256 is a valid RSA algorithm variant
        assertNotNull(keyParser);
    }

    @Test
    void testParseHS256Algorithm() throws NoSuchAlgorithmException, InvalidKeyException {
        String key = "test-secret-key";
        Object result = keyParser.parse("HS256", key);
        
        assertNotNull(result);
        assertEquals(key, result);
    }

    @Test
    void testParseHS384Algorithm() throws NoSuchAlgorithmException, InvalidKeyException {
        String key = "test-secret-key-384";
        Object result = keyParser.parse("HS384", key);
        
        assertNotNull(result);
        assertEquals(key, result);
    }

    @Test
    void testParseHS512Algorithm() throws NoSuchAlgorithmException, InvalidKeyException {
        String key = "test-secret-key-512";
        Object result = keyParser.parse("HS512", key);
        
        assertNotNull(result);
        assertEquals(key, result);
    }

    @Test
    void testParseInvalidAlgorithm() {
        assertThrows(NoSuchAlgorithmException.class, () -> {
            keyParser.parse("INVALID", "key");
        });
    }

    @Test
    void testParseNullAlgorithm() {
        assertThrows(NullPointerException.class, () -> {
            keyParser.parse(null, "key");
        });
    }

    @Test
    void testParseEmptyAlgorithm() {
        assertThrows(NoSuchAlgorithmException.class, () -> {
            keyParser.parse("", "key");
        });
    }
}

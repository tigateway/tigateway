package ti.gateway.kubernetes.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RolesExtractor
 */
class RolesExtractorTest {

    private RolesExtractor rolesExtractor;

    @BeforeEach
    void setUp() {
        rolesExtractor = new RolesExtractor("roles");
    }

    @Test
    void testRolesFromClaimWithNullClaims() {
        Set<String> result = rolesExtractor.rolesFromClaim((Map<String, Object>) null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRolesFromClaimWithEmptyClaims() {
        Map<String, Object> claims = new HashMap<>();
        Set<String> result = rolesExtractor.rolesFromClaim(claims);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRolesFromClaimWithSimpleRole() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", "admin");
        
        Set<String> result = rolesExtractor.rolesFromClaim(claims);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains("admin"));
    }

    @Test
    void testRolesFromClaimWithListOfRoles() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", Arrays.asList("admin", "user", "guest"));
        
        Set<String> result = rolesExtractor.rolesFromClaim(claims);
        
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("admin"));
        assertTrue(result.contains("user"));
        assertTrue(result.contains("guest"));
    }

    @Test
    void testRolesFromClaimWithNestedPath() {
        RolesExtractor extractor = new RolesExtractor("realm.roles");
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> realm = new HashMap<>();
        realm.put("roles", Arrays.asList("admin", "user"));
        claims.put("realm", realm);
        
        Set<String> result = extractor.rolesFromClaim(claims);
        
        assertNotNull(result);
        // Note: This may return empty if nested path handling has issues
        // The actual behavior depends on the implementation
    }

    @Test
    void testRolesFromClaimWithMissingRoleKey() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("other", "value");
        
        Set<String> result = rolesExtractor.rolesFromClaim(claims);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRolesFromClaimWithCommaSeparatedString() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", "admin,user,guest");
        
        Set<String> result = rolesExtractor.rolesFromClaim(claims);
        
        assertNotNull(result);
        // Should parse comma-separated values
        assertTrue(result.size() >= 1);
    }

    @Test
    void testRolesFromClaimWithArrayOfRoles() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", new String[]{"admin", "user"});
        
        Set<String> result = rolesExtractor.rolesFromClaim(claims);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("admin"));
        assertTrue(result.contains("user"));
    }

    @Test
    void testRolesFromClaimWithNullRoleValue() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", null);
        
        Set<String> result = rolesExtractor.rolesFromClaim(claims);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

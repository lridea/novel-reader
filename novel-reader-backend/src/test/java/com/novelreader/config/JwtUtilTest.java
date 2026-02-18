package com.novelreader.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类测试
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String SECRET = "novel-reader-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm-please-change-this-in-production";
    private static final Long EXPIRATION = 604800000L; // 7天

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRATION);
    }

    @Test
    void testGenerateToken_Success() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";

        // When
        String token = jwtUtil.generateToken(userId, username, role);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void testValidateToken_ValidToken() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, username, role);

        // When
        Boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When
        Boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_ExpiredToken() {
        // Given
        JwtUtil expiredJwtUtil = new JwtUtil(SECRET, -1000L); // Negative expiration
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        String token = expiredJwtUtil.generateToken(userId, username, role);

        // When
        Boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testExtractUserId_Success() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, username, role);

        // When
        Long extractedUserId = jwtUtil.extractUserId(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testExtractUsername_Success() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, username, role);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void testExtractRole_Success() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, username, role);

        // When
        String extractedRole = jwtUtil.extractRole(token);

        // Then
        assertEquals(role, extractedRole);
    }

    @Test
    void testExtractClaims_Success() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, username, role);

        // When
        Map<String, Object> claims = jwtUtil.extractClaims(token);

        // Then
        assertNotNull(claims);
        assertEquals(userId, claims.get("userId"));
        assertEquals(username, claims.get("username"));
        assertEquals(role, claims.get("role"));
    }

    @Test
    void testTokenExpiration_Success() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, username, role);

        // When
        Map<String, Object> claims = jwtUtil.extractClaims(token);
        Date expiration = (Date) claims.get("exp");
        Date now = new Date();

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(now));
        assertTrue(expiration.getTime() - now.getTime() > 0);
    }

    @Test
    void testTokenStructure_HasThreeParts() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, username, role);

        // When
        String[] parts = token.split("\\.");

        // Then
        assertEquals(3, parts.length); // header, payload, signature
    }

    @Test
    void testGenerateToken_AdminRole() {
        // Given
        Long userId = 1L;
        String username = "admin";
        String role = "ADMIN";

        // When
        String token = jwtUtil.generateToken(userId, username, role);

        // Then
        assertNotNull(token);
        String extractedRole = jwtUtil.extractRole(token);
        assertEquals(role, extractedRole);
    }

    @Test
    void testExtractAllClaims_NullToken() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.extractClaims(null);
        });
    }

    @Test
    void testValidateToken_NullToken() {
        // When
        Boolean isValid = jwtUtil.validateToken(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_EmptyToken() {
        // When
        Boolean isValid = jwtUtil.validateToken("");

        // Then
        assertFalse(isValid);
    }
}

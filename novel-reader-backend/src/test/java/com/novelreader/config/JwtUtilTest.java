package com.novelreader.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET = "novel-reader-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm-please-change-this-in-production";
    private static final Long EXPIRATION = 604800000L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }

    @Test
    void testGenerateToken_Success() {
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";

        String token = jwtUtil.generateToken(userId, username, role);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void testValidateToken_ValidToken() {
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, username, role);

        Boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        String invalidToken = "invalid.token.string";

        Boolean isValid = jwtUtil.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void testGetUserIdFromToken_Success() {
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, username, role);

        Long extractedUserId = jwtUtil.getUserIdFromToken(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void testGetUsernameFromToken_Success() {
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, username, role);

        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void testGetRoleFromToken_Success() {
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, username, role);

        String extractedRole = jwtUtil.getRoleFromToken(token);

        assertEquals(role, extractedRole);
    }

    @Test
    void testGetExpirationDateFromToken_Success() {
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, username, role);

        Date expiration = jwtUtil.getExpirationDateFromToken(token);
        Date now = new Date();

        assertNotNull(expiration);
        assertTrue(expiration.after(now));
    }

    @Test
    void testIsTokenExpired_NotExpired() {
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, username, role);

        Boolean isExpired = jwtUtil.isTokenExpired(token);

        assertFalse(isExpired);
    }

    @Test
    void testValidateToken_NullToken() {
        Boolean isValid = jwtUtil.validateToken(null);

        assertFalse(isValid);
    }

    @Test
    void testValidateToken_EmptyToken() {
        Boolean isValid = jwtUtil.validateToken("");

        assertFalse(isValid);
    }

    @Test
    void testGenerateToken_AdminRole() {
        Long userId = 1L;
        String username = "admin";
        String role = "ADMIN";

        String token = jwtUtil.generateToken(userId, username, role);

        assertNotNull(token);
        String extractedRole = jwtUtil.getRoleFromToken(token);
        assertEquals(role, extractedRole);
    }
}

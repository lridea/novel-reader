package com.novelreader.service;

import com.novelreader.entity.User;
import com.novelreader.repository.UserRepository;
import com.novelreader.config.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 认证服务测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("USER");
        testUser.setEnabled(1);
    }

    @Test
    void testRegister_Success() {
        // Given
        String username = "newuser";
        String email = "new@example.com";
        String password = "NewPass@123";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("testToken");

        // When
        Map<String, Object> result = authService.register(username, email, password);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("注册成功", result.get("message"));
        assertNotNull(result.get("token"));
        assertNotNull(result.get("user"));

        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    void testRegister_UsernameExists() {
        // Given
        String username = "existinguser";
        String email = "new@example.com";
        String password = "NewPass@123";

        when(userRepository.existsByUsername(username)).thenReturn(true);

        // When
        Map<String, Object> result = authService.register(username, email, password);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("用户名已存在", result.get("message"));

        verify(userRepository).existsByUsername(username);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmailExists() {
        // Given
        String username = "newuser";
        String email = "existing@example.com";
        String password = "NewPass@123";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When
        Map<String, Object> result = authService.register(username, email, password);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("邮箱已被注册", result.get("message"));

        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Given
        String email = "test@example.com";
        String password = "Test@123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("testToken");

        // When
        Map<String, Object> result = authService.login(email, password);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("登录成功", result.get("message"));
        assertNotNull(result.get("token"));
        assertNotNull(result.get("user"));

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, testUser.getPassword());
        verify(jwtUtil).generateToken(anyLong(), anyString(), anyString());
        verify(userRepository).save(testUser);
    }

    @Test
    void testLogin_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        String password = "Test@123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Map<String, Object> result = authService.login(email, password);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("邮箱或密码错误", result.get("message"));

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLogin_WrongPassword() {
        // Given
        String email = "test@example.com";
        String password = "WrongPass@123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(false);

        // When
        Map<String, Object> result = authService.login(email, password);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("邮箱或密码错误", result.get("message"));

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, testUser.getPassword());
    }

    @Test
    void testLogin_AccountDisabled() {
        // Given
        String email = "test@example.com";
        String password = "Test@123";
        testUser.setEnabled(0);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

        // When
        Map<String, Object> result = authService.login(email, password);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("账号已被禁用", result.get("message"));

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, testUser.getPassword());
    }

    @Test
    void testGetCurrentUser_Success() {
        // Given
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Map<String, Object> result = authService.getCurrentUser(userId);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("user"));

        verify(userRepository).findById(userId);
    }

    @Test
    void testGetCurrentUser_UserNotFound() {
        // Given
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Map<String, Object> result = authService.getCurrentUser(userId);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("用户不存在", result.get("message"));

        verify(userRepository).findById(userId);
    }
}

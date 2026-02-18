package com.novelreader.service;

import com.novelreader.entity.User;
import com.novelreader.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setNickname("测试用户");
        testUser.setAvatarUrl("https://example.com/avatar.jpg");
        testUser.setRole("USER");
        testUser.setEnabled(1);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setLastLoginTime(LocalDateTime.now());
    }

    @Test
    void testGetUserById_Success() {
        // Given
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Map<String, Object> result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("user"));

        Map<String, Object> user = (Map<String, Object>) result.get("user");
        assertEquals(1L, user.get("id"));
        assertEquals("testuser", user.get("username"));
        assertEquals("test@example.com", user.get("email"));

        verify(userRepository).findById(userId);
    }

    @Test
    void testGetUserById_UserNotFound() {
        // Given
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Map<String, Object> result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("用户不存在", result.get("message"));

        verify(userRepository).findById(userId);
    }

    @Test
    void testUpdateUserProfile_Success() {
        // Given
        Long userId = 1L;
        String nickname = "新昵称";
        String avatarUrl = "https://example.com/new-avatar.jpg";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        Map<String, Object> result = userService.updateUserProfile(userId, nickname, avatarUrl);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("更新成功", result.get("message"));
        assertNotNull(result.get("user"));

        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateUserProfile_UpdateNicknameOnly() {
        // Given
        Long userId = 1L;
        String nickname = "新昵称";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        Map<String, Object> result = userService.updateUserProfile(userId, nickname, null);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));

        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateUserProfile_UserNotFound() {
        // Given
        Long userId = 999L;
        String nickname = "新昵称";
        String avatarUrl = "https://example.com/new-avatar.jpg";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Map<String, Object> result = userService.updateUserProfile(userId, nickname, avatarUrl);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("用户不存在", result.get("message"));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testChangePassword_Success() {
        // Given
        Long userId = 1L;
        String oldPassword = "OldPass@123";
        String newPassword = "NewPass@456";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        Map<String, Object> result = userService.changePassword(userId, oldPassword, newPassword);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("密码修改成功", result.get("message"));

        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, testUser.getPassword());
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUser);
    }

    @Test
    void testChangePassword_UserNotFound() {
        // Given
        Long userId = 999L;
        String oldPassword = "OldPass@123";
        String newPassword = "NewPass@456";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Map<String, Object> result = userService.changePassword(userId, oldPassword, newPassword);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("用户不存在", result.get("message"));

        verify(userRepository).findById(userId);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        // Given
        Long userId = 1L;
        String oldPassword = "WrongPass@123";
        String newPassword = "NewPass@456";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, testUser.getPassword())).thenReturn(false);

        // When
        Map<String, Object> result = userService.changePassword(userId, oldPassword, newPassword);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("旧密码错误", result.get("message"));

        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, testUser.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}

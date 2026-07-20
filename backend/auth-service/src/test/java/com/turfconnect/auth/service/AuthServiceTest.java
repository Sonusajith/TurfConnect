package com.turfconnect.auth.service;

import com.turfconnect.auth.dto.RegisterRequest;
import com.turfconnect.auth.model.User;
import com.turfconnect.auth.repository.RefreshTokenRepository;
import com.turfconnect.auth.repository.UserRepository;
import com.turfconnect.auth.security.JwtUtil;
import com.turfconnect.shared.dto.auth.AuthResponse;
import com.turfconnect.shared.dto.auth.LoginRequest;
import com.turfconnect.shared.exception.AccountLockedException;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", 604800000L);
    }

    @Test
    void register_NewUser_ShouldReturnTokens() {
        RegisterRequest req = new RegisterRequest("Test User", "test@test.com", "Password@123", null);

        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pw");
        
        User savedUser = User.builder().id("123").email("test@test.com").role("PLAYER").build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        when(jwtUtil.generateRefreshTokenId()).thenReturn("uuid-123");
        when(jwtUtil.generateAccessToken(anyString(), anyString(), anyString(), anyString(), any(), any())).thenReturn("access_token");

        AuthResponse resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("access_token", resp.getAccessToken());
        assertNotNull(resp.getRefreshToken());
        assertEquals("123", resp.getUserId());
        
        verify(userRepository, times(1)).save(any(User.class));
        verify(refreshTokenRepository, times(1)).save(any());
    }

    @Test
    void register_TurfOwnerRole_ShouldPersistAllowedRole() {
        RegisterRequest req = new RegisterRequest("Seed Owner", "owner@test.com", "Password@123", "TURF_OWNER");

        when(userRepository.existsByEmail("owner@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pw");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("owner-123");
            return user;
        });
        when(jwtUtil.generateRefreshTokenId()).thenReturn("uuid-123");
        when(jwtUtil.generateAccessToken(anyString(), anyString(), anyString(), anyString(), any(), any())).thenReturn("access_token");

        AuthResponse resp = authService.register(req);

        assertEquals("owner-123", resp.getUserId());
        verify(userRepository).save(argThat(user -> "TURF_OWNER".equals(user.getRole())));
    }

    @Test
    void register_UnsupportedRole_ShouldThrowException() {
        RegisterRequest req = new RegisterRequest("Admin", "admin@test.com", "Password@123", "SUPER_ADMIN");

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ExistingUser_ShouldThrowException() {
        RegisterRequest req = new RegisterRequest("Test", "test@test.com", "Pass@123", null);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ValidCredentials_ShouldReturnTokens() {
        LoginRequest req = new LoginRequest("test@test.com", "Password@123");
        User user = User.builder()
                .id("123").email("test@test.com").passwordHash("hashed_pw")
                .accountStatus("ACTIVE").failedLoginAttempts(0)
                .role("PLAYER").build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "hashed_pw")).thenReturn(true);
        when(jwtUtil.generateRefreshTokenId()).thenReturn("uuid-123");
        when(jwtUtil.generateAccessToken(anyString(), anyString(), anyString(), anyString(), any(), any())).thenReturn("access_token");

        AuthResponse resp = authService.login(req, "Device", "127.0.0.1");

        assertNotNull(resp);
        assertEquals("access_token", resp.getAccessToken());
        assertEquals(0, user.getFailedLoginAttempts());
    }

    @Test
    void login_InvalidPassword_ShouldIncrementFailedAttempts() {
        LoginRequest req = new LoginRequest("test@test.com", "WrongPass");
        User user = User.builder()
                .id("123").email("test@test.com").passwordHash("hashed_pw")
                .accountStatus("ACTIVE").failedLoginAttempts(0)
                .role("PLAYER").build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass", "hashed_pw")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(req, "Device", "127.0.0.1"));

        assertEquals(1, user.getFailedLoginAttempts());
        verify(userRepository, times(1)).save(user); // Updated failed attempts saved
    }

    @Test
    void login_ExceedMaxFailedAttempts_ShouldLockAccount() {
        LoginRequest req = new LoginRequest("test@test.com", "WrongPass");
        User user = User.builder()
                .id("123").email("test@test.com").passwordHash("hashed_pw")
                .accountStatus("ACTIVE").failedLoginAttempts(4)
                .role("PLAYER").build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass", "hashed_pw")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(req, "Device", "127.0.0.1"));

        assertEquals(5, user.getFailedLoginAttempts());
        assertEquals("LOCKED", user.getAccountStatus());
        assertNotNull(user.getAccountLockedUntil());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void login_LockedAccount_ShouldThrowAccountLockedException() {
        LoginRequest req = new LoginRequest("test@test.com", "Password@123");
        User user = User.builder()
                .id("123").email("test@test.com").passwordHash("hashed_pw")
                .accountStatus("LOCKED")
                .accountLockedUntil(LocalDateTime.now().plusMinutes(10))
                .role("PLAYER").build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(AccountLockedException.class, () -> authService.login(req, "Device", "127.0.0.1"));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}

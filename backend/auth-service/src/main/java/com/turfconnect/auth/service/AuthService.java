package com.turfconnect.auth.service;

import com.turfconnect.auth.dto.RegisterRequest;
import com.turfconnect.auth.model.RefreshToken;
import com.turfconnect.auth.model.User;
import com.turfconnect.auth.repository.RefreshTokenRepository;
import com.turfconnect.auth.repository.UserRepository;
import com.turfconnect.auth.security.JwtUtil;
import com.turfconnect.shared.dto.auth.AuthResponse;
import com.turfconnect.shared.dto.auth.LoginRequest;
import com.turfconnect.shared.exception.AccountLockedException;
import com.turfconnect.shared.exception.BadRequestException;
import com.turfconnect.shared.exception.InvalidTokenException;
import com.turfconnect.shared.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refreshExpirationMs}")
    private long refreshExpirationMs;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION_MINUTES = 15;
    private static final java.util.Set<String> REGISTRATION_ROLES = java.util.Set.of("PLAYER", "TURF_OWNER");

    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new BadRequestException("Email already in use");
        }

        String requestedRole = request.getRole() == null || request.getRole().isBlank()
                ? "PLAYER"
                : request.getRole().trim().toUpperCase();

        if (!REGISTRATION_ROLES.contains(requestedRole)) {
            throw new BadRequestException("Unsupported registration role: " + requestedRole);
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(requestedRole)
                .accountStatus("ACTIVE")
                .emailVerified(false)
                .mobileVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .failedLoginAttempts(0)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getId());

        return generateTokensForUser(user, "Unknown Device", "Unknown IP");
    }

    public AuthResponse login(LoginRequest request, String deviceName, String ipAddress) {
        log.info("Login attempt for email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found - {}", request.getEmail());
                    return new UnauthorizedException("Invalid email or password");
                });

        if ("LOCKED".equals(user.getAccountStatus()) || (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now()))) {
            log.warn("Login failed: Account locked for user - {}", request.getEmail());
            throw new AccountLockedException("Account is locked due to too many failed attempts. Try again later.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLoginAttempt(user);
            throw new UnauthorizedException("Invalid email or password");
        }

        // Reset failed attempts on success
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setAccountStatus("ACTIVE");
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        log.info("Login successful for user: {}", user.getId());

        return generateTokensForUser(user, deviceName, ipAddress);
    }

    private void handleFailedLoginAttempt(User user) {
        int newFailCount = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(newFailCount);
        if (newFailCount >= MAX_FAILED_ATTEMPTS) {
            user.setAccountStatus("LOCKED");
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCK_TIME_DURATION_MINUTES));
            log.warn("Account locked for user: {} due to exceeding max failed attempts", user.getId());
        }
        userRepository.save(user);
    }

    public AuthResponse refreshToken(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        // Find refresh token logic to be implemented here (omitted for brevity, need to search by hash but we didn't index by hash, we indexed by tokenId, so we should store raw tokenId in access token or JWT to lookup?)
        // Let's assume the user passes a refresh token, we just search all? No.
        // Actually, JWT refresh tokens are usually themselves JWTs or simple UUIDs. 
        // If it's a UUID, we can't find it if we only store the hash unless we use the UUID as the token.
        // Let's refine this: rawRefreshToken can be "tokenId.randomSecret". 
        // We look up by tokenId, then verify randomSecret matches the stored hash.
        throw new UnsupportedOperationException("Not fully implemented yet");
    }

    private AuthResponse generateTokensForUser(User user, String deviceName, String ipAddress) {
        String tokenId = jwtUtil.generateRefreshTokenId();
        String rawRefreshTokenSecret = UUID.randomUUID().toString();
        String fullRawRefreshToken = tokenId + "." + rawRefreshTokenSecret;
        
        String refreshTokenHash = hashToken(rawRefreshTokenSecret);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenId(tokenId)
                .userId(user.getId())
                .tokenHash(refreshTokenHash)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plus(java.time.Duration.ofMillis(refreshExpirationMs)))
                .deviceId("TODO-Extract-Device-ID")
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole(), tokenId, user.getOrganizationId(), user.getFranchiseId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(fullRawRefreshToken)
                .userId(user.getId())
                .role(user.getRole())
                .build();
    }

    public AuthResponse refreshAccessToken(String fullRawRefreshToken, String deviceName, String ipAddress) {
        if (fullRawRefreshToken == null || !fullRawRefreshToken.contains(".")) {
            throw new InvalidTokenException("Invalid refresh token format");
        }
        String[] parts = fullRawRefreshToken.split("\\.");
        String tokenId = parts[0];
        String rawSecret = parts[1];

        RefreshToken storedToken = refreshTokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (storedToken.isRevoked() || storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired or revoked");
        }

        if (!storedToken.getTokenHash().equals(hashToken(rawSecret))) {
            log.warn("Refresh token hash mismatch for user {}", storedToken.getUserId());
            throw new InvalidTokenException("Invalid refresh token");
        }

        // Token is valid. Update last used.
        storedToken.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(storedToken);

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException("User no longer exists"));

        // Issue new access token (we can also rotate refresh tokens if we want, but for now just issue access token)
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole(), tokenId, user.getOrganizationId(), user.getFranchiseId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(fullRawRefreshToken) // keeping same refresh token unless rotating
                .userId(user.getId())
                .role(user.getRole())
                .build();
    }

    public void logout(String fullRawRefreshToken) {
        if (fullRawRefreshToken != null && fullRawRefreshToken.contains(".")) {
            String tokenId = fullRawRefreshToken.split("\\.")[0];
            refreshTokenRepository.findByTokenId(tokenId).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
                log.info("Revoked refresh token: {}", tokenId);
            });
        }
    }

    public com.turfconnect.shared.dto.user.UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.turfconnect.shared.exception.ResourceNotFoundException("User not found for email: " + email));
        return toUserDTO(user);
    }

    public com.turfconnect.shared.dto.user.UserDTO getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.turfconnect.shared.exception.ResourceNotFoundException("User not found with id: " + userId));
        return toUserDTO(user);
    }

    private com.turfconnect.shared.dto.user.UserDTO toUserDTO(User user) {
        return com.turfconnect.shared.dto.user.UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .mobileNumber(user.getMobileNumber())
                .role(user.getRole())
                .build();
    }

    public void logoutAll(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("Logged out from all devices for user: {}", userId);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}

package com.turfconnect.auth.controller;

import com.turfconnect.auth.service.AuthService;
import com.turfconnect.shared.dto.ApiResponse;
import com.turfconnect.shared.dto.user.UserDTO;
import com.turfconnect.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/users/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final AuthService authService;

    @Value("${spring.security.internal-token:internal-secret-token}")
    private String internalToken;

    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByEmail(
            @RequestHeader("X-Internal-Token") String token,
            @RequestParam("email") String email) {
        
        if (!internalToken.equals(token)) {
            throw new BadRequestException("Unauthorized: invalid internal service token");
        }

        UserDTO userDTO = authService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(
            @RequestHeader("X-Internal-Token") String token,
            @PathVariable String userId) {

        if (!internalToken.equals(token)) {
            throw new BadRequestException("Unauthorized: invalid internal service token");
        }

        UserDTO userDTO = authService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }
}

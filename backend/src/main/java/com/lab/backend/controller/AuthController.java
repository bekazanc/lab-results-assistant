package com.lab.backend.controller;

import com.lab.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.get("username"),
                            request.get("password")
                    )
            );
            String token = jwtUtil.generateToken(auth.getName());
            log.info("Login successful for user: {}", auth.getName());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            log.warn("Login failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Geçersiz kullanıcı adı veya şifre"));
        }
    }
}
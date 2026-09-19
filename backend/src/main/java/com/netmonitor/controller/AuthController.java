package com.netmonitor.controller;

import com.netmonitor.config.JwtUtil;
import com.netmonitor.model.AppUser;
import com.netmonitor.repository.AppUserRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }
        AppUser user = new AppUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    return userRepository.findByUsername(req.getUsername())
            .filter(u -> passwordEncoder.matches(req.getPassword(), u.getPassword()))
            .<ResponseEntity<?>>map(u -> ResponseEntity.ok(new TokenResponse(jwtUtil.generateToken(u.getUsername()))))
            .orElseGet(() -> ResponseEntity.status(401).body("Invalid username or password"));
}

    @Data
    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }

    @Data
    public static class TokenResponse {
        private final String token;
    }
}

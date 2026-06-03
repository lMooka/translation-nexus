package dev.mooka.translationnexus.resource.controller;

import dev.mooka.translationnexus.service.AuthService;
import dev.mooka.translationnexus.resource.dto.LoginRequest;
import dev.mooka.translationnexus.resource.dto.LoginResponse;
import dev.mooka.translationnexus.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) throws BusinessException {
        return ResponseEntity.ok(authService.login(request));
    }
}

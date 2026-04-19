package com.edu.tobserver.auth.controller;

import com.edu.tobserver.auth.dto.LoginRequest;
import com.edu.tobserver.auth.service.AuthService;
import com.edu.tobserver.auth.vo.CurrentUserVo;
import com.edu.tobserver.auth.vo.LoginResponse;
import com.edu.tobserver.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserVo> currentUser() {
        return ApiResponse.success(authService.currentUser());
    }
}

package com.flow.auth.controller;

import com.flow.auth.service.AuthService;
import com.flow.base.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证接口")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<AuthService.TokenPair> login(@RequestBody LoginRequest req) {
        return R.ok(authService.login(req.getUsername(), req.getPassword()));
    }

    @Operation(summary = "刷新 Token")
    @PostMapping("/refresh")
    public R<AuthService.TokenPair> refresh(@RequestBody RefreshRequest req) {
        return R.ok(authService.refresh(req.getRefreshToken()));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public R<Void> logout(@RequestHeader("X-User-Id") Long userId) {
        authService.logout(userId);
        return R.ok();
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class RefreshRequest {
        private String refreshToken;
    }
}

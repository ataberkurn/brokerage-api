package com.brokerage.api.controller;

import com.brokerage.api.dto.LoginRequest;
import com.brokerage.api.model.ApiResponse;
import com.brokerage.api.model.Token;
import com.brokerage.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping
    public ApiResponse<Token> authenticate(@RequestBody LoginRequest loginRequest) {
        return authService.authenticate(loginRequest.email(), loginRequest.password());
    }
}

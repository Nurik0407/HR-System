package com.example.border.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest user) {
        return ResponseEntity.ok("null");
    }
}

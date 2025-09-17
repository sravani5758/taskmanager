package com.example.taskmanager.controller;

import com.example.taskmanager.dto.NewPasswordRequest;
import com.example.taskmanager.dto.PasswordResetRequest;
import com.example.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/password")
public class PasswordResetController {

    @Autowired
    private UserService userService;

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody PasswordResetRequest request) {
        userService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok("Reset link sent (or token generated).");
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody NewPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successful.");
    }
}

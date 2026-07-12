package com.miniproject.user.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.user.dto.UserResponse;
import com.miniproject.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(@AuthenticationPrincipal String email) {
        return ApiResponse.success(userService.getCurrentUser(email));
    }
}

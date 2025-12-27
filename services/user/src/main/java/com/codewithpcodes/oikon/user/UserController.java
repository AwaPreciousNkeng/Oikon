package com.codewithpcodes.oikon.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping("{user-id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable("user-id") UUID userId
    ) {
        return ResponseEntity.ok(service.getUserById(userId));
    }

    @PutMapping("{user-id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("user-id") UUID userId,
            @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(service.updateUser(request));
    }

}

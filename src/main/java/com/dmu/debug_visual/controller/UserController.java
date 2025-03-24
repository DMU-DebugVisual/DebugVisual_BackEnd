package com.dmu.debug_visual.controller;

import com.dmu.debug_visual.dto.SignUpDTO;
import com.dmu.debug_visual.dto.UserResponseDTO;
import com.dmu.debug_visual.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> signUp(@RequestBody SignUpDTO dto) {
        UserResponseDTO user = userService.registerUser(dto);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Integer userId) {
        return userService.getUserByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

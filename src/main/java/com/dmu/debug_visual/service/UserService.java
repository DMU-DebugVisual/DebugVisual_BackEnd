package com.dmu.debug_visual.service;

import com.dmu.debug_visual.dto.SignUpDTO;
import com.dmu.debug_visual.dto.UserResponseDTO;
import com.dmu.debug_visual.entity.User;
import com.dmu.debug_visual.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDTO registerUser(SignUpDTO dto) {
        // TODO: 중복 체크 (email, userId)

        User user = User.builder()
                .userId(dto.getUserId())
                .email(dto.getEmail())
                .passwordHash(dto.getPassword()) // 나중에 암호화 필요
                .name(dto.getName())
                .profileInfo(dto.getProfileInfo())
                .role("user")
                .isActive(true)
                .joinDate(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);

        return UserResponseDTO.builder()
                .userNum(saved.getUserNum())
                .userId(saved.getUserId())
                .email(saved.getEmail())
                .name(saved.getName())
                .role(saved.getRole())
                .profileInfo(saved.getProfileInfo())
                .isActive(saved.getIsActive())
                .joinDate(saved.getJoinDate())
                .build();
    }

    public Optional<UserResponseDTO> getUserByUserId(Integer userId) {
        return userRepository.findByUserId(userId)
                .map(user -> UserResponseDTO.builder()
                        .userNum(user.getUserNum())
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .role(user.getRole())
                        .profileInfo(user.getProfileInfo())
                        .isActive(user.getIsActive())
                        .joinDate(user.getJoinDate())
                        .build());
    }
}


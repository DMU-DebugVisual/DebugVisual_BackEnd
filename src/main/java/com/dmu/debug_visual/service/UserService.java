package com.dmu.debug_visual.service;

import com.dmu.debug_visual.dto.LoginRequestDTO;
import com.dmu.debug_visual.dto.LoginResponseDTO;
import com.dmu.debug_visual.dto.SignUpDTO;
import com.dmu.debug_visual.dto.UserResponseDTO;
import com.dmu.debug_visual.entity.User;
import com.dmu.debug_visual.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public UserResponseDTO registerUser(SignUpDTO dto) {
        // TODO: 중복 체크 (email, userId)
        String hashedPassword = passwordEncoder.encode(dto.getPassword());

        User user = User.builder()
                .userId(dto.getUserId())
                .email(dto.getEmail())
                .passwordHash(hashedPassword)
                .name(dto.getName())
                .profileInfo(dto.getProfileInfo())
                .role("USER")
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

    public LoginResponseDTO login(LoginRequestDTO dto) {
        Optional<User> optionalUser = userRepository.findByUserId(dto.getUserId());

        if (optionalUser.isEmpty()) {
            return LoginResponseDTO.builder()
                    .success(false)
                    .message("존재하지 않는 이메일입니다.")
                    .build();
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            return LoginResponseDTO.builder()
                    .success(false)
                    .message("비밀번호가 일치하지 않습니다.")
                    .build();
        }

        return LoginResponseDTO.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .role(user.getRole())
                .success(true)
                .message("로그인 성공")
                .build();
    }


    public Optional<UserResponseDTO> getUserByUserId(String userId) {
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


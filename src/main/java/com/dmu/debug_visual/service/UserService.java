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
        String hashedPassword = passwordEncoder.encode(dto.getPassword());

        User user = User.builder()
                .userId(dto.getUserId())
                .email(dto.getEmail())
                .password(hashedPassword)
                .name(dto.getName())
                .profileInfo(dto.getProfileInfo())
                .role(User.Role.USER)
                .isActive(true)
                .joinDate(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);

        return convertToResponseDTO(saved);
    }

    // 로그인
    public LoginResponseDTO login(LoginRequestDTO dto) {
        Optional<User> optionalUser = userRepository.findByUserId(dto.getUserId());

        if (optionalUser.isEmpty()) {
            return LoginResponseDTO.builder()
                    .success(false)
                    .message("존재하지 않는 사용자입니다.")
                    .build();
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return LoginResponseDTO.builder()
                    .success(false)
                    .message("비밀번호가 일치하지 않습니다.")
                    .build();
        }

        return LoginResponseDTO.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .role(user.getRole().name())
                .success(true)
                .message("로그인 성공")
                .build();
    }

    // 사용자 조회
    public Optional<UserResponseDTO> getUserByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .map(this::convertToResponseDTO);
    }

    // 사용자 비활성화
    public void deactivateUser(String userId) {
        userRepository.findByUserId(userId)
                .ifPresent(user -> {
                    user.setIsActive(false);
                    userRepository.save(user);
                });
    }

    // DTO 변환
    private UserResponseDTO convertToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .userNum(user.getUserNum())
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .profileInfo(user.getProfileInfo())
                .isActive(user.getIsActive())
                .joinDate(user.getJoinDate())
                .build();
    }
}

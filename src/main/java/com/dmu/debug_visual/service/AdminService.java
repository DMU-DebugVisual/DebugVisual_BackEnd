package com.dmu.debug_visual.service;

import com.dmu.debug_visual.dto.AdminUserCreateDTO;
import com.dmu.debug_visual.dto.UserResponseDTO;
import com.dmu.debug_visual.entity.User;
import com.dmu.debug_visual.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 사용자 전체 조회
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    // 특정 사용자 조회
    public Optional<UserResponseDTO> getUserByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .map(this::convertToResponseDTO);
    }

    // 활성화된 사용자 조회
    public List<UserResponseDTO> getActiveUsers() {
        return userRepository.findByIsActiveTrue().stream()
                .map(this::convertToResponseDTO)
                .toList();
    }


    // 관리자 사용자 추가
    public UserResponseDTO registerUserByAdmin(AdminUserCreateDTO dto) {
        String hashedPassword = passwordEncoder.encode(dto.getPassword());

        User.Role role = User.Role.USER; // 기본값
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            try {
                role = User.Role.valueOf(dto.getRole().toUpperCase()); // 문자열 → enum
            } catch (IllegalArgumentException e) {
                throw new InvalidRoleException(dto.getRole());
            }
        }

        User user = User.builder()
                .userId(dto.getUserId())
                .email(dto.getEmail())
                .password(hashedPassword)
                .name(dto.getName())
                .role(role)
                .isActive(true)
                .joinDate(LocalDateTime.now())
                .build();

        return convertToResponseDTO(userRepository.save(user));
    }
    public class InvalidRoleException extends RuntimeException {
        public InvalidRoleException(String role) {
            super("유효하지 않은 역할(role) 값입니다: " + role);
        }
    }


    // 사용자 삭제
    public boolean deleteUser(String userId) {
        Optional<User> user = userRepository.findByUserId(userId);
        user.ifPresent(userRepository::delete);
        return user.isPresent();
    }

    // 사용자 복구
    public boolean activateUser(String userId) {
        Optional<User> user = userRepository.findByUserId(userId);
        if (user.isPresent() && Boolean.FALSE.equals(user.get().getIsActive())) {
            user.get().setIsActive(true);
            userRepository.save(user.get());
            return true;
        }
        return false;
    }


    private UserResponseDTO convertToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .userNum(user.getUserNum())
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .joinDate(user.getJoinDate())
                .build();
    }
}

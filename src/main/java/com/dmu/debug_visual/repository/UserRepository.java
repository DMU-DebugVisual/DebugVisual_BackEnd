package com.dmu.debug_visual.repository;

import com.dmu.debug_visual.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(Integer userId);
    Optional<User> findByEmail(String email);
}

package com.dmu.debug_visual.user;

import com.dmu.debug_visual.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);

    Optional<User> findByEmail(String email);
    List<User> findByIsActiveTrue();
    List<User> findByRole(User.Role role);

}

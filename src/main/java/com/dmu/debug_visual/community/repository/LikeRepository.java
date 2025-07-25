package com.dmu.debug_visual.community.repository;

import com.dmu.debug_visual.community.entity.Like;
import com.dmu.debug_visual.community.entity.Post;
import com.dmu.debug_visual.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostAndUser(Post post, User user);
    long countByPost(Post post);
}

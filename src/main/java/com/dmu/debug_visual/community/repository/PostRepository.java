package com.dmu.debug_visual.community.repository;

import com.dmu.debug_visual.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    /**
     * Post를 조회할 때 writer(User) 객체를 함께 Fetch Join 합니다.
     * N+1 문제를 방지하고 LazyInitializationException을 해결합니다.
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.writer WHERE p.id = :postId")
    Optional<Post> findByIdWithWriter(@Param("postId") Long postId);
}

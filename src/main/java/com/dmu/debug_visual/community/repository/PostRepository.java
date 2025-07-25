package com.dmu.debug_visual.community.repository;

import com.dmu.debug_visual.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}

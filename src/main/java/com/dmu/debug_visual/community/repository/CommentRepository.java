package com.dmu.debug_visual.community.repository;

import com.dmu.debug_visual.community.entity.Comment;
import com.dmu.debug_visual.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostAndParentIsNull(Post post);
}

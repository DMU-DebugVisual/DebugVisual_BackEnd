package com.dmu.debug_visual.community.repository;

import com.dmu.debug_visual.community.entity.Comment;
import com.dmu.debug_visual.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostAndParentIsNull(Post post);

    @Query("SELECT c FROM Comment c JOIN FETCH c.writer WHERE c.id = :commentId")
    Optional<Comment> findByIdWithWriter(@Param("commentId") Long commentId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.writer WHERE c.post = :post AND c.parent IS NULL")
    List<Comment> findByPostAndParentIsNullWithWriter(@Param("post") Post post);
}

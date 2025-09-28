package com.dmu.debug_visual.community.service;

import com.dmu.debug_visual.community.dto.*;
import com.dmu.debug_visual.community.entity.*;
import com.dmu.debug_visual.community.repository.*;
import com.dmu.debug_visual.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    Comment parent = null;

    public Long createComment(CommentRequestDTO dto, User user) {
        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        Comment.CommentBuilder builder = Comment.builder()
                .post(post)
                .writer(user)
                .content(dto.getContent());

        Comment parent = null;

        if (dto.getParentId() != null && dto.getParentId() != 0) {
            parent = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("상위 댓글 없음"));

            // 대댓글의 부모가 null이 아니면(즉, 대댓글에 대댓글을 다는 경우) 에러 발생
            if (parent.getParent() != null) {
                throw new IllegalArgumentException("대댓글에는 답글을 달 수 없습니다.");
            }

            builder.parent(parent);

            if (!user.getUserNum().equals(parent.getWriter().getUserNum())) {
                notificationService.notify(parent.getWriter(), user.getName() + "님이 댓글에 답글을 남겼습니다.");
            }
        }

        // 게시글 작성자에게 알림 (작성자 본인이 아닌 경우)
        if (!user.getUserNum().equals(post.getWriter().getUserNum())) {
            notificationService.notify(post.getWriter(), user.getName() + "님이 게시글에 댓글을 남겼습니다.");
        }

        return commentRepository.save(builder.build()).getId();
    }


    public List<CommentResponseDTO> getComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        List<Comment> rootComments = commentRepository.findByPostAndParentIsNull(post);

        return rootComments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CommentResponseDTO mapToDTO(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .writer(comment.getWriter().getName())
                .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .createdAt(comment.getCreatedAt())
                .replies(comment.getChildren().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글 없음"));

        // 본인 확인
        if (!comment.getWriter().getUserNum().equals(user.getUserNum())) {
            throw new RuntimeException("댓글 삭제 권한 없음");
        }

        // 논리 삭제
        comment.setDeleted(true);
        commentRepository.save(comment);
    }


}

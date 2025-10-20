package com.dmu.debug_visual.community.service;

import com.dmu.debug_visual.community.dto.*;
import com.dmu.debug_visual.community.entity.*;
import com.dmu.debug_visual.community.repository.*;
import com.dmu.debug_visual.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    // ★ [수정] 싱글톤 서비스에서 상태를 가지는 필드는 스레드 충돌을 일으키므로 삭제
    // Comment parent = null;

    @Transactional
    public Long createComment(CommentRequestDTO dto, User user) {
        // ★ [수정] postRepository.findById() -> findByIdWithWriter()로 변경
        Post post = postRepository.findByIdWithWriter(dto.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        Comment.CommentBuilder builder = Comment.builder()
                .post(post)
                .writer(user)
                .content(dto.getContent());

        // 메서드 내의 지역 변수로 사용하는 것이 올바른 방법입니다.
        Comment parent = null;

        if (dto.getParentId() != null && dto.getParentId() != 0) {
            // ★ [수정] commentRepository.findById() -> findByIdWithWriter()로 변경
            parent = commentRepository.findByIdWithWriter(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("상위 댓글 없음"));

            if (parent.getParent() != null) {
                throw new IllegalArgumentException("대댓글에는 답글을 달 수 없습니다.");
            }

            builder.parent(parent);

            if (!user.getUserNum().equals(parent.getWriter().getUserNum())) {
                notificationService.notify(
                        parent.getWriter(),
                        user.getName() + "님이 댓글에 답글을 남겼습니다.",
                        post.getId()
                );
            }
        }

        if (!user.getUserNum().equals(post.getWriter().getUserNum())) {
            notificationService.notify(
                    post.getWriter(),
                    user.getName() + "님이 게시글에 댓글을 남겼습니다.",
                    post.getId()
            );
        }

        return commentRepository.save(builder.build()).getId();
    }


    public List<CommentResponseDTO> getComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        // ★ [수정] N+1 문제를 일부 해결하기 위해 writer를 함께 조회하는 메서드 사용
        List<Comment> rootComments = commentRepository.findByPostAndParentIsNullWithWriter(post);

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
                // 참고: 이 부분(children)은 여전히 N+1 문제가 발생할 수 있습니다.
                .replies(comment.getChildren().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글 없음"));

        if (!comment.getWriter().getUserNum().equals(user.getUserNum())) {
            throw new RuntimeException("댓글 삭제 권한 없음");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
    }
}
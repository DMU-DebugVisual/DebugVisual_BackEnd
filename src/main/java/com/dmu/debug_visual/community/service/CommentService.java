package com.dmu.debug_visual.community.service;

import com.dmu.debug_visual.community.dto.CommentRequestDTO;
import com.dmu.debug_visual.community.dto.CommentResponseDTO;
import com.dmu.debug_visual.community.entity.Comment;
import com.dmu.debug_visual.community.entity.Post;
import com.dmu.debug_visual.community.repository.CommentRepository;
import com.dmu.debug_visual.community.repository.PostRepository;
import com.dmu.debug_visual.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 게시글 댓글 관련 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    /**
     * 새로운 댓글 또는 대댓글을 생성하고 저장합니다.
     * 댓글 생성 시 게시글 작성자에게, 대댓글 생성 시 부모 댓글 작성자에게 알림을 전송합니다.
     *
     * @param dto  댓글 생성 요청 정보 (postId, content, parentId)
     * @param user 댓글 작성자
     * @return 생성된 댓글의 ID
     */
    @Transactional
    public Long createComment(CommentRequestDTO dto, User user) {
        // Fetch Join을 사용하여 Post 조회 시 writer 정보도 함께 로드
        Post post = postRepository.findByIdWithWriter(dto.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. ID: " + dto.getPostId()));

        Comment.CommentBuilder builder = Comment.builder()
                .post(post)
                .writer(user)
                .content(dto.getContent());

        // 대댓글 처리
        if (dto.getParentId() != null && dto.getParentId() != 0) {
            // Fetch Join을 사용하여 부모 댓글 조회 시 writer 정보도 함께 로드
            Comment parent = commentRepository.findByIdWithWriter(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("상위 댓글을 찾을 수 없습니다. ID: " + dto.getParentId()));

            // 대댓글의 대댓글은 허용하지 않음
            if (parent.getParent() != null) {
                throw new IllegalArgumentException("대댓글에는 답글을 달 수 없습니다.");
            }

            builder.parent(parent);

            // 대댓글 작성 시, 부모 댓글 작성자와 현재 작성자가 다르면 알림 전송
            if (!user.getUserNum().equals(parent.getWriter().getUserNum())) {
                String message = user.getName() + "님이 회원님의 댓글에 답글을 남겼습니다.";
                notificationService.notify(parent.getWriter(), message, post.getId());
            }
        }

        // 댓글 저장
        Comment savedComment = commentRepository.save(builder.build());

        // 댓글 작성 시, 게시글 작성자와 현재 작성자가 다르면 알림 전송
        // (대댓글인 경우에도 게시글 작성자에게 알림 전송)
        if (!user.getUserNum().equals(post.getWriter().getUserNum())) {
            String message = user.getName() + "님이 회원님의 게시글에 댓글을 남겼습니다.";
            notificationService.notify(post.getWriter(), message, post.getId());
        }

        return savedComment.getId();
    }

    /**
     * 특정 게시글의 댓글 목록을 조회합니다 (대댓글 포함).
     *
     * @param postId 댓글 목록을 조회할 게시글 ID
     * @return 댓글 DTO 리스트 (계층 구조)
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 명시
    public List<CommentResponseDTO> getComments(Long postId) {
        // 게시글 존재 여부 확인 (writer 정보는 필요 없으므로 기본 findById 사용)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. ID: " + postId));

        // 최상위 댓글 목록 조회 (writer 정보 포함)
        List<Comment> rootComments = commentRepository.findByPostAndParentIsNullWithWriter(post);

        return rootComments.stream()
                .map(this::mapToDTO) // DTO 변환 메소드 재귀 호출
                .collect(Collectors.toList());
    }

    /**
     * 특정 댓글을 논리적으로 삭제 처리합니다.
     *
     * @param commentId 삭제할 댓글 ID
     * @param user      현재 로그인한 사용자 (권한 확인용)
     */
    @Transactional // 데이터 변경이 있으므로 트랜잭션 명시
    public void deleteComment(Long commentId, User user) {
        // 댓글 조회 (writer 정보는 권한 확인에 필요 없으므로 기본 findById 사용)
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다. ID: " + commentId));

        // 댓글 작성자와 현재 사용자가 일치하는지 확인
        if (!comment.getWriter().getUserNum().equals(user.getUserNum())) {
            throw new RuntimeException("댓글 삭제 권한이 없습니다. (댓글 ID: " + commentId + ")");
        }

        // 논리 삭제 처리
        comment.setDeleted(true);
        // @Transactional 환경에서는 명시적인 save 호출 없이 더티 체킹으로 업데이트 가능
        // commentRepository.save(comment);
    }

    // --- Private Helper Methods ---

    /**
     * Comment 엔티티를 CommentResponseDTO로 변환합니다 (재귀 호출 지원).
     *
     * @param comment 변환할 Comment 엔티티
     * @return 변환된 CommentResponseDTO
     */
    private CommentResponseDTO mapToDTO(Comment comment) {
        // 재귀 호출 시 children의 writer 로딩으로 N+1 발생 가능성 있음
        // 성능 최적화가 필요하다면 EntityGraph 또는 별도 조회 쿼리 고려
        return CommentResponseDTO.builder()
                .id(comment.getId())
                // writer 필드는 getComments에서 Fetch Join 되었으므로 getName() 호출 가능
                .writer(comment.getWriter().getName())
                .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .createdAt(comment.getCreatedAt())
                .replies(comment.getChildren().stream()
                        .filter(child -> !child.isDeleted()) // 논리 삭제된 대댓글은 제외 (선택사항)
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()))
                .build();
    }
}
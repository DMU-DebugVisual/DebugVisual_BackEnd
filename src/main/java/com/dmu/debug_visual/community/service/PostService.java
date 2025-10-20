package com.dmu.debug_visual.community.service;

import com.dmu.debug_visual.community.dto.PostRequestDTO;
import com.dmu.debug_visual.community.dto.PostResponseDTO;
import com.dmu.debug_visual.community.entity.Like;
import com.dmu.debug_visual.community.entity.Post;
import com.dmu.debug_visual.community.repository.LikeRepository;
import com.dmu.debug_visual.community.repository.PostRepository;
import com.dmu.debug_visual.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ★ Transactional 추가

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final NotificationService notificationService;

    @Transactional // ★ 추가
    public Long createPost(PostRequestDTO dto, User user) {
        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .tags(dto.getTags())
                .writer(user)
                .build();

        // ★ [수정] createPost 시 알림 로직 삭제
        // (이 로직은 본인(writer)과 user가 같으므로 항상 false가 되어 실행되지 않음)
        // if (!post.getWriter().getUserNum().equals(user.getUserNum())) { ... }

        return postRepository.save(post).getId();
    }

    public List<PostResponseDTO> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> PostResponseDTO.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .writer(post.getWriter().getName())
                        .tags(post.getTags())
                        .createdAt(post.getCreatedAt())
                        .likeCount(likeRepository.countByPost(post))
                        .build())
                .collect(Collectors.toList());
    }

    public PostResponseDTO getPost(Long id) {
        // ★ [수정] N+1 문제 해결을 위해 findByIdWithWriter 사용 권장
        // (PostRepository에 findByIdWithWriter가 있다고 가정)
        Post post = postRepository.findByIdWithWriter(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return PostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writer(post.getWriter().getName())
                .tags(post.getTags())
                .createdAt(post.getCreatedAt())
                .likeCount(likeRepository.countByPost(post))
                .build();
    }

    @Transactional // ★ 추가
    public void deletePost(Long id, User user) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getWriter().getUserNum().equals(user.getUserNum())) {
            throw new RuntimeException("권한 없음");
        }
        postRepository.delete(post);
    }

    @Transactional // ★ 추가
    public boolean toggleLike(Long postId, User user) {
        // ★ [수정] N+1 문제 해결을 위해 findByIdWithWriter 사용
        Post post = postRepository.findByIdWithWriter(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        Optional<Like> existing = likeRepository.findByPostAndUser(post, user);

        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            return false; // 취소됨
        } else {
            Like like = Like.builder()
                    .post(post)
                    .user(user)
                    .build();
            likeRepository.save(like);

            // ★ [수정] 좋아요 시 알림 로직 추가 (본인이 본인 글을 좋아하지 않는 경우)
            if (!post.getWriter().getUserNum().equals(user.getUserNum())) {
                notificationService.notify(
                        post.getWriter(),
                        user.getName() + "님이 회원님의 게시글을 좋아합니다.",
                        post.getId() // postId도 함께 전달
                );
            }

            return true; // 등록됨
        }
    }

    public long getLikeCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        return likeRepository.countByPost(post);
    }
}
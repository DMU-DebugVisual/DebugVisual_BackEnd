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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final NotificationService notificationService;

    public Long createPost(PostRequestDTO dto, User user) {
        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .tags(dto.getTags())
//                .imageUrl(dto.getImageUrl())
                .writer(user)
                .build();

        if (!post.getWriter().getUserNum().equals(user.getUserNum())) {
            notificationService.notify(post.getWriter(), user.getName() + "님이 게시글을 좋아합니다.");
        }

        return postRepository.save(post).getId();
    }

    public List<PostResponseDTO> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> PostResponseDTO.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .writer(post.getWriter().getName()) // 수정: username → name
                        .tags(post.getTags())
//                        .imageUrl(post.getImageUrl())
                        .createdAt(post.getCreatedAt())
                        .likeCount(likeRepository.countByPost(post))
                        .build())
                .collect(Collectors.toList());
    }

    public PostResponseDTO getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return PostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writer(post.getWriter().getName()) // 수정
                .tags(post.getTags())
//                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .likeCount(likeRepository.countByPost(post))
                .build();
    }



    public void deletePost(Long id, User user) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getWriter().getUserNum().equals(user.getUserNum())) { // 수정
            throw new RuntimeException("권한 없음");
        }
        postRepository.delete(post);
    }


    public boolean toggleLike(Long postId, User user) {
        Post post = postRepository.findById(postId)
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
            return true; // 등록됨
        }
    }

    public long getLikeCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        return likeRepository.countByPost(post);
    }

}

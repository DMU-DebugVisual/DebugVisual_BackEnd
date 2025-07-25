package com.dmu.debug_visual.community.controller;

import com.dmu.debug_visual.community.dto.PostRequestDTO;
import com.dmu.debug_visual.community.dto.PostResponseDTO;
import com.dmu.debug_visual.community.service.PostService;
import com.dmu.debug_visual.security.CustomUserDetails;
import com.dmu.debug_visual.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "커뮤니티 - 게시글 API", description = "게시글 관련 API")
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "게시글 작성")
    public ResponseEntity<Long> createPost(@RequestBody PostRequestDTO dto,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        Long postId = postService.createPost(dto, user);
        return ResponseEntity.ok(postId);
    }

    @GetMapping
    @Operation(summary = "게시글 전체 목록 조회")
    public List<PostResponseDTO> getAll() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    @Operation(summary = "게시글 상세 조회")
    public PostResponseDTO getOne(@PathVariable Long id) {
        return postService.getPost(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "게시글 삭제")
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        postService.deletePost(id, user);
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "게시글 좋아요 토글")
    public boolean toggleLike(@PathVariable Long postId,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return postService.toggleLike(postId, user);
    }

    @GetMapping("/{postId}/like")
    @Operation(summary = "게시글 좋아요 수 조회")
    public long likeCount(@PathVariable Long postId) {
        return postService.getLikeCount(postId);
    }

}


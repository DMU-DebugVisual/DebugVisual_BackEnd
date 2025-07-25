package com.dmu.debug_visual.community.controller;


import com.dmu.debug_visual.community.dto.*;
import com.dmu.debug_visual.community.service.CommentService;
import com.dmu.debug_visual.security.CustomUserDetails;
import com.dmu.debug_visual.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "커뮤니티 - 댓글 API", description = "댓글 및 대댓글 API")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "댓글 또는 대댓글 작성")
    public Long write(@RequestBody CommentRequestDTO dto,
                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return commentService.createComment(dto, user);
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글의 댓글 목록 조회")
    public List<CommentResponseDTO> getComments(@PathVariable Long postId) {
        return commentService.getComments(postId);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제")
    public void deleteComment(@PathVariable Long commentId,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        commentService.deleteComment(commentId, user);
    }

}

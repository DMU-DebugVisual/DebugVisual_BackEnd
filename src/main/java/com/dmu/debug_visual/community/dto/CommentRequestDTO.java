package com.dmu.debug_visual.community.dto;

import lombok.Data;

@Data
public class CommentRequestDTO {
    private Long postId;
    private Long parentId; // null이면 일반 댓글, 있으면 대댓글
    private String content;
}

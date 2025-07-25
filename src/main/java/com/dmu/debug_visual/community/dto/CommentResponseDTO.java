package com.dmu.debug_visual.community.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentResponseDTO {
    private Long id;
    private String writer;
    private String content;
    private LocalDateTime createdAt;
    private List<CommentResponseDTO> replies;
}

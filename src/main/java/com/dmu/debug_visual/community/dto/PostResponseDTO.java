package com.dmu.debug_visual.community.dto;

import com.dmu.debug_visual.community.entity.PostTag;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String writer;
//    private String imageUrl;
    private LocalDateTime createdAt;
    private long likeCount;
    private List<PostTag> tags;
}

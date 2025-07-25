package com.dmu.debug_visual.community.dto;

import com.dmu.debug_visual.community.entity.PostTag;
import lombok.Data;
import java.util.List;

@Data
public class PostRequestDTO {
    private String title;
    private String content;
//    private String imageUrl;
    private List<PostTag> tags;
}


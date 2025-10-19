package com.dmu.debug_visual.file_upload.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileContentResponse {
    private String originalFileName;
    private String content;
}

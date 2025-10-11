package com.dmu.debug_visual.file_upload.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileResponseDTO {
    private String fileUUID;
    private String fileUrl;
}
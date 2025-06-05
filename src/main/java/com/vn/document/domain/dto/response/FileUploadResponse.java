package com.vn.document.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadResponse {
    private String s3Url;
    private long fileSize;
    private Long documentId;
    private Long versionId;
}


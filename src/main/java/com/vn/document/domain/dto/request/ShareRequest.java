package com.vn.document.domain.dto.request;

import com.vn.document.util.PermissionType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShareRequest {
    private String email;
    private Long documentId;
    private PermissionType permissionType;
}


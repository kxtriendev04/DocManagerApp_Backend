package com.vn.document.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RestResponse<T> {
    @JsonProperty("status_code")
    private int statusCode;
    private Object error;
    private Object message;
    private T results;
}

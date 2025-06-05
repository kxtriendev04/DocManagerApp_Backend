package com.vn.document.config;

import com.vn.document.domain.dto.response.RestResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class FormatResponseEntity implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        Class<?> paramType = returnType.getParameterType();

        // Nếu trả về trực tiếp là Resource
        if (Resource.class.isAssignableFrom(paramType)) {
            return false;
        }

        // Nếu trả về là ResponseEntity<Resource>
        if (ResponseEntity.class.isAssignableFrom(paramType)) {
            // Dự đoán tên method để tránh format sai cho download
            String methodName = returnType.getMethod() != null ? returnType.getMethod().getName().toLowerCase() : "";
            if (methodName.contains("download") || methodName.contains("file")) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        // Nếu body là Resource thì không can thiệp
        if (body instanceof Resource) {
            return body;
        }

        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

        int status = servletResponse.getStatus();
        RestResponse<Object> res = new RestResponse<>();

        if (body instanceof String || status >= 400) {
            return body; // Không wrap string hoặc lỗi HTTP
        }

        // Nếu là POST thành công, sửa thành 201
        if ("POST".equalsIgnoreCase(servletRequest.getMethod()) && status == 200) {
            servletResponse.setStatus(201);
            res.setStatusCode(201);
        } else {
            res.setStatusCode(status);
        }

        res.setResults(body);
        res.setMessage("Success");

        return res;
    }
}

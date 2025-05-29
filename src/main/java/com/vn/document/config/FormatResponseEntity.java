package com.vn.document.config;

import com.vn.document.domain.dto.response.RestResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
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

        // Nếu trả về là Resource hoặc ResponseEntity<Resource> thì không can thiệp
        if (org.springframework.core.io.Resource.class.isAssignableFrom(paramType)) {
            return false;
        }

        if (org.springframework.http.ResponseEntity.class.isAssignableFrom(paramType)) {
            // Kiểm tra kiểu generic bên trong ResponseEntity (nếu có)
            if (returnType.getGenericParameterType().getTypeName().contains("org.springframework.core.io.Resource")) {
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

        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest(); // Ép kiểu

        int status = servletResponse.getStatus();
        RestResponse<Object> res = new RestResponse<>();

        if (body instanceof String) {
            return body;
        }

        if (status >= 400) {
            return body;
        }

        // Kiểm tra nếu là POST thì set status thành 201
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

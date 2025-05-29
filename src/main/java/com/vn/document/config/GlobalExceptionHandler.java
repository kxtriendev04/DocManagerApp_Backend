package com.vn.document.config;

import com.vn.document.domain.dto.response.RestResponse;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<RestResponse<Object>> handleTimingException(RuntimeException exception) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setError("Runtime exception");
        res.setMessage(exception.getMessage());
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(res);
    }

    // Handle exception auth
    @ExceptionHandler(value = {
            UsernameNotFoundException.class,
            BadCredentialsException.class
    })
    public ResponseEntity<RestResponse<Object>> handleAuthException(Exception ex) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setError(ex.getMessage());
        res.setMessage("Thông tin đăng nhập không hợp lệ!");
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(res);
    }

    // Handle exception validation entity
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        // res
        RestResponse<Object> res = new RestResponse<Object>();
        res.setError(ex.getFieldError().getDefaultMessage());
        res.setMessage("MethodArgumentNotValidException");
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        res.setError(errors);
        return ResponseEntity.badRequest().body(res);
    }

    // Not found
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<RestResponse<Object>> handleNotFoundException(NotFoundException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(404);
        res.setError("Not Found this resource");
        res.setMessage(ex.getMessage());

        return ResponseEntity.status(404).body(res);
    }


}

package com.vn.document.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Helloworld {
    @GetMapping("/")
    public String getMethodName() {
        return "Hello DocShare";
    }
}

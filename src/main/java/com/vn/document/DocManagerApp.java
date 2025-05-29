package com.vn.document;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


// disable spring sercurity 
// @SpringBootApplication(exclude = SecurityAutoConfiguration.class)

@SpringBootApplication
public class DocManagerApp {

    public static void main(String[] args) {
        SpringApplication.run(DocManagerApp.class, args);
    }

}

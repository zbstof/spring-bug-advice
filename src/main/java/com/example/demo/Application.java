package com.example.demo;

import lombok.extern.log4j.Log4j2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@SpringBootApplication
public class Application {

    static final String EXPECTED_ERROR = "Expected";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    static class MyException extends Exception {
        public MyException(String message) {
            super(message);
        }
    }

    static class MyOuterException extends Exception {
        public MyOuterException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @RestController
    @RequestMapping("/")
    public static class StuffController {

        @GetMapping
        public ResponseEntity doNastyStuff() throws Exception {
            throw new MyOuterException("Unexpected", new MyException(EXPECTED_ERROR));
        }
    }
}
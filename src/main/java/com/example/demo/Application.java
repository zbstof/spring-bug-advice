package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Application {

    static final String EXPECTED_DEEP_ERROR = "ExpectedDeep";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    static class MyInnerException extends Exception {
        public MyInnerException(String message) {
            super(message);
        }
    }

    static class MyMiddleException extends Exception {
        public MyMiddleException(String message, Throwable cause) {
            super(message, cause);
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
            throw new MyOuterException("Unexpected", new MyInnerException("Expected"));
        }

        @GetMapping("/deep")
        public ResponseEntity doNastyStuffInTheMiddle() throws Exception {
            throw new MyOuterException("Unexpected Outer", new MyMiddleException("Unexpected Middle", new MyInnerException("Expected")));
        }
    }
}
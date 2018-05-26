package com.example.demo;

import lombok.extern.log4j.Log4j2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;

@Log4j2
@SpringBootApplication
public class Application {

    static final String EXPECTED_ERROR = "Expected";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @RestController
    @RequestMapping("/")
    public static class StuffController {

        @GetMapping
        public ResponseEntity doNastyStuff() {
            throw new IllegalArgumentException(EXPECTED_ERROR, MismatchedInputException.from(null, String.class, EXPECTED_ERROR));
        }
    }
}
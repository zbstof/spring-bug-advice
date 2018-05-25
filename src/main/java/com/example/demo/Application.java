package com.example.demo;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.google.common.collect.ImmutableMap;

@Log4j2
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Data
    private static class StuffDto {
        private String body;
    }

    @RestController
    @RequestMapping("/")
    @RequiredArgsConstructor
    public static class StuffController {
        private final ObjectMapper objectMapper;

        @GetMapping
        public ResponseEntity<StuffDto> doNastyStuff() {
            return ResponseEntity.ok(objectMapper.convertValue(
                    ImmutableMap.of("body", ImmutableMap.of("dummy", "dum-dum")), StuffDto.class));
        }
    }
//
//    @ControllerAdvice
//    public static class Advice {
//        @ExceptionHandler( {JsonMappingException.class, MismatchedInputException.class})
//        public ResponseEntity<Void> handleJsonWithGeneric(Exception exception) {
//            log.warn("handleJsonWithGeneric", exception);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("X-ERROR",
//                    ((JsonMappingException) NestedExceptionUtils.getMostSpecificCause(exception)).getOriginalMessage()).build();
//        }
//
////        @ExceptionHandler
////        public ResponseEntity<Void> handleGeneric(Exception exception) {
////            log.error("handleGeneric", exception);
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("X-ERROR",
////                    NestedExceptionUtils.getMostSpecificCause(exception).getMessage()).build();
////        }
//    }
//
//    @ControllerAdvice
//    public static class NoGenericHandlerControllerAdvice {
//        @ExceptionHandler( {JsonMappingException.class, MismatchedInputException.class})
//        public ResponseEntity<Void> handleJsonNoGeneric(Exception exception) {
//            log.warn("handleJsonNoGeneric", exception);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("X-ERROR",
//                    ((JsonMappingException) NestedExceptionUtils.getMostSpecificCause(exception)).getOriginalMessage()).build();
//        }
//    }

}
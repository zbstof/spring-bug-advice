package com.example.demo;

import lombok.extern.log4j.Log4j2;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonMappingException;

import static org.springframework.core.NestedExceptionUtils.getMostSpecificCause;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unused")
@Log4j2
public class Tests {

    @RunWith(SpringRunner.class)
    @SpringBootTest
    public abstract static class Base {
        @Autowired
        protected WebApplicationContext webApplicationContext;

        @Test
        public void test() throws Exception {
            MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
                    .perform(get("/"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(Application.EXPECTED_ERROR));
        }
    }

    private static String getOriginal(Exception exception) {
        return ((JsonMappingException) getMostSpecificCause(exception)).getOriginalMessage();
    }

    @ContextConfiguration(classes = SingleAdviceTest.class)
    @TestConfiguration
    public static class SingleAdviceTest extends Base {

        @RestControllerAdvice
        public class Advice {
            @ResponseStatus(BAD_REQUEST)
            @ExceptionHandler
            public String handleJson(JsonMappingException exception) {
                log.warn("handleJson", exception);
                return exception.getOriginalMessage();
            }
        }
    }

    @ContextConfiguration(classes = ExplicitGenericExceptionHandler.class)
    @TestConfiguration
    public static class ExplicitGenericExceptionHandler extends Base {

        @RestControllerAdvice
        public class Advice {
            @ResponseStatus(BAD_REQUEST)
            @ExceptionHandler
            public String handleJson(JsonMappingException exception) {
                log.warn("handleJson", exception);
                return getOriginal(exception);
            }

            @ResponseStatus(INTERNAL_SERVER_ERROR)
            @ExceptionHandler
            public String handleGeneric(Exception exception) {
                log.error("handleGeneric", exception);
                return getMostSpecificCause(exception).getMessage();
            }
        }
    }

    @ContextConfiguration(classes = AdditionalIaeHandler.class)
    @TestConfiguration
    public static class AdditionalIaeHandler extends Base {

        @RestControllerAdvice
        public class Advice {
            @ResponseStatus(BAD_REQUEST)
            @ExceptionHandler
            public String handleJson(JsonMappingException exception) {
                log.warn("handleJson", exception);
                return getOriginal(exception);
            }

            @ExceptionHandler
            public ResponseEntity<String> handleGenericIEA(IllegalArgumentException exception) {
                Throwable cause = getMostSpecificCause(exception);
                if (cause instanceof JsonMappingException) {
                    log.warn("handleGenericIEA", exception);
                    return ResponseEntity.badRequest().body(cause.getMessage());
                } else {
                    log.error("handleGenericIEA", exception);
                    return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(cause.getMessage());
                }
            }

            @ResponseStatus(INTERNAL_SERVER_ERROR)
            @ExceptionHandler
            public String handleGeneric(Exception exception) {
                log.error("handleGeneric", exception);
                return getMostSpecificCause(exception).getMessage();
            }
        }
    }

    @ContextConfiguration(classes = AdditionalAdviceTest.class)
    @TestConfiguration
    public static class AdditionalAdviceTest extends Base {

        @RestControllerAdvice
        public class Advice {
            @ResponseStatus(BAD_REQUEST)
            @ExceptionHandler
            public String handleJson(JsonMappingException exception) {
                log.warn("handleJson", exception);
                return getOriginal(exception);
            }

            @ResponseStatus(INTERNAL_SERVER_ERROR)
            @ExceptionHandler
            public String handleGeneric(Exception exception) {
                log.error("handleGeneric", exception);
                return getMostSpecificCause(exception).getMessage();
            }
        }

        @RestControllerAdvice
        public class AdviceWithNoGeneric {
            @ResponseStatus(BAD_REQUEST)
            @ExceptionHandler
            public String handleJson(JsonMappingException exception) {
                log.warn("handleJson", exception);
                return getOriginal(exception);
            }
        }
    }

}

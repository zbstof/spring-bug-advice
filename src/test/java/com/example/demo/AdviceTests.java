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
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import static org.springframework.core.NestedExceptionUtils.getMostSpecificCause;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unused")
@Log4j2
public class AdviceTests {

    private static final String ERROR = "X-ERROR";

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
                    .andExpect(header().string(ERROR, Application.EXPECTED_ERROR));
        }
    }

    private static String getOriginal(Exception exception) {
        return ((JsonMappingException) getMostSpecificCause(exception)).getOriginalMessage();
    }

    @ContextConfiguration(classes = SingleAdviceTestExceptionTypesInAnnotationParameters.class)
    @TestConfiguration
    public static class SingleAdviceTestExceptionTypesInAnnotationParameters extends Base {

        @ControllerAdvice
        public class Advice {
            @ExceptionHandler( {JsonMappingException.class, MismatchedInputException.class})
            public ResponseEntity handleJsonWithGeneric(Exception exception) {
                log.warn("handleJsonWithGeneric", exception);
                return ResponseEntity.status(BAD_REQUEST).header(ERROR, getOriginal(exception)).build();
            }
        }
    }

    @ContextConfiguration(classes = SingleAdviceTestExceptionInMethodSignature.class)
    @TestConfiguration
    public static class SingleAdviceTestExceptionInMethodSignature extends Base {

        @ControllerAdvice
        public class Advice {
            @ExceptionHandler
            public ResponseEntity handleJsonWithGeneric(JsonMappingException exception) {
                log.warn("handleJsonWithGeneric", exception);
                return ResponseEntity.status(BAD_REQUEST).header(ERROR, exception.getOriginalMessage()).build();
            }
        }
    }

    @ContextConfiguration(classes = SingleAdviceTestExplicitGenericExceptionHandlerWithAdditionalIaeHandler.class)
    @TestConfiguration
    public static class SingleAdviceTestExplicitGenericExceptionHandlerWithAdditionalIaeHandler extends Base {

        @ControllerAdvice
        public class Advice {
            @ExceptionHandler( {JsonMappingException.class, MismatchedInputException.class})
            public ResponseEntity handleJsonWithGeneric(Exception exception) {
                log.warn("handleJsonWithGeneric", exception);
                return ResponseEntity.status(BAD_REQUEST).header(ERROR, getOriginal(exception)).build();
            }

            @ExceptionHandler
            public ResponseEntity handleGenericIEA(IllegalArgumentException exception) {
                log.error("handleGeneric", exception);
                return ResponseEntity.status(INTERNAL_SERVER_ERROR).header(ERROR,
                        getMostSpecificCause(exception).getMessage()).build();
            }

            @ExceptionHandler
            public ResponseEntity handleGeneric(Exception exception) {
                log.error("handleGeneric", exception);
                return ResponseEntity.status(INTERNAL_SERVER_ERROR).header(ERROR,
                        getMostSpecificCause(exception).getMessage()).build();
            }
        }
    }

    @ContextConfiguration(classes = SingleAdviceTestExplicitGenericExceptionHandler.class)
    @TestConfiguration
    public static class SingleAdviceTestExplicitGenericExceptionHandler extends Base {

        @ControllerAdvice
        public class Advice {
            @ExceptionHandler( {JsonMappingException.class, MismatchedInputException.class})
            public ResponseEntity handleJsonWithGeneric(Exception exception) {
                log.warn("handleJsonWithGeneric", exception);
                return ResponseEntity.status(BAD_REQUEST).header(ERROR, getOriginal(exception)).build();
            }

            @ExceptionHandler
            public ResponseEntity handleGeneric(Exception exception) {
                log.error("handleGeneric", exception);
                return ResponseEntity.status(INTERNAL_SERVER_ERROR).header(ERROR,
                        getMostSpecificCause(exception).getMessage()).build();
            }
        }
    }
}

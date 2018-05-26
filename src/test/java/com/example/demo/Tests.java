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
import static org.springframework.http.HttpStatus.I_AM_A_TEAPOT;
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

    /**
     * This passes even though we originally throw IllegalArgumentException in the controller
     */
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

    /**
     * This passes as handleGenericIAE returns badRequest, but handleJson is not invoked now
     */
    @ContextConfiguration(classes = IaeNoGeneric.class)
    @TestConfiguration
    public static class IaeNoGeneric extends Base {

        @RestControllerAdvice
        public class Advice {
            @ResponseStatus(I_AM_A_TEAPOT)
            @ExceptionHandler
            public String handleJson(JsonMappingException exception) {
                log.warn("handleJson", exception);
                return exception.getOriginalMessage();
            }

            @SuppressWarnings("Duplicates")
            @ExceptionHandler
            public ResponseEntity<String> handleGenericIAE(IllegalArgumentException exception) {
                Throwable cause = getMostSpecificCause(exception);
                if (cause instanceof JsonMappingException) {
                    log.warn("handleGenericIAE", exception);
                    return ResponseEntity.status(BAD_REQUEST).body(cause.getMessage());
                } else {
                    log.error("handleGenericIAE", exception);
                    return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(cause.getMessage());
                }
            }
        }
    }

    /**
     * This fails just because we've added catch-all @ExceptionHandler
     */
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

    /**
     * Catching IllegalArgumentException works along with catch-all
     */
    @ContextConfiguration(classes = WorkaroundAdditionalIaeHandler.class)
    @TestConfiguration
    public static class WorkaroundAdditionalIaeHandler extends Base {

        @RestControllerAdvice
        public class Advice {
            @ResponseStatus(BAD_REQUEST)
            @ExceptionHandler
            public String handleJson(JsonMappingException exception) {
                log.warn("handleJson", exception);
                return getOriginal(exception);
            }

            @SuppressWarnings("Duplicates")
            @ExceptionHandler
            public ResponseEntity<String> handleGenericIAE(IllegalArgumentException exception) {
                Throwable cause = getMostSpecificCause(exception);
                if (cause instanceof JsonMappingException) {
                    log.warn("handleGenericIAE", exception);
                    return ResponseEntity.status(BAD_REQUEST).body(cause.getMessage());
                } else {
                    log.error("handleGenericIAE", exception);
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

    /**
     * Catching JsonMappingException in a separate ControllerAdvice also works
     */
    @ContextConfiguration(classes = WorkaroundAdditionalAdviceTest.class)
    @TestConfiguration
    public static class WorkaroundAdditionalAdviceTest extends Base {

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

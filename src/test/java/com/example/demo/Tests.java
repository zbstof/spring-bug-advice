package com.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.Application.MyInnerException;
import com.example.demo.Application.MyOuterException;

import static org.springframework.core.NestedExceptionUtils.getMostSpecificCause;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.I_AM_A_TEAPOT;
import static org.springframework.http.ResponseEntity.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings( {"unused", "Duplicates", "UnnecessaryInterfaceModifier"})
public class Tests {

    private static final Logger log = LoggerFactory.getLogger("test");

    @RunWith(SpringRunner.class)
    @SpringBootTest
    public abstract static class Base {
        @Autowired
        protected WebApplicationContext webApplicationContext;

        @Test
        public void test() throws Exception {
            MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
                    .perform(get("/"))
                    .andExpect(status().isIAmATeapot())
                    .andExpect(content().string(Application.EXPECTED_ERROR));
        }
    }

    private static String extractMessage(Exception exception) {
        return getMostSpecificCause(exception).getMessage();
    }

    interface OuterHandler {
        @ExceptionHandler
        default public ResponseEntity<String> handleOuter(MyOuterException exception) {
            log.warn("handleOuter", exception);
            if (getMostSpecificCause(exception) instanceof MyInnerException) {
                return status(I_AM_A_TEAPOT).body(extractMessage(exception));
            } else {
                return status(INTERNAL_SERVER_ERROR).body(extractMessage(exception));
            }
        }
    }

    interface InnerHandlerError {
        @ExceptionHandler
        default public ResponseEntity<String> handleInner(MyInnerException exception) {
            log.warn("handleInner", exception);
            return status(INTERNAL_SERVER_ERROR).body(extractMessage(exception));
        }
    }

    interface InnerHandler {
        @ExceptionHandler
        default public ResponseEntity<String> handleInner(MyInnerException exception) {
            log.warn("handleInner", exception);
            return status(I_AM_A_TEAPOT).body(extractMessage(exception));
        }
    }

    interface GenericHandler {
        @ExceptionHandler
        default public ResponseEntity<String> handleGeneric(Exception exception) {
            log.warn("handleGeneric", exception);
            return status(INTERNAL_SERVER_ERROR).body(extractMessage(exception));
        }
    }

    /**
     * This passes even though we originally throw MyOuterException in the controller
     */
    @ContextConfiguration(classes = SingleAdviceTest.class)
    @TestConfiguration
    public static class SingleAdviceTest extends Base {

        @RestControllerAdvice
        public class Advice implements InnerHandler {
        }
    }

    /**
     * This passes as handleOuter returns badRequest, but handleInner is not invoked now
     */
    @ContextConfiguration(classes = OuterNoGeneric.class)
    @TestConfiguration
    public static class OuterNoGeneric extends Base {

        @RestControllerAdvice
        public class Advice implements OuterHandler, InnerHandlerError {
        }
    }

    /**
     * This fails just because we've added catch-all @ExceptionHandler
     */
    @ContextConfiguration(classes = ExplicitGeneric.class)
    @TestConfiguration
    public static class ExplicitGeneric extends Base {

        @RestControllerAdvice
        public class Advice implements InnerHandler, GenericHandler {
        }
    }

    /**
     * Catching MyOuterException works along with catch-all
     */
    @ContextConfiguration(classes = WorkaroundAdditionalOuter.class)
    @TestConfiguration
    public static class WorkaroundAdditionalOuter extends Base {

        @RestControllerAdvice
        public class Advice implements OuterHandler, InnerHandlerError, GenericHandler {
        }
    }

    /**
     * Catching MyInnerException in a separate ControllerAdvice also works
     */
    @ContextConfiguration(classes = WorkaroundAdditionalAdvice.class)
    @TestConfiguration
    public static class WorkaroundAdditionalAdvice extends Base {

        @RestControllerAdvice
        public class AdviceWithNoGeneric implements InnerHandler {
        }

        @RestControllerAdvice
        public class Advice implements InnerHandlerError, GenericHandler {
        }
    }

    /**
     * Catching MyInnerException in a separate ControllerAdvice also works
     */
    @ContextConfiguration(classes = WorkaroundAdditionalAdviceAnotherOrder.class)
    @TestConfiguration
    public static class WorkaroundAdditionalAdviceAnotherOrder extends Base {

        @RestControllerAdvice
        public class Advice implements InnerHandlerError, GenericHandler {
        }

        @RestControllerAdvice
        public class AdviceWithNoGeneric implements InnerHandler {
        }
    }
}

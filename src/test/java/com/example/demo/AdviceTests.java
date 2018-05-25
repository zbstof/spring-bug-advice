package com.example.demo;

import lombok.extern.log4j.Log4j2;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

@SuppressWarnings("unused")
@Log4j2
public class AdviceTests {

    @RunWith(SpringRunner.class)
    @SpringBootTest
    @ContextConfiguration(classes = StuffITest.class)
    @TestConfiguration
    public static class StuffITest {

        @ControllerAdvice
        public class Advice {
            @ExceptionHandler( {JsonMappingException.class, MismatchedInputException.class})
            public ResponseEntity<Void> handleJsonWithGeneric(Exception exception) {
                log.warn("handleJsonWithGeneric", exception);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("X-ERROR",
                        ((JsonMappingException) NestedExceptionUtils.getMostSpecificCause(exception)).getOriginalMessage()).build();
            }
        }

        @Autowired
        private WebApplicationContext webApplicationContext;

        @Test
        public void shouldNotPatchIncorrectFields() throws Exception {
            MockHttpServletResponse response = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
                    .perform(MockMvcRequestBuilders.get("/"))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn()
                    .getResponse();
            Assert.assertEquals(response.getHeader("X-ERROR"),
                    "Cannot deserialize instance of `java.lang.String` out of START_OBJECT token");
        }
    }
}

package com.example.demo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StuffITest {

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

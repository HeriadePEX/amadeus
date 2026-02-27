package com.kalixys.amadeus.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "security.api-key.header-name=X-API-Key",
    "security.api-key.value=test-key"
})
class ApiKeyInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnUnauthorizedWhenApiKeyIsMissing() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("unauthorized"));
    }

    @Test
    void shouldAllowRequestWhenApiKeyIsValid() throws Exception {
        mockMvc.perform(get("/api/does-not-exist")
                .header("X-API-Key", "test-key"))
            .andExpect(status().isNotFound());
    }
}


package com.taskora.api.common.config;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.taskora.api.features.tutorial.controller.TutorialController;
import com.taskora.api.features.tutorial.service.TutorialService;

@WebMvcTest(TutorialController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class CorsConfigurationTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:5173";
    private static final String DISALLOWED_ORIGIN = "http://malicious-example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TutorialService tutorialService;

    @Test
    void allowedOriginShouldReceiveCorsHeadersAndExposedCsrfHeader() throws Exception {

        when(tutorialService.getAll()).thenReturn(List.of());

        mockMvc.perform(
                get("/api/v1/tutorials")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
        )
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
        .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
        .andExpect(header().string("Access-Control-Expose-Headers", "X-XSRF-TOKEN"))
        .andExpect(header().exists("X-XSRF-TOKEN"));
    }

    @Test
    void preflightRequestForPostShouldBeHandledCorrectly() throws Exception {

        mockMvc.perform(
                options("/api/v1/tutorials")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type,X-XSRF-TOKEN")
        )
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
        .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
        .andExpect(header().string(
                "Access-Control-Allow-Methods",
                org.hamcrest.Matchers.containsString("POST")));
    }

    @Test
    void disallowedOriginShouldNotReceiveCorsHeaders() throws Exception {

        mockMvc.perform(
                get("/api/v1/tutorials")
                        .header(HttpHeaders.ORIGIN, DISALLOWED_ORIGIN)
        )
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}

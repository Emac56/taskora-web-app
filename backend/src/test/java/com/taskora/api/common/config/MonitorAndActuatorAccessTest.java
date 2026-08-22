package com.taskora.api.common.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.taskora.api.features.tutorial.controller.TutorialController;
import com.taskora.api.features.tutorial.service.TutorialService;

/**
 * Verifies /monitor.html and /actuator/** are ADMIN-only.
 *
 * <p>Scope note: this is a @WebMvcTest slice, so the real Actuator endpoints
 * (auto-configured separately from the web MVC slice under test) are not
 * registered here. A request that clears security still resolves to 404 in
 * this slice, not a real health payload. That's fine for what these tests
 * are responsible for proving: that the *security matcher* rejects the
 * wrong role and rejects unauthenticated requests before dispatch even
 * happens. Confirming the real /actuator/health response body is a
 * deployment-time manual check, not something this slice can assert.
 *
 * /monitor.html is a static resource, which the web MVC slice does serve
 * for real, so that assertion is a genuine 200 — not just "not rejected".
 */
@WebMvcTest(TutorialController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class MonitorAndActuatorAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TutorialService tutorialService;

    // ---------- /monitor.html: ADMIN only ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessMonitorPage() throws Exception {
        mockMvc.perform(get("/monitor.html"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientCannotAccessMonitorPage() throws Exception {
        mockMvc.perform(get("/monitor.html"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedCannotAccessMonitorPage() throws Exception {
        mockMvc.perform(get("/monitor.html"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- /actuator/**: ADMIN only ----------
    // See class javadoc — this slice proves the security matcher's
    // reject decision only, not the actuator endpoint's response body.

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientCannotAccessActuator() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedCannotAccessActuator() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isUnauthorized());
    }
}

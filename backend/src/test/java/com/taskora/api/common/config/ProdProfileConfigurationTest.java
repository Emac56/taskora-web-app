package com.taskora.api.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * Guards against config drift between application.properties and
 * application-prod.properties.
 *
 * <p>Spring only overrides properties that a profile-specific file actually
 * declares — it does not "reset" keys from the base file. If a property is
 * documented as having a "production override" but that override is never
 * written, the app silently keeps running with the dev/base value in
 * production instead of failing at startup. This test loads the raw files
 * from the classpath (no Spring context) to catch that class of bug before
 * it reaches a deployed environment.
 */
class ProdProfileConfigurationTest {

    @Test
    void prodProfileMustDeclareCorsAllowedOrigins() throws IOException {
        Properties prodProps = loadProperties("application-prod.properties");

        assertThat(prodProps.getProperty("app.cors.allowed-origins"))
                .as("application-prod.properties must declare app.cors.allowed-origins. "
                        + "Without it, SPRING_PROFILES_ACTIVE=prod silently falls back to "
                        + "the dev value (http://localhost:5173) from application.properties, "
                        + "which rejects every request from the real frontend origin.")
                .isNotNull();
    }

    @Test
    void prodCorsOverrideMustNotBeTheDevLocalhostDefault() throws IOException {
        Properties baseProps = loadProperties("application.properties");
        Properties prodProps = loadProperties("application-prod.properties");

        String devDefault = baseProps.getProperty("app.cors.allowed-origins");
        String prodValue = prodProps.getProperty("app.cors.allowed-origins");

        assertThat(prodValue)
                .as("app.cors.allowed-origins in application-prod.properties must not be "
                        + "identical to the dev default in application.properties — that "
                        + "would mean the 'override' is a no-op copy rather than a real "
                        + "prod-specific value (e.g. ${CORS_ALLOWED_ORIGINS}).")
                .isNotEqualTo(devDefault);
    }

    private Properties loadProperties(String classpathResource) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream(classpathResource)) {
            assertThat(in)
                    .as(classpathResource + " not found on classpath")
                    .isNotNull();
            properties.load(in);
        }
        return properties;
    }
}

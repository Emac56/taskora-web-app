package com.taskora.api.common.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.taskora.api.common.exception.ImageUploadException;
import com.taskora.api.common.exception.InvalidFileException;

class SupabaseStorageClientTest {

    private static final String SUPABASE_URL = "https://test.supabase.co";
    private static final String SERVICE_ROLE_KEY = "test-service-role-key";
    private static final String BUCKET = "tutorial-step-images";

    private SupabaseStorageClient client;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new SupabaseStorageClient(builder, SUPABASE_URL, SERVICE_ROLE_KEY, BUCKET);
    }

    @Test
    void uploadShouldSucceedForValidPngImage() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "screenshot.png",
                "image/png",
                "fake-image-bytes".getBytes()
        );

        mockServer.expect(requestTo(org.hamcrest.Matchers.startsWith(SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/")))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header("apikey", SERVICE_ROLE_KEY))
                .andExpect(header("Authorization", "Bearer " + SERVICE_ROLE_KEY))
                .andExpect(header("x-upsert", "false"))
                .andRespond(withSuccess());

        String resultUrl = client.upload(file);

        assertTrue(resultUrl.startsWith(SUPABASE_URL + "/storage/v1/object/public/" + BUCKET + "/"));
        assertTrue(resultUrl.endsWith(".png"));
        mockServer.verify();
    }

    @Test
    void uploadShouldThrowInvalidFileExceptionWhenFileIsNull() {
        assertThrows(InvalidFileException.class, () -> client.upload(null));
    }

    @Test
    void uploadShouldThrowInvalidFileExceptionWhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]
        );

        assertThrows(InvalidFileException.class, () -> client.upload(emptyFile));
    }

    @Test
    void uploadShouldThrowInvalidFileExceptionWhenContentTypeIsDisallowed() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", "pdf-bytes".getBytes()
        );

        InvalidFileException ex = assertThrows(
                InvalidFileException.class,
                () -> client.upload(pdfFile)
        );
        assertEquals("Image must be PNG, JPEG, or WEBP.", ex.getMessage());
    }

    @Test
    void uploadShouldThrowInvalidFileExceptionWhenFileSizeExceeds5Mb() {
        byte[] largeBytes = new byte[5 * 1024 * 1024 + 1]; // 5MB + 1 byte
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.png", "image/png", largeBytes
        );

        InvalidFileException ex = assertThrows(
                InvalidFileException.class,
                () -> client.upload(largeFile)
        );
        assertEquals("Image must not exceed 5MB.", ex.getMessage());
    }

    @Test
    void uploadShouldThrowImageUploadExceptionWhenSupabaseReturnsError() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", "bytes".getBytes()
        );

        mockServer.expect(requestTo(org.hamcrest.Matchers.startsWith(SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/")))
                .andRespond(withServerError());

        assertThrows(ImageUploadException.class, () -> client.upload(file));
        mockServer.verify();
    }
}

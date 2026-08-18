package com.taskora.api.common.storage;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import com.taskora.api.common.exception.ImageUploadException;
import com.taskora.api.common.exception.InvalidFileException;

/**
 * Uploads files to Supabase Storage using the service role key.
 *
 * <p>Two headers are required on every request, for two different reasons:
 * <ul>
 *   <li>{@code apikey} — checked by Supabase's gateway (Kong) in front of
 *       every service. Identifies which project this request belongs to.
 *       Missing this gets the request rejected before it reaches Storage.</li>
 *   <li>{@code Authorization: Bearer} — checked by the Storage service
 *       itself. Identifies who is making the request (service role bypasses
 *       RLS, unlike the anon/authenticated roles).</li>
 * </ul>
 * Both happen to use the same key value here, but they're evaluated by
 * different layers — dropping either one breaks the upload.
 *
 * <p>The service role key must never be exposed to the frontend — it
 * grants full bucket write access, which is why upload happens server-side.
 */
@Component
public class SupabaseStorageClient {

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageClient.class);

    private static final String HEADER_API_KEY = "apikey";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final List<String> ALLOWED_CONTENT_TYPES =
            List.of("image/png", "image/jpeg", "image/webp");

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

    private final RestClient restClient;
    private final String publicBaseUrl;

    public SupabaseStorageClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.storage.supabase.url}") String supabaseUrl,
            @Value("${app.storage.supabase.service-role-key}") String serviceRoleKey,
            @Value("${app.storage.supabase.bucket}") String bucket) {

        this.publicBaseUrl =
                supabaseUrl + "/storage/v1/object/public/" + bucket;

        this.restClient = restClientBuilder
                .baseUrl(supabaseUrl + "/storage/v1/object/" + bucket)
                .defaultHeader(HEADER_API_KEY, serviceRoleKey)
                .defaultHeader(HEADER_AUTHORIZATION, BEARER_PREFIX + serviceRoleKey)
                .build();
    }

    public String upload(MultipartFile file) {
        validate(file);

        String objectPath =
                UUID.randomUUID() + extractExtension(file.getOriginalFilename());

        try {
            restClient.put()
                    .uri("/" + objectPath)
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .header("x-upsert", "false")
                    .body(file.getBytes())
                    .retrieve()
                    .toBodilessEntity();
        } catch (IOException | RestClientException exception) {
            log.error("Supabase upload failed for object '{}'", objectPath, exception);
            throw new ImageUploadException(
                    "Failed to upload image to storage.");
        }

        return publicBaseUrl + "/" + objectPath;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Image file is required.");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidFileException(
                    "Image must be PNG, JPEG, or WEBP.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("Image must not exceed 5MB.");
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }

        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}

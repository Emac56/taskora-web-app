package com.taskora.api.common.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
 *
 * <p><b>Upload vs. delete failure semantics differ on purpose.</b> A failed
 * upload throws, because the caller has nothing usable without it (no
 * imageUrl to save). A failed delete only logs, because by the time callers
 * invoke it the database change it's cleaning up after (a row deleted, an
 * imageUrl replaced) has already happened — refusing to let the caller's
 * request succeed just because Supabase hiccuped on cleanup would trade a
 * cosmetic storage leak for a confusing user-facing error. Worst case on
 * failure is one leftover object, logged for follow-up — the same class of
 * leak this method exists to close, just not silent about it.
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

    // Magic-byte signatures for the three formats above. The declared
    // multipart Content-Type header is client-supplied and trivially
    // spoofable (see validate()) — these are checked against the actual
    // file bytes, which the client does not control.
    private static final byte[] PNG_SIGNATURE =
            {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPEG_SIGNATURE =
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] RIFF_SIGNATURE = {'R', 'I', 'F', 'F'};
    private static final byte[] WEBP_SIGNATURE = {'W', 'E', 'B', 'P'};
    private static final int WEBP_SIGNATURE_OFFSET = 8;
    // Longest signature we need to inspect: RIFF(4) + size field(4) + WEBP(4).
    private static final int MAGIC_HEADER_LENGTH = 12;

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
        String verifiedContentType = validate(file);

        String objectPath =
                UUID.randomUUID() + extractExtension(file.getOriginalFilename());

        try {
            restClient.put()
                    .uri("/" + objectPath)
                    .contentType(MediaType.parseMediaType(verifiedContentType))
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

    /**
     * Deletes the object backing a previously-uploaded image, identified by
     * the public URL that {@link #upload} returned and callers persisted
     * (e.g. {@code TutorialStep.imageUrl}).
     *
     * <p>This is best-effort cleanup, not a transactional guarantee:
     * <ul>
     *   <li>{@code null}/blank input is treated as "nothing to clean up"
     *       and silently ignored — most steps have no image at all, so
     *       callers can pass {@code step.getImageUrl()} straight through
     *       without a null check of their own.</li>
     *   <li>A URL that doesn't belong to this bucket (wrong host, wrong
     *       bucket segment — e.g. stale data from a previous
     *       configuration) is logged and skipped rather than treated as
     *       an error, since there is no corresponding object here to
     *       delete.</li>
     *   <li>A failed HTTP call (network issue, Supabase-side error) is
     *       logged and swallowed rather than thrown. See the class-level
     *       note on why upload and delete deliberately fail differently.</li>
     * </ul>
     */
    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String objectPath = extractObjectPath(imageUrl);
        if (objectPath == null) {
            log.warn(
                    "Skipping storage cleanup: '{}' is not an object URL for this bucket.",
                    imageUrl);
            return;
        }

        try {
            restClient.delete()
                    .uri("/" + objectPath)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            log.error(
                    "Supabase delete failed for object '{}'; it may remain orphaned in storage.",
                    objectPath, exception);
        }
    }

    /**
     * Reverses the URL construction in {@link #upload}: turns a stored
     * public URL back into the bucket-relative object path Supabase's
     * object API expects. Returns {@code null} if the URL doesn't start
     * with this bucket's public prefix at all.
     */
    private String extractObjectPath(String imageUrl) {
        String prefix = publicBaseUrl + "/";
        if (!imageUrl.startsWith(prefix)) {
            return null;
        }
        return imageUrl.substring(prefix.length());
    }

    /**
     * Validates the file and returns its verified content type.
     *
     * <p>The declared {@code Content-Type} multipart header is client-supplied
     * and can be set to anything regardless of the actual file bytes. It is
     * still checked against the allow-list first (cheap, rejects obviously
     * wrong uploads early), but the value trusted for the actual upload —
     * and therefore for what gets served back later — is the type detected
     * from the file's magic bytes, not the client's claim.
     */
    private String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Image file is required.");
        }

        String declaredContentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(declaredContentType)) {
            throw new InvalidFileException(
                    "Image must be PNG, JPEG, or WEBP.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("Image must not exceed 5MB.");
        }

        String actualContentType = detectActualContentType(file);
        if (!declaredContentType.equals(actualContentType)) {
            throw new InvalidFileException(
                    "Image content does not match its declared type.");
        }

        return actualContentType;
    }

    private String detectActualContentType(MultipartFile file) {
        byte[] header = readHeaderBytes(file, MAGIC_HEADER_LENGTH);

        if (startsWith(header, PNG_SIGNATURE)) {
            return "image/png";
        }
        if (startsWith(header, JPEG_SIGNATURE)) {
            return "image/jpeg";
        }
        if (startsWith(header, RIFF_SIGNATURE)
                && matchesAt(header, WEBP_SIGNATURE_OFFSET, WEBP_SIGNATURE)) {
            return "image/webp";
        }
        return null;
    }

    private byte[] readHeaderBytes(MultipartFile file, int maxBytes) {
        try (InputStream in = file.getInputStream()) {
            byte[] buffer = new byte[maxBytes];
            int bytesRead = in.readNBytes(buffer, 0, maxBytes);
            return bytesRead == maxBytes ? buffer : Arrays.copyOf(buffer, bytesRead);
        } catch (IOException exception) {
            throw new InvalidFileException("Unable to read image file for validation.");
        }
    }

    private boolean startsWith(byte[] data, byte[] signature) {
        return matchesAt(data, 0, signature);
    }

    private boolean matchesAt(byte[] data, int offset, byte[] signature) {
        if (data.length < offset + signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (data[offset + i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }

        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}

-- V4__widen_description_instruction_imageurl_columns.sql
-- These columns defaulted to VARCHAR(255) (Hibernate's default when no
-- @Column(length=...) was set), but the request DTOs already validate
-- description/instruction up to 5000 chars and imageUrl up to 2048.
-- Any value between 256 chars and the DTO limit passed validation but
-- failed at insert with "value too long for type character varying(255)",
-- which GlobalExceptionHandler reports as the same generic
-- "This operation conflicts with existing data." message used for unique
-- constraint clashes — making it look like the step-reorder conflict bug
-- when it is actually unrelated. Widening these to match validation fixes
-- the root cause.

ALTER TABLE tutorials ALTER COLUMN description TYPE VARCHAR(5000);
ALTER TABLE tutorial_steps ALTER COLUMN instruction TYPE VARCHAR(5000);
ALTER TABLE tutorial_steps ALTER COLUMN image_url TYPE VARCHAR(2048);

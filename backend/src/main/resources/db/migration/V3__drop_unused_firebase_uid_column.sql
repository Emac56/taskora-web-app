-- V3__drop_unused_firebase_uid_column.sql
-- Removes firebase_uid, a leftover from an earlier Firebase-based auth
-- exploration. Never mapped to the User entity and unreferenced anywhere
-- in the codebase (confirmed via full-repo search). Safe to drop.

ALTER TABLE users
    DROP COLUMN firebase_uid;

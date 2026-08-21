-- V2__add_unique_step_number_per_tutorial.sql
-- Prevents two steps of the same tutorial from sharing a step_number.
-- Backstops the service-level check in TutorialStepServiceImpl so the
-- rule holds even for direct DB writes or future code paths that skip
-- the service layer.

ALTER TABLE tutorial_steps
    ADD CONSTRAINT uq_tutorial_steps_tutorial_id_step_number
    UNIQUE (tutorial_id, step_number);

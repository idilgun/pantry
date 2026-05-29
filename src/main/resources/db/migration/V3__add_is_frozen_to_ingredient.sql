-- V3__add_is_frozen_to_ingredient.sql
-- Adds is_frozen flag to ingredient.
-- Frozen items are treated as having indefinite shelf life.

ALTER TABLE ingredient ADD COLUMN is_frozen BOOLEAN NOT NULL DEFAULT FALSE;
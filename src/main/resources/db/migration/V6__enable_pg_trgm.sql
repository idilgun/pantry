CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_ingredient_name_trgm ON ingredient USING GIN (name gin_trgm_ops);

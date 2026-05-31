ALTER TABLE recipe
    ADD COLUMN household_id BIGINT NOT NULL;

ALTER TABLE recipe
    ADD CONSTRAINT fk_recipe_household FOREIGN KEY (household_id) REFERENCES household (id);

CREATE INDEX idx_recipe_household_id ON recipe (household_id);

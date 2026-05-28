-- V2__create_core_schema.sql
-- Creates the full Pantry domain schema.
-- Drops the smoke_test table introduced in V1.

DROP TABLE IF EXISTS smoke_test;

-- ------------------------------------------------------------
-- Independent tables (no foreign key dependencies)
-- ------------------------------------------------------------

CREATE TABLE household (
                           id          BIGSERIAL PRIMARY KEY,
                           name        VARCHAR(255) NOT NULL,
                           created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE category (
                          id                      BIGSERIAL PRIMARY KEY,
                          name                    VARCHAR(255) NOT NULL UNIQUE,
                          default_shelf_life_days INTEGER NOT NULL
);

CREATE TABLE recipe (
                        id               BIGSERIAL PRIMARY KEY,
                        name             VARCHAR(255) NOT NULL,
                        duration_minutes INTEGER NOT NULL,
                        serves           INTEGER NOT NULL,
                        thumbnail_ref    VARCHAR(500)
);

-- ------------------------------------------------------------
-- Tables with foreign key dependencies
-- ------------------------------------------------------------

CREATE TABLE "user" (
                        id           BIGSERIAL PRIMARY KEY,
                        household_id BIGINT NOT NULL,
                        name         VARCHAR(255) NOT NULL,
                        email        VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE ingredient (
                            id                      BIGSERIAL PRIMARY KEY,
                            household_id            BIGINT NOT NULL,
                            category_id             BIGINT NOT NULL,
                            name                    VARCHAR(255) NOT NULL,
                            tracking_type           VARCHAR(50) NOT NULL,
                            location                VARCHAR(50) NOT NULL,
                            quantity                NUMERIC(10,3),
                            canonical_unit          VARCHAR(50),
                            custom_shelf_life_days  INTEGER,
                            is_available            BOOLEAN NOT NULL DEFAULT TRUE,
                            added_at                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT chk_tracking_type CHECK (tracking_type IN ('stocked', 'measured', 'counted')),
                            CONSTRAINT chk_location CHECK (location IN ('refrigerator', 'pantry'))
);

CREATE TABLE ingredient_conversion (
                                       id                      BIGSERIAL PRIMARY KEY,
                                       ingredient_id           BIGINT NOT NULL,
                                       from_unit               VARCHAR(50) NOT NULL,
                                       value_in_canonical_unit NUMERIC(10,4) NOT NULL
);

CREATE TABLE recipe_ingredient (
                                   id                BIGSERIAL PRIMARY KEY,
                                   recipe_id         BIGINT NOT NULL,
                                   ingredient_id     BIGINT NOT NULL,
                                   suggested_quantity NUMERIC(10,3),
                                   suggested_unit    VARCHAR(50),
                                   is_flexible       BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE recipe_step (
                             id          BIGSERIAL PRIMARY KEY,
                             recipe_id   BIGINT NOT NULL,
                             step_number INTEGER NOT NULL,
                             instruction TEXT NOT NULL
);


CREATE TABLE shopping_list_item (
                                    id            BIGSERIAL PRIMARY KEY,
                                    household_id  BIGINT NOT NULL,
                                    ingredient_id BIGINT NOT NULL,
                                    added_by      BIGINT,
                                    reason        VARCHAR(500),
                                    status        VARCHAR(50) NOT NULL DEFAULT 'pending',
                                    added_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    bought_at     TIMESTAMP,
                                    CONSTRAINT chk_shopping_status CHECK (status IN ('pending', 'in_basket', 'bought', 'cancelled'))
);

CREATE TABLE cooking_log (
                             id           BIGSERIAL PRIMARY KEY,
                             household_id BIGINT NOT NULL,
                             recipe_id    BIGINT NOT NULL,
                             cooked_by    BIGINT NOT NULL,
                             cooked_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cooking_log_item (
                                  id                   BIGSERIAL PRIMARY KEY,
                                  cooking_log_id       BIGINT NOT NULL,
                                  ingredient_id        BIGINT NOT NULL,
                                  actual_quantity      NUMERIC(10,3),
                                  actual_unit          VARCHAR(50),
                                  tracking_type_at_time VARCHAR(50) NOT NULL,
                                  CONSTRAINT chk_tracking_type_at_time CHECK (tracking_type_at_time IN ('stocked', 'measured', 'counted'))
);

-- ------------------------------------------------------------
-- Foreign key constraints
-- ------------------------------------------------------------

ALTER TABLE "user"
    ADD CONSTRAINT fk_user_household FOREIGN KEY (household_id) REFERENCES household (id);

ALTER TABLE ingredient
    ADD CONSTRAINT fk_ingredient_household FOREIGN KEY (household_id) REFERENCES household (id),
    ADD CONSTRAINT fk_ingredient_category  FOREIGN KEY (category_id) REFERENCES category (id);

ALTER TABLE ingredient_conversion
    ADD CONSTRAINT fk_ingredient_conversion_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient (id);

ALTER TABLE recipe_ingredient
    ADD CONSTRAINT fk_recipe_ingredient_recipe     FOREIGN KEY (recipe_id)     REFERENCES recipe (id),
    ADD CONSTRAINT fk_recipe_ingredient_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient (id);

ALTER TABLE recipe_step
    ADD CONSTRAINT fk_recipe_step_recipe FOREIGN KEY (recipe_id) REFERENCES recipe (id);

ALTER TABLE shopping_list_item
    ADD CONSTRAINT fk_shopping_list_item_household  FOREIGN KEY (household_id)  REFERENCES household (id),
    ADD CONSTRAINT fk_shopping_list_item_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient (id),
    ADD CONSTRAINT fk_shopping_list_item_user       FOREIGN KEY (added_by)      REFERENCES "user" (id);

ALTER TABLE cooking_log
    ADD CONSTRAINT fk_cooking_log_household FOREIGN KEY (household_id) REFERENCES household (id),
    ADD CONSTRAINT fk_cooking_log_recipe    FOREIGN KEY (recipe_id)    REFERENCES recipe (id),
    ADD CONSTRAINT fk_cooking_log_user      FOREIGN KEY (cooked_by)    REFERENCES "user" (id);

ALTER TABLE cooking_log_item
    ADD CONSTRAINT fk_cooking_log_item_cooking_log FOREIGN KEY (cooking_log_id) REFERENCES cooking_log (id),
    ADD CONSTRAINT fk_cooking_log_item_ingredient  FOREIGN KEY (ingredient_id)  REFERENCES ingredient (id);

-- ------------------------------------------------------------
-- Indexes
-- ------------------------------------------------------------

-- Foreign key indexes (Postgres does not index these automatically)
CREATE INDEX idx_user_household_id             ON "user" (household_id);
CREATE INDEX idx_ingredient_household_id       ON ingredient (household_id);
CREATE INDEX idx_ingredient_category_id        ON ingredient (category_id);
CREATE INDEX idx_ingredient_conversion_ingredient_id ON ingredient_conversion (ingredient_id);
CREATE INDEX idx_recipe_ingredient_recipe_id   ON recipe_ingredient (recipe_id);
CREATE INDEX idx_recipe_ingredient_ingredient_id ON recipe_ingredient (ingredient_id);
CREATE INDEX idx_recipe_step_recipe_id         ON recipe_step (recipe_id);
CREATE INDEX idx_shopping_list_item_household_id  ON shopping_list_item (household_id);
CREATE INDEX idx_shopping_list_item_ingredient_id ON shopping_list_item (ingredient_id);
CREATE INDEX idx_shopping_list_item_added_by   ON shopping_list_item (added_by);
CREATE INDEX idx_cooking_log_household_id      ON cooking_log (household_id);
CREATE INDEX idx_cooking_log_recipe_id         ON cooking_log (recipe_id);
CREATE INDEX idx_cooking_log_cooked_by         ON cooking_log (cooked_by);
CREATE INDEX idx_cooking_log_item_cooking_log_id ON cooking_log_item (cooking_log_id);
CREATE INDEX idx_cooking_log_item_ingredient_id  ON cooking_log_item (ingredient_id);

-- Query pattern indexes
CREATE INDEX idx_ingredient_name               ON ingredient (household_id, name);
CREATE INDEX idx_shopping_list_item_status     ON shopping_list_item (household_id, status);
CREATE INDEX idx_cooking_log_cooked_at         ON cooking_log (cooked_at);
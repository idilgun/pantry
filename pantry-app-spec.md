# Pantry — Project Specification

A household inventory and recipe app for tracking what you have at home, suggesting recipes based on available ingredients, and managing the shopping list.

This document captures the product vision, screen flows, and data model. It is the reference point for development decisions.

---

## 1. Context and goals

### What the app does

- Tracks ingredients across the refrigerator and pantry.
- Suggests recipes based on what is currently available.
- Lets the user mark a recipe as cooked, deducting actual quantities used.
- Maintains a shopping list with both manual entries and system-suggested items.
- Tracks ingredient categories and shelf life to flag items that should be consumed soon.

### Target users

A two-person household. Designed around real cooking habits — Mediterranean and Turkish palate, ingredient-led meal planning.

### Portfolio goals

- Demonstrate hands-on experience with modern Spring Boot, PostgreSQL, Docker, and cloud deployment.
- Signal fluency with AI-native development via an MCP server layer alongside the REST API.
- Show judgment about where LLMs add real value (recipe suggestions, unit conversion) versus where deterministic code is better.

---

## 2. Core design decisions

### Tracking types — three modes per ingredient

Real households track different ingredients differently. The app supports three modes, chosen by the user when adding an ingredient:

- **Counted** — whole units. Eggs, lemons, garlic cloves. Stored as integers.
- **Measured** — quantity with unit. Olive oil, milk, flour, lentils. Stored as decimal in a canonical unit.
- **Stocked** — boolean presence. Cumin, salt, soy sauce, oregano. No quantity, just "I have this." Stays stocked until the user marks it as out.

### Inventory locations

Two physical locations: **Refrigerator** and **Pantry**. Items belong to one or the other.

### Categories and perishability

Each ingredient belongs to a category (e.g. fresh greens, meat, dairy, spices, grains, oils). Categories carry a default shelf life in days. The user can override this per ingredient.

Items approaching expiry get a "use soon" indicator. The recipe suggestion logic can boost recipes that use expiring ingredients.

Categories are seeded in V4 and are intentionally broad — new ones are added via migration as needed.

### Frozen ingredients

Ingredients have an `is_frozen` boolean flag (default false). Frozen items are treated as having indefinite shelf life, overriding any category or custom shelf life. The flag is updated separately if the user freezes something.

Tracking open vs. frozen state of the same item (e.g. 5 bottles of wine, one open) is left to the user — they can add it as a separate ingredient entry. Not a system-level concern.

### Recipe quantities are suggestions, not commands

Recipes store a `suggested_quantity` for each ingredient, but this is not what gets deducted when cooking. When the user marks a recipe as cooked, they see a confirmation screen with editable steppers. The actual deducted amount comes from the user's confirmation input.

Some ingredients are marked `is_flexible = true` — for these (like spinach), the default is to use whatever quantity is currently in the inventory, with a note like "you have 200g — use all?"

### Recipes are household-owned

Recipes belong to a household, not a global catalog. This fits the AI-backed generation model — recipes are tailored to what your household has — and allows per-household customization. Recipe matching queries filter by `household_id` on both recipe and ingredient sides.

### Unit conversion via AI

Users may want to log "a cup of lentils" rather than measuring grams. Cups-to-grams (and similar) ratios are ingredient-specific.

Approach: when a new ingredient is added, the backend asks the Claude API to estimate conversion ratios for common units (cup, tablespoon, teaspoon). The result is cached in the `IngredientConversion` table. All quantities are stored in the ingredient's canonical unit (grams for solids, ml for liquids, pieces for counted).

### Shopping list as a transaction

Checking an item off the shopping list is not a visual toggle — it is a "bought" transaction. The flow supports an in-store mode:

1. While shopping, items are ticked into `in_basket` status without hitting inventory yet.
2. When the shopping trip is complete, all `in_basket` items transition to `bought`, quantities are confirmed, and inventory is updated.

Shopping list statuses: `pending`, `in_basket`, `bought`, `cancelled`. Nothing is hard-deleted — full history is retained. The UI "delete" and "delete all" buttons map to setting status to `cancelled`.

Manual entries have a null `reason`. System-suggested entries (running low, ran out, needed for a recipe) have a populated `reason`.

### Cooking log

Every cooking event is recorded with what was actually used. The log is not surfaced in the UI at this stage but is kept for future features (cooking history, smarter suggestions, analytics).

### Multi-user model

A `Household` entity owns ingredients, the shopping list, and the cooking log. Users belong to a household. This keeps the data model clean and allows the app to scale to other households later without restructuring.

---

## 3. Screens (four tabs)

### Inventory

Top-level tab. Two sub-sections: **Refrigerator** and **Pantry**.

Each item shows: name, quantity (or "stocked" badge if stocked-type), and a "low" badge if running low. An "add item" button at the bottom of each sub-section opens the add-item flow with the tracking type picker.

### Recipes

Two-level browse:

- **Recipe list**: cards with thumbnail, name, match summary ("4 of 5 ingredients · 30 min"), and a progress bar indicating inventory coverage. Sorted by match percentage.
- **Recipe detail**: thumbnail header, ingredient list with status dots (green = have, blue = stocked, gray = missing), recipe steps, and a "mark as cooked" button.

For missing ingredients in recipe detail, the user can tap "add" and choose:

- **Add to shopping list** — system entry with reason populated.
- **Mark as available** — opens the tracking type picker and adds to inventory.

### Shop

Two sections: **To buy** (pending and in_basket items) and **Done** (bought items).

Ticking an item moves it to `in_basket`. Completing the trip confirms quantities and moves everything to inventory.

### Cook confirmation flow

When the user taps "mark as cooked":

1. **Adjust quantities screen**: editable steppers for each measured/counted ingredient. Stocked ingredients shown separately with an option to flag "used the last of this" (auto-adds to shopping list).
2. **Confirm**: updates inventory, writes `CookingLog` and `CookingLogItem` rows, shows a brief success screen.

---

## 4. Data model

### Entity reference

**HOUSEHOLD** — `id, name, created_at`.

**USER** — `id, household_id, name, email`.

**CATEGORY** — `id, name, default_shelf_life_days`.

**INGREDIENT** — `id, household_id, category_id, name, tracking_type, location, quantity, canonical_unit, custom_shelf_life_days, is_available, is_frozen, added_at`.

- `tracking_type`: VARCHAR + CHECK (`stocked`, `measured`, `counted`).
- `location`: VARCHAR + CHECK (`refrigerator`, `pantry`).
- `quantity`: nullable for stocked items.
- `canonical_unit`: grams, ml, or pieces.
- `custom_shelf_life_days`: nullable; overrides category default if set.
- `is_available`: boolean. For stocked items, becomes false when marked as out.
- `is_frozen`: boolean, default false. Overrides shelf life to indefinite when true.

**INGREDIENT_CONVERSION** — `id, ingredient_id, from_unit, value_in_canonical_unit`.

One row per non-canonical unit. For 1 cup of olive oil (canonical unit ml): `from_unit = "cup"`, `value_in_canonical_unit = 237`.

**RECIPE** — `id, household_id, name, duration_minutes, serves, thumbnail_ref`.

- `duration_minutes` and `serves` are NOT NULL — recipes are AI-generated and always include this information.
- `household_id` — recipes are household-owned, not global.

**RECIPE_INGREDIENT** — `id, recipe_id, ingredient_id, suggested_quantity, suggested_unit, is_flexible`.

**RECIPE_STEP** — `id, recipe_id, step_number, instruction`.

Ordered steps as separate rows. `instruction` is TEXT (no length limit).

**SHOPPING_LIST_ITEM** — `id, household_id, ingredient_id, added_by, reason, status, added_at, bought_at`.

- `reason`: nullable. Null = user-added; populated = system-suggested.
- `status`: VARCHAR + CHECK (`pending`, `in_basket`, `bought`, `cancelled`).
- `added_by`: nullable — null means system-generated.
- `bought_at`: nullable; set when status transitions to bought.

**COOKING_LOG** — `id, household_id, recipe_id, cooked_by, cooked_at`.

**COOKING_LOG_ITEM** — `id, cooking_log_id, ingredient_id, actual_quantity, actual_unit, tracking_type_at_time`.

### Key relationships

- A `Household` has many `Users`, `Ingredients`, `Recipes`, `ShoppingListItems`, and `CookingLogs`.
- A `Category` classifies many `Ingredients`.
- An `Ingredient` has many `IngredientConversions`, can appear in many `RecipeIngredients`, `ShoppingListItems`, and `CookingLogItems`.
- A `Recipe` has many `RecipeIngredients`, `RecipeSteps`, and `CookingLogs`.
- A `CookingLog` has many `CookingLogItems`.

### Implementation notes

- All primary keys are `BIGSERIAL`.
- Enums implemented as `VARCHAR` + `CHECK` constraints, not Postgres native enums — easier to evolve.
- All FK columns have explicit indexes.
- `user` is a reserved word in PostgreSQL — quoted as `"user"` in DDL and JPA mapping.

---

## 5. Architecture

### Stack

- **Backend**: Java 21 + Spring Boot 4.x. Kotlin adoption planned selectively later.
- **Database**: PostgreSQL 16
- **Containerization**: Docker / docker-compose
- **Deployment**: Railway or Render (free tier)
- **AI integration**: Claude API for recipe suggestions and unit conversion estimation

### Dual API surface

The backend exposes the same domain through two interfaces:

- **REST API** — conventional HTTP endpoints.
- **MCP server** — exposes the same operations as MCP tools so any MCP-compatible AI client can read and manipulate the household's inventory and shopping list through natural language.

The MCP layer is a thin wrapper over the same service layer used by the REST controllers.

---

## 6. Build order

1. Schema migrations — Flyway DDL for all tables.
2. JPA entities and repositories.
3. Service layer — inventory operations, recipe matching, cook events, shopping list transitions.
4. REST controllers — HTTP endpoints over the service layer.
5. Docker for the app + first deployment.
6. Recipe matching and AI features — Claude API backed suggestions and unit conversion.
7. MCP server layer — wrap the service layer as MCP tools.
8. README and portfolio documentation.

---

## 7. Open questions and future work

- **Recipe seeding**: recipes are AI-generated per household. Seeding an initial set of recipes for a new household is a possible onboarding feature.
- **Image handling**: `thumbnail_ref` is a string reference — file storage strategy (local, S3, etc.) to be decided.
- **Authentication**: simple shared-household login for the first version. OAuth or magic links as a later concern.
- **Frontend**: out of scope for the first version. The MCP server provides a conversational interface; a web UI is a possible future addition.
- **Recipe versioning**: if a recipe is edited, past cooking log entries reference the old version. Acceptable to leave the log as-is since it records actual usage, not the recipe definition.
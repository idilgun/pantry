# Pantry — CLAUDE.md

Household inventory and recipe app for a two-person household. Portfolio project. Pair with `pantry-app-spec.md` for full design detail and `PROGRESS.md` for product decisions.

---

## Stack

- Java 21 + Spring Boot 4.x
- PostgreSQL 16
- Docker / docker-compose (`docker-compose up -d` to start Postgres)
- Claude API (planned: unit conversion on ingredient add, recipe suggestions)
- Dual API surface planned: REST + MCP server layer

---

## What's built

### Schema (Flyway migrations)
- **V1** — smoke test table (dropped in V2)
- **V2** — full core schema: household, user, category, ingredient, ingredient_conversion, recipe, recipe_ingredient, recipe_step, shopping_list_item, cooking_log, cooking_log_item
- **V3** — `is_frozen` column on ingredient
- **V4** — category seed data
- **V5** — `household_id` added to recipe table; FK + index
- **V6** — `pg_trgm` extension + GIN trigram index on `ingredient.name`

### Domain layer
All JPA entities and Spring Data repositories for every table.
`RecipeRepository` has `findByHouseholdId(Long householdId)`.
`ShoppingListItemRepository` has `findByHouseholdIdAndStatus` and `findByHouseholdIdAndStatusIn`.

### Services + controllers

**CategoryService, CategoryController** — `GET /categories`, `GET /categories/{id}`

**HouseholdService, HouseholdController** — household CRUD

**IngredientService, IngredientController** — mounted at `/households/{householdId}/ingredients`
- Add, get by household, get by id, update quantity, mark as out
- Fuzzy ingredient search at `GET /households/{householdId}/ingredients/search?q=` — hybrid: trigram similarity (pg_trgm, threshold 0.3) OR ILIKE substring match; results ordered by similarity score

**ShoppingListService, ShoppingListItemDto, ShoppingListController** — mounted at `/households/{householdId}/shopping-list`
- Full status transitions: `pending → in_basket`, `in_basket → pending`, `pending/in_basket → cancelled`, `in_basket → bought`
- `addItem` accepts either an existing `ingredientId` or an inline `newIngredient` payload (name, categoryId, trackingType, location, canonicalUnit); exactly one required, validated at controller with 400; new ingredients created with `isAvailable = false` in the same transaction
- `completeShoppingTrip` validates all request item IDs are in_basket for the household before processing (rejects entire request on first failure); supports `confirmedUnit` with IngredientConversion lookup for measured ingredients — multiplies by `valueInCanonicalUnit`; rejects with 400 if no conversion exists
- `addedById` is nullable — null is valid until the user system is built; `reason` null = user-added, populated = system-suggested

---

## What's next

1. **RecipeService, CookingLogService, RecipeController** — recipe CRUD, recipe matching
   (filter by household_id, sort by ingredient coverage), cook confirmation flow
   (inventory deduction from confirmed quantities, CookingLog + CookingLogItem writes,
   stocked-ran-out auto-add to shopping list as system-generated entry with reason populated).
   Recipe entity and RecipeRepository already updated with household_id and findByHouseholdId.
2. **Docker packaging + deployment** — Railway or Render (free tier).
3. **Claude API integration** — unit conversion ratios on ingredient add (cache in ingredient_conversion), recipe suggestions.
4. **MCP server layer** — thin wrapper over the service layer as MCP tools.

---

## Backlog

### Onboarding / default ingredient list (not started)

New households start with an empty ingredient list, which makes the search
dropdown feel broken on day one. An onboarding flow would solve the cold start problem.

**Proposed flow:**
1. Household is created
2. User is shown a curated list of common staples grouped by category
   (e.g. Pantry: olive oil, salt, pepper, flour, sugar, rice, lentils, cumin, sumac,
   bulgur, tahini, pomegranate molasses / Fridge: eggs, milk, butter, garlic, onions, lemons)
3. For each item the user can:
   - Mark as stocked — already have it, sets isAvailable = true
   - Add to shopping list — don't have it, creates a pending shopping list item
   - Skip — ingredient is seeded as isAvailable = false, appears in search but not in inventory view
4. Unreviewed items default to isAvailable = false

**Goals:**
- Eliminate the cold start problem (empty search on day one)
- The staples screen (salt, pepper, oil etc) gives the user a sense of being set up —
  the app already knows what a real kitchen looks like
- Mediterranean/Turkish household skew for the default list alongside universal staples

**Implementation notes:**
- Seed list lives in a migration as a reference table, not hardcoded in Java
- HouseholdService copies from the reference table on household creation,
  or a dedicated onboarding endpoint handles it explicitly
- Ties into recipe seeding (also in backlog) — both are onboarding content problems
- **User system** — simple shared-household login. `addedById` in shopping list and `cookedBy` in cooking log are nullable until this is built.
- **Authentication** — OAuth or magic links as a later concern.
- **Frontend** — out of scope for first version; MCP server provides the conversational interface.
- **Recipe seeding** — AI-generated recipes for a new household as an onboarding feature.
- **Image handling** — `thumbnailRef` is a string reference; file storage strategy (local, S3) TBD.

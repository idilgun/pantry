# Pantry — Progress & Development Notes

Covers what's been decided, what's been built, and what comes next. Pair with `pantry-app-spec.md` for full design detail.

---

## Key product decisions

**Three tracking types per ingredient** — stocked (boolean presence), measured (quantity + unit), counted (whole units). Chosen per ingredient by the user.

**Two inventory locations** — Refrigerator and Pantry.

**Categories with shelf life** — each ingredient belongs to a category with a default shelf life in days, overridable per ingredient. Seeded in V4, intentionally broad — add new ones via migration as needed.

**is_frozen flag on ingredient** — overrides shelf life to indefinite. Defaults to false. Tracking open vs. frozen state of the same item is left to the user as a separate ingredient entry.

**Recipe quantities are suggestions** — when marking a recipe cooked, the user confirms actual amounts used via editable steppers. `is_flexible` ingredients default to "use what you have."

**Recipes are household-owned** — not a global catalog. Fits the AI-backed generation model; allows per-household customization.

**Unit conversion via AI** — Claude API estimates conversion ratios (cup → grams etc.) when an ingredient is added. Cached in `ingredient_conversion`. All quantities stored in canonical units.

**Shopping list: four statuses** — `pending`, `in_basket`, `bought`, `cancelled`. Supports an in-store mode (tick into basket, complete trip to move to inventory). Nothing hard-deleted — full history retained. UI del
# Pantry

A household inventory and recipe app. Track what you have at home, get recipe suggestions based on available ingredients, and manage a shared shopping list.

Built as a portfolio project to support a return to backend development, and also a real tool — it replaces a shared Apple note in my household.

## What it does

- Tracks ingredients across refrigerator and pantry, with three tracking modes: counted (eggs, lemons), measured (olive oil, flour), and stocked (salt, cumin — just presence, no quantity)
- Suggests recipes based on what's currently available, favouring ingredients close to expiry
- Marks recipes as cooked, deducting actual quantities used (not the recipe's suggested amounts — the user confirms what they actually used)
- Manages a shopping list with an in-store mode: tick items into basket as you shop, confirm quantities at the end, inventory updates automatically
- System-generated shopping list entries when ingredients run out or are needed for a recipe

## Stack

- Java 21 + Spring Boot 4.x
- PostgreSQL 16
- Docker / docker-compose
- Flyway for schema migrations
- Claude API (planned: recipe suggestions, unit conversion)
- MCP server layer (planned: conversational access to household data)

## Architecture decisions worth noting

**Three tracking types** — different ingredients genuinely need different tracking. Counting olive oil in units or tracking salt as a decimal quantity both feel wrong in practice. The tracking type is chosen per ingredient and shapes how quantities are stored and deducted.

**Shopping list as a transaction** — checking an item off doesn't immediately update inventory. Items move to `in_basket` while shopping, then a single "complete trip" call confirms quantities and updates stock. Supports the reality of how shopping actually works.

**Recipes are household-owned** — recipes belong to a household, not a global catalog. This fits the AI generation model (recipes tailored to what you have) and keeps matching queries clean.

**Dual API surface** — the same service layer is exposed as both a REST API and an MCP server, so the app can be used conventionally or through any MCP-compatible AI client.

**Enums as VARCHAR + CHECK constraints** — easier to evolve than Postgres native enums without migration complexity.

## Running locally

```bash
docker-compose up -d
./mvnw spring-boot:run
```

Flyway applies all migrations automatically on startup.

## Status

Active development. Data layer, shopping list, and ingredient search complete. Recipe service and AI integration in progress.
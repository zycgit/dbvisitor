---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: H2
description: H2 dialect capabilities, primary key backfill, IDENTITY, sequences, and testing scenario differences in dbVisitor.
---

# H2

H2 is commonly used for unit testing, integration testing, and lightweight embedded scenarios. dbVisitor provides H2 with pagination, sequences, regular INSERT, MERGE conflict handling, and JDBC generated keys backfill capabilities.

## Quick Overview of Differences

| Concern | H2 Behavior |
|--------|--------|
| Primary Key Generation | `IDENTITY` column + JDBC generated keys |
| Pagination | `LIMIT ? OFFSET ?` |
| Write Conflicts | `MERGE INTO ...` |
| Batch Writes | JDBC batch supported (when no backfill) |
| Stored Procedures | Supported (Java stored procedures) |
| Sequences | Supports `values next value for seq` |

## Primary Key Backfill

`IDENTITY` columns are backfilled via JDBC generated keys. When no backfill is required, JDBC batch is used; when backfill is needed, row-by-row execution ensures each record is backfilled correctly.

## Write Conflict Strategies

| Strategy | H2 Implementation |
|------|--------|
| Ignore | `MERGE INTO ...` (requires PK) |
| Update | `MERGE INTO ...` (requires PK) |

## Testing Scenario Recommendations

- For local development/testing, recommend `-Dnxn.env=h2` — no Docker needed
- H2 compatibility mode can simulate MySQL/PostgreSQL behavior; dialect auto-detection
- Note subtle differences between H2 and actual databases in pagination and sequence syntax

## Special Topics

- [Auto-generated Key Backfill](./generated-keys): `IDENTITY`, sequence, `KeyType.Auto`, `KeyType.Sequence`, and Mapper configuration.
- [Dialect Details](./dialect-details): Low-level implementation of pagination SQL, MERGE syntax, and sequences.

## Relationship to General Documentation

For general API usage, see [Core API](../../guides/overview). The following content supplements specific differences and recommended practices for H2.

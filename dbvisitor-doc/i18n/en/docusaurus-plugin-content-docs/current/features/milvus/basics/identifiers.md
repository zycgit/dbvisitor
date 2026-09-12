---
id: identifiers
sidebar_position: 2
title: Identifiers
---

## Identifiers and Scope

SQL keywords are case-insensitive; Milvus determines name case sensitivity. Databases, collections, fields and indexes use ordinary identifiers. Prefer names starting with a letter or underscore, followed by letters, digits or underscores, and avoid keywords. The server validates the final name.

Double-quoted and backtick-quoted identifiers are unsupported. The lexer accepts dots and hyphens within names but does not interpret `database.collection` as a cross-database reference. The default scope is the connection database; only commands explicitly offering `IN DATABASE` or `ON DATABASE` accept a database scope.

Names, types and constraint keywords cannot be bound using `?`. For dynamic collection or field selection, choose from a trusted application allowlist rather than concatenating user input.

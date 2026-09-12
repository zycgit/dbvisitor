---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: H2
description: H2 usage with dbVisitor.
---

# H2

Use JdbcTemplate, method annotations, Mapper files, or the Fluent API with H2. The following pages cover database-specific configuration and usage.

| Scenario | Usage |
| --- | --- |
| Key generation | IDENTITY and sequences |
| Pagination | LIMIT / OFFSET |
| Insert conflicts | MERGE |

- [Type Support](./types.md): Choose Java property types.
- [Pagination](./pagination.mdx): Page queries and total counts.
- [Insert Conflicts](./conflict.mdx): Handle existing records during insertion.
- [Key Generation](./generated-keys.mdx): Configure and retrieve IDs.
- [Multiple-Write Consistency](./write.mdx): Roll back multiple writes on failure.

For an in-memory database, use `jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1`. This keeps its data until the JVM exits. See [Core API](../../guides/overview) for common operations.

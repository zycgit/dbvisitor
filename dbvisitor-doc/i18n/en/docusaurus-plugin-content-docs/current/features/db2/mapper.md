---
id: mapper
sidebar_position: 20
title: Mapper API
---

## Key Strategies {#keys}

Assigned keys, IDENTITY key retrieval, and composite keys are supported. Method annotations configure retrieval with `useGeneratedKeys` and `keyProperty`; BaseMapper uses the entity's key configuration.

Keys are read from JDBC generated keys by default. Use `generatedKeySource="resultSet"` only when the write command itself returns a result set containing the required columns. See [Key Generation](generated-keys.mdx) for examples.

## Pagination {#pagination}

Method annotations, BaseMapper, and file-mapper calls by statement ID support pagination. Page numbers start at 0; the total count and current page are queried separately. See [Pagination](pagination.mdx) for generated commands and usage notes.

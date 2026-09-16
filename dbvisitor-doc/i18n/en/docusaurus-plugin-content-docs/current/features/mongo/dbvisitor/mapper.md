---
id: mapper
slug: /features/mongo/mapper
sidebar_position: 20
title: Mapper API
---

## Method Annotations {#annotations}

Annotations contain MongoDB commands, not relational SQL. See [Query Operations](query.mdx#query-apis) and [Data Writes](write.mdx#write-apis) for examples.

Querying a missing collection returns an empty result, not a missing-table exception. To check collection existence, query collection information separately rather than catching that exception.

## Key Strategies {#keys}

Supply `_id` yourself or retrieve the generated `_id` after insertion. Keep the default generated-key source on annotation methods; do not set `generatedKeySource="resultSet"`, because inserts do not return a SQL RETURNING-style result set.

See [Key Generation](generated-keys.mdx#key-source) for mapping and retrieval examples.

## Pagination {#pagination}

Method annotations, BaseMapper, and file calls support pagination using MongoDB query and count commands. See [Pagination](pagination.mdx) for skip / limit behavior.

## Execution Options {#options}

Use the default or `FORWARD_ONLY` result-set type. The driver does not support `SCROLL_INSENSITIVE` or `SCROLL_SENSITIVE`; scrolling options cannot enable backward movement or arbitrary row positioning.

To read results again, requery or collect them into a List. See the core API's [Execution Options](../../../guides/core/mapper/annotation_query.mdx#options).

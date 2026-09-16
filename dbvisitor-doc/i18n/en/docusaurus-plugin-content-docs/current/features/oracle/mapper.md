---
id: mapper
sidebar_position: 20
title: Mapper API
---

## Key Strategies {#keys}

Assigned keys, IDENTITY, sequences, and composite keys are supported. To retrieve an IDENTITY value in a method annotation, set `useGeneratedKeys=true`, `keyProperty`, and `keyColumn`. For sequences, `selectKey` can obtain the value before insertion.

`RETURNING ... INTO` uses OUT parameters, not a current result set for key retrieval. See [Key Generation](generated-keys.mdx) for configuration and examples.

## Pagination {#pagination}

Method annotations, BaseMapper, and file-mapper calls by statement ID support pagination. Page numbers start at 0; the total count and current page are queried separately. See [Pagination](pagination.mdx) for generated commands and usage notes.

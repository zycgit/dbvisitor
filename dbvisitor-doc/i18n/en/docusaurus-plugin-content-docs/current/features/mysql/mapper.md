---
id: mapper
sidebar_position: 20
title: Mapper API
---

## Key Strategies {#keys}

Assigned keys, AUTO_INCREMENT key retrieval, and composite keys are supported. For method annotations, enable `useGeneratedKeys` and specify `keyProperty`; keep the default key source instead of `generatedKeySource="resultSet"`.

See [Key Generation](generated-keys.mdx#identity). BaseMapper uses the entity's key configuration; annotation methods use their own retrieval settings.

## Pagination {#pagination}

Method annotations, BaseMapper, and file-mapper calls by statement ID support pagination. Page numbers start at 0; the total count and current page are queried separately. See [Pagination](pagination.mdx) for generated commands and usage notes.

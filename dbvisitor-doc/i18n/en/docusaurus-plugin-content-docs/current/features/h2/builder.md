---
id: builder
sidebar_position: 30
title: Builder API
---

## Write Conflicts {#conflicts}

`Ignore` uses a `MERGE` with only an insert branch; `Update` uses `MERGE INTO ... KEY (...)`. Both match using the primary-key columns in the entity mapping.

Mark the entity's primary key. H2's MERGE count is not necessarily the number of newly inserted rows. See [Insert Conflicts](conflict.mdx).

## Pagination {#pagination}

`initPage(pageSize, pageNumber)` generates `LIMIT size OFFSET offset`, with page numbers starting at 0. The total is queried separately. Pagination iteration issues successive queries; specify an order for stable paging.

See [Pagination](pagination.mdx).

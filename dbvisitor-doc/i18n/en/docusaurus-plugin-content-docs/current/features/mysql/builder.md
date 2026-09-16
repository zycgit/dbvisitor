---
id: builder
sidebar_position: 30
title: Builder API
---

## Write Conflicts {#conflicts}

`Ignore` generates `INSERT IGNORE`; `Update` generates `INSERT ... ON DUPLICATE KEY UPDATE`. The table's primary and unique constraints determine conflicts, not just the primary key marked in the entity.

`IGNORE` can also turn data errors into warnings, so do not use it to validate input. See [Insert Conflicts](conflict.mdx) for configuration and examples.

## Pagination {#pagination}

`initPage(pageSize, pageNumber)` generates `LIMIT offset, size`. Page numbers start at 0. Pagination iteration fetches successive pages; order by a unique field for stable paging.

A separate count query obtains the total. See [Pagination](pagination.mdx).

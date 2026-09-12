---
id: hints
sidebar_position: 5
title: Hint Support
---

## Query Hints {#hint}

Place a Hint (`/*+ ... */`) before the statement it applies to.

Supported Hints:

- `overwrite_find_limit`: Force overwrite query LIMIT (TopK).
- `overwrite_find_skip`: Force overwrite query OFFSET.
- `overwrite_find_as_count`: Presence switches to COUNT, ignoring projection/sorting rather than counting a vector selection. Vector ranges are rejected. Even false triggers the hint; remove it to disable. WHERE/vector/pagination/WITH placeholders must still be bound and validated in their original SQL positions.

Ordinary Query/Search requests use connection property `consistencyLevel`; `consistency_level` is not an implemented query hint. QueryIterator in Java SDK 2.6.22 uses the collection's default consistency. For read-after-write guarantees with paginated queries, explicitly create the collection with `WITH (consistency_level='Strong')` rather than relying only on the connection property. These three overwrite hints apply only to SELECT, not UPDATE/DELETE. For sync/timeout, see [IMPORT](../write/import.md) and [Loading and Maintenance](../admin/load.md).

Example:

```sql
-- Force limit return to 5 records, skip first 10
/*+ overwrite_find_limit=5, overwrite_find_skip=10 */
SELECT * FROM table_name WHERE status = 1;

-- Use Hint to get total number of records matching conditions (Equivalent to count from ... where ...)
/*+ overwrite_find_as_count=true */
SELECT * FROM table_name WHERE age > 20;
```

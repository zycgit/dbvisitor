---
id: results
sidebar_position: 2
title: Command Results
---

On command pages, “update count” and “result set” describe the JDBC return format, not whether a command reads or writes data.

| Return format | dbVisitor usage | Examples |
| --- | --- | --- |
| Update count | `executeUpdate`, or Mapper write annotations/elements. | `SET`, `DEL`, `HSET` |
| Result set | Query methods, or Mapper `@Query` / `<select>`. | `GET`, `INCR`, `LPOP` |
| Depends on options | Choose according to the options used. | `SET ... GET` returns the old value as a result set. |

INCR changes data but returns its new value in a result set. Update counts do not always mean affected rows: LPUSH returns the resulting list length.

See [Reading Results](../dbvisitor/results.mdx) for multi-column and multi-row results.

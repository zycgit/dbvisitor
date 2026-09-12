---
id: limits
sidebar_position: 7
title: Limitations
---

- `db.createCollection(...)` does not support the `viewOn` option.
- `createIndex(...)` requires the `name` option; missing it causes an error.
- `runCommand(...)` requires the first argument to be a `Document` object.
- `find` method chaining only supports `limit`, `skip`, `sort`, `hint`. Other method calls (for example `explain`) are rejected.
- Using `db` without selecting a database (URL path or `use <db>`) causes “No database selected”.
- `connectTimeout` is declared but not applied in MongoClient settings.

`runCommand({...})` exposes native commands, not a complete mongosh JavaScript runtime.

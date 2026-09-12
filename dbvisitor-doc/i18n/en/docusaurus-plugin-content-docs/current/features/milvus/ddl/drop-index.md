---
id: drop-index
sidebar_position: 10
title: DROP INDEX
---

:::info[Note]
SDK methods: `dropIndex`.
:::

## Drop Index

```text
DROP INDEX index_name ON [TABLE] table_name;
```

Drops the specified index without deleting entities from the collection. `IF EXISTS` is not supported. Request failures caused by a missing index, insufficient permissions or an incompatible server state are reported to the JDBC caller. Success returns update count `0`.

Object names are SQL identifiers, not value parameters. A successful operation returns update count 0.

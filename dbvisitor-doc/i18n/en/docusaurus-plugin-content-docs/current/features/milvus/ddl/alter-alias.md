---
id: alter-alias
sidebar_position: 14
title: ALTER ALIAS
---

:::info[Note]
SDK methods: `alterAlias`.
:::

```text
ALTER ALIAS alias_name FOR TABLE table_name;
```

Milvus changes alias targets without copying entities. Queries newly executed after the change address the new collection; already-open ResultSets are not reexecuted, and there is no cross-query transaction guarantee.

Object names are SQL identifiers, not value parameters. A successful operation returns update count 0.

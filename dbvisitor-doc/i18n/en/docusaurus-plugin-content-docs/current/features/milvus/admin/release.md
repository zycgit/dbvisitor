---
id: release
sidebar_position: 3
title: RELEASE
---

:::info[Note]
SDK methods: `releaseCollection`, `releasePartitions`.
:::

```text
RELEASE TABLE table_name [PARTITION partition_name];
/*+ sync=false */ RELEASE TABLE table_name;
```

Releases the collection or partition from memory without deleting its entities. By default, waits for NotLoad; sync=false submits the request without waiting. Returns update count 0.

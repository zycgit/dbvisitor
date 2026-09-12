---
id: resource-groups
sidebar_position: 9
title: SHOW RESOURCE GROUPS / GROUP
---

:::info[Note]
SDK methods: `listResourceGroups`, `describeResourceGroup`.
:::

## Resource-group queries {#resource-groups}

```sql
SHOW RESOURCE GROUPS;
SHOW RESOURCE GROUP __default_resource_group;
```

These commands read native cluster-level resource-group information. They do not filter by the current JDBC database, change resource configuration, or move nodes. The list returns one VARCHAR `RESOURCE_GROUP` per row with no ordering guarantee. JDBC maxRows can limit returned rows.

A description returns one row: `RESOURCE_GROUP` is the name; `CAPACITY` and `AVAILABLE_NODES` are INTEGER; `LOADED_REPLICAS`, `OUTGOING_NODES`, and `INCOMING_NODES` are VARCHAR JSON objects preserving the SDK's collection replica-count, outgoing-node-count, and incoming-node-count maps. `CONFIG` is a VARCHAR JSON configuration object, or SQL NULL when absent from the SDK response. `NODES` is a VARCHAR JSON node array. Field meanings and state come from the server; queries neither wait for transfers nor infer additional scheduling state from counts. List and description calls are independent snapshots.

---
id: transfer
sidebar_position: 6
title: TRANSFER NODES / REPLICAS
---

:::info[Note]
SDK methods: `transferNode`, `transferReplica`.
:::

## Node and replica transfers {#resource-group-transfers}

```sql
TRANSFER NODES 1 FROM RESOURCE GROUP source_group TO RESOURCE GROUP target_group;
TRANSFER REPLICAS 1 OF TABLE table_name FROM RESOURCE GROUP source_group TO RESOURCE GROUP target_group;
```

These commands call SDK `transferNode` and `transferReplica` respectively. Counts accept `?` binding and must be positive integers. Node counts must fit Integer.MAX_VALUE; replica counts retain the Long range. Resource-group and collection names are SQL identifiers, not value parameters. Node transfer is cluster-level; replica transfer targets the specified collection in the current JDBC database.

Success returns update count 0. The SDK does not return a transfer task ID or migrated count, and the driver does not invent them. SQL does not wait for scheduling or implicitly create groups, change configurations, or load collections. The server determines resource availability, group existence, and allowed transfers. SHOW RESOURCE GROUP exposes incoming, outgoing, and replica information, but these are group snapshots rather than per-call task status.

Transfers are administrative writes that can affect query capacity and collection availability. Errors or disconnected clients do not guarantee that an operation was not applied; inspect server state before resubmitting.

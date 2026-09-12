---
id: load
slug: /features/milvus/sql/maintenance
sidebar_position: 2
title: LOAD
---

:::info[Note]
SDK methods: `loadCollection`, `loadPartitions`.
:::

Milvus requires collections to be loaded into memory before searching.
```text
LOAD TABLE table_name [PARTITION partition_name] [WITH (option=value, ...)];

-- Set the load wait timeout
/*+ timeout=60000 */ LOAD TABLE table_name;

-- Submit the release request without waiting
```

`LOAD TABLE` uses the SDK's synchronous wait by default. Refresh loads wait for SDK refresh progress, not merely an existing Loaded state. `RELEASE TABLE` waits for NotLoad by default; use `sync=false` to submit without waiting. The LOAD `timeout` hint is in milliseconds, defaults to 60000, must be positive, and is passed to the SDK load request and synchronous wait. Asynchronous submission does not certify background completion. The command returns update count 0, not a loaded-row count.

| LOAD WITH option | SDK request field | Type and default |
| --- | --- | --- |
| num_replicas | numReplicas | Positive INT32 integer, default 1; availability depends on cluster resources. |
| refresh | refresh | boolean, default false; refreshes a loaded collection or partition. |
| load_fields | loadFields | String list, empty by default, preserving the SDK's default field-loading scope. |
| skip_load_dynamic_field | skipLoadDynamicField | boolean, default false; true makes dynamic fields unavailable for filtering or output. |
| resource_groups | resourceGroups | String list, empty by default, leaving resource-group placement to the server. |

These options apply to both collections and partitions. Lists accept SQL list literals, JDBC `setObject` with `List<String>` / `String[]`, or JSON string arrays. Elements cannot be null or blank. Unknown or duplicate options and invalid types fail before the load API call. Keep `sync` / `timeout` in hints, not WITH.

```sql
LOAD TABLE books WITH (num_replicas=1, load_fields=['id','book_intro'], skip_load_dynamic_field=true);
/*+ timeout=30000 */ LOAD TABLE books WITH (refresh=true);
/*+ sync=false */ LOAD TABLE books PARTITION p WITH (resource_groups=['query_group']);
```

Official guidance requires the primary key and at least one vector field for partial loading, with filters and output restricted to loaded fields. Explicitly RELEASE and then LOAD to change the field set. This feature is documented as beta; check the target version's conditions before production use. See the [official loading guide](https://milvus.io/docs/load-and-release.md). The driver neither releases collections automatically nor turns the field list into access control. In the current 2.6.2 environment, both the native SDK and JDBC can still read an omitted scalar field. Rejection of omitted-field access is therefore not guaranteed for this version; memory use depends on actual loading behavior. `SHOW PROGRESS OF LOADING` does not expose separate refresh-task progress.

Multiple replicas and non-default resource groups require corresponding cluster resources. Request options do not create query nodes or additional replica capacity.

<span id="progress" />

<span id="load" />

<span id="flush" />

<span id="replicas" />

<span id="diagnostics" />

<span id="resource-groups" />

<span id="resource-group-config" />

<span id="resource-group-transfers" />

<span id="compaction" />

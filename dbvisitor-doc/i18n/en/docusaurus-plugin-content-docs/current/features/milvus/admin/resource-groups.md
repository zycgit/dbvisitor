---
id: resource-groups
sidebar_position: 5
title: Resource Group Statements
---

:::info[Note]
SDK methods: `createResourceGroup`, `updateResourceGroups`, `dropResourceGroup`.
:::

## Resource-group configuration {#resource-group-config}

```sql
CREATE RESOURCE GROUP rg_demo CONFIG '{"requests":{"nodeNum":0},"limits":{"nodeNum":0}}';
ALTER RESOURCE GROUP rg_demo CONFIG ?;
ALTER RESOURCE GROUPS CONFIG ?;
DROP RESOURCE GROUP rg_demo;
```

CONFIG is optional during creation, preserving SDK default creation behavior when omitted. CONFIG accepts a JSON object string (`setString`), Map, or JsonObject (`setObject`). A single-group CONFIG is a configuration object. Plural GROUPS expects a nonempty map from resource-group names to configuration objects. The driver validates all configurations before submitting one SDK `updateResourceGroups` call, rather than separate per-group requests.

Configurations use official ResourceGroupConfig Protobuf JSON: `requests.nodeNum` is the requested node count and `limits.nodeNum` is the node-count limit; `transferFrom` and `transferTo` are arrays of objects containing a `resourceGroup` name; `nodeFilter.nodeLabels` is an array of string key/value objects, such as `[ {"key":"zone","value":"east"} ]`. The official JSON parser rejects unknown fields, invalid types, and out-of-range integers. SHOW returns CONFIG in the same format, allowing it to be bound again. Omitted fields take Protobuf defaults rather than preserving existing values, so submit the complete desired configuration when updating.

Successful writes return update count 0, not a migrated-node count. Configuration changes can trigger server scheduling; SQL neither waits for scheduling nor promises multi-group transactions. The server determines whether deletion is allowed; the driver does not implicitly release collections or move nodes. The example uses zero-node configurations. Scheduling for nonzero node requests and label filters depends on actual cluster resources.

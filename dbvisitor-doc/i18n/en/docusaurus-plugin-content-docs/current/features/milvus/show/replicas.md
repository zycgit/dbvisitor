---
id: replicas
sidebar_position: 8
title: SHOW REPLICAS
---

:::info[Note]
SDK methods: `describeReplicas`.
:::

## Replica and shard status {#replicas}

```sql
SHOW REPLICAS FROM TABLE books;
SHOW REPLICAS FROM books;
```

Use `executeQuery()` to obtain a snapshot of the specified collection's replicas in the connection's current database, one row per replica. `TABLE` is optional. The collection name is a trusted SQL identifier, not a `?` value parameter. This calls SDK `describeReplicas` without implicitly loading, releasing, transferring replicas or scanning entities.

| Returned column (in order) | JDBC type | Meaning |
| --- | --- | --- |
| REPLICA_ID | BIGINT | Replica ID |
| COLLECTION_ID | BIGINT | Collection ID |
| PARTITION_IDS | VARCHAR | Native partition ID list as JSON array text |
| SHARD_REPLICAS | VARCHAR | Shard information as JSON array text; objects contain leaderID, leaderAddress, channelName and nodeIDs |
| NODE_IDS | VARCHAR | Replica query-node IDs as JSON array text, including leaders |
| RESOURCE_GROUP | VARCHAR | Resource group name |
| NUM_OUTBOUND_NODE | VARCHAR | Native numOutboundNode mapping as JSON object text, with resource-group names as keys and node counts as values |

Nested shards do not expand into extra rows or result sets. IDs retain Long/JSON integer precision; JSON consumers should avoid conversion to floating-point numbers that could lose precision. Empty SDK containers remain `[]` / `{}`, and NULL containers remain SQL NULL. An empty replica list yields an empty result set with column metadata. Missing or unloaded collections and permission restrictions retain the server's actual response; errors do not become empty results.

`setMaxRows()` caps JDBC replica rows; `fetchSize` does not turn this SDK snapshot into a paginated request. Node addresses are for observation only: the driver does not use them for multiple-address connections or read/write routing. A snapshot is not transactionally consistent across nodes and does not prove replica failover or scheduling performance.

In the current single-instance 2.6.2 environment, shard `nodeIDs` and replica `partitionIDs` can remain empty even after insertion and FLUSH; native SDK and JDBC results agree. The driver does not synthesize these fields from replica nodes or other queries. An empty shard node list does not mean the replica has no query nodes.

API reference: [describeReplicas](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/describeReplicas.md).

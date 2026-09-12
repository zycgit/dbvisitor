---
id: replicas
sidebar_position: 8
title: SHOW REPLICAS
---

:::info[说明]
对应 SDK 方法：`describeReplicas`。
:::

## 副本与分片状态 {#replicas}

```sql
SHOW REPLICAS FROM TABLE books;
SHOW REPLICAS FROM books;
```

通过 `executeQuery()` 返回当前连接 database 中指定集合的副本快照，每个副本一行；`TABLE` 可省略。集合名是可信 SQL 标识符，不能用 `?` 绑定。查询调用 SDK `describeReplicas`，不会隐式 LOAD、RELEASE、迁移副本或扫描实体。

| 返回列（按顺序） | JDBC 类型 | 含义 |
| --- | --- | --- |
| REPLICA_ID | BIGINT | 副本 ID |
| COLLECTION_ID | BIGINT | 集合 ID |
| PARTITION_IDS | VARCHAR | 原生分区 ID 列表的 JSON 数组文本 |
| SHARD_REPLICAS | VARCHAR | 分片信息的 JSON 数组文本；对象包含 leaderID、leaderAddress、channelName、nodeIDs |
| NODE_IDS | VARCHAR | 副本查询节点 ID 列表的 JSON 数组文本，包括负责人节点 |
| RESOURCE_GROUP | VARCHAR | 所属资源组名称 |
| NUM_OUTBOUND_NODE | VARCHAR | 原生 numOutboundNode 映射的 JSON 对象文本，键为资源组名、值为节点数 |

嵌套分片不会展开成额外行或结果集。整数 ID 以 Long/JSON 整数保留；解析 JSON 的应用应避免将其转为可能丢失精度的浮点数。SDK 返回的空容器保留为 `[]` / `{}`，NULL 容器保留为 SQL NULL。空副本列表返回带列元数据的空结果集；集合不存在、未加载或权限不足时，保留服务端实际响应，错误不转换为空结果。

`setMaxRows()` 限制 JDBC 返回的副本行数；`fetchSize` 不会将这个 SDK 快照请求变成分页查询。节点地址仅供观察，驱动不会据此改为多地址连接或读写路由。快照不保证跨节点事务一致性，不能用于证明副本容灾或调度性能。

在当前 2.6.2 单实例环境中，即使集合已写入并 FLUSH，分片的 `nodeIDs` 和副本的 `partitionIDs` 仍可能是空列表；原生 SDK 与 JDBC 返回一致。驱动不会用副本节点列表或其他查询结果补造这些字段，空分片节点列表也不等于副本没有查询节点。

API 对照：[describeReplicas](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/describeReplicas.md)。

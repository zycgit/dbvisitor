---
id: load
slug: /features/milvus/sql/maintenance
sidebar_position: 2
title: LOAD
---

:::info[说明]
对应 SDK 方法：`loadCollection`、`loadPartitions`。
:::

Milvus 要求在搜索前将 Collection 加载到内存。
```text
LOAD TABLE table_name [PARTITION partition_name] [WITH (option=value, ...)];

-- 设置加载等待超时
/*+ timeout=60000 */ LOAD TABLE table_name;

```

`LOAD TABLE` 默认通过 SDK 同步等待加载完成；刷新加载等待的是 SDK 的刷新进度，不能用既有 Loaded 状态代替。`RELEASE TABLE` 默认等待进入 NotLoad 状态；可使用 `sync=false` 只提交请求。LOAD 的 `timeout` 提示以毫秒计，默认 60000，必须大于 0，传入 SDK 的加载请求与同步等待；异步提交不表示后台任务已经成功。命令返回更新计数 0，不是加载行数。

| LOAD WITH 参数 | SDK 请求字段 | 类型与默认值 |
| --- | --- | --- |
| num_replicas | numReplicas | 正 INT32 整数，默认 1；可用数量取决于集群资源。 |
| refresh | refresh | boolean，默认 false；刷新已加载集合或分区。 |
| load_fields | loadFields | 字符串列表，默认空列表，沿用 SDK 默认加载字段范围。 |
| skip_load_dynamic_field | skipLoadDynamicField | boolean，默认 false；true 时动态字段不能用于过滤或输出。 |
| resource_groups | resourceGroups | 字符串列表，默认空列表，使用服务端默认资源组安排。 |

以上参数同时适用于集合和分区；列表可使用 SQL 列表、JDBC `setObject` 绑定 `List<String>` / `String[]`，或绑定 JSON 字符串数组。列表元素不能为空或空白；未知、重复参数和错误类型在调用加载 API 前报错。`sync` / `timeout` 仍使用 Hint，不放入 WITH。

```sql
LOAD TABLE books WITH (num_replicas=1, load_fields=['id','book_intro'], skip_load_dynamic_field=true);
/*+ timeout=30000 */ LOAD TABLE books WITH (refresh=true);
/*+ sync=false */ LOAD TABLE books PARTITION p WITH (resource_groups=['query_group']);
```

按官方说明，部分字段加载应包含主键和至少一个向量字段，查询过滤和输出应只引用加载字段；变更加载字段范围需显式 RELEASE 后重新 LOAD。官方将此功能列为 beta，生产采用前应确认对应版本条件，见[官方加载说明](https://milvus.io/docs/load-and-release.md)。驱动不会自动释放集合，也不把加载字段列表变成访问控制。在当前 2.6.2 环境中，原生 SDK 与 JDBC 都仍能读取列表外的标量字段，因此不保证此版本拒绝访问未指定字段；内存占用取决于实际加载行为。`SHOW PROGRESS OF LOADING` 不提供刷新任务的独立进度。

多副本及非默认资源组加载需要相应集群资源；请求参数本身不会创建查询节点或提供额外副本容量。

<span id="progress" />

<span id="load" />

<span id="flush" />

<span id="replicas" />

<span id="diagnostics" />

<span id="resource-groups" />

<span id="resource-group-config" />

<span id="resource-group-transfers" />

<span id="compaction" />

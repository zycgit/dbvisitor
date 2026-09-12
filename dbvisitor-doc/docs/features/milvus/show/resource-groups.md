---
id: resource-groups
sidebar_position: 9
title: SHOW RESOURCE GROUPS / GROUP
---

:::info[说明]
对应 SDK 方法：`listResourceGroups`、`describeResourceGroup`。
:::

## 资源组查询 {#resource-groups}

```sql
SHOW RESOURCE GROUPS;
SHOW RESOURCE GROUP __default_resource_group;
```

这两个命令读取原生集群级资源组信息，不按当前 JDBC database 过滤，也不修改资源配置或迁移节点。列表每行返回一个 VARCHAR `RESOURCE_GROUP`，不承诺排序，可用 JDBC maxRows 限制返回行数。

详情返回一行：`RESOURCE_GROUP` 为名称；`CAPACITY`、`AVAILABLE_NODES` 为 INTEGER；`LOADED_REPLICAS`、`OUTGOING_NODES`、`INCOMING_NODES` 为 VARCHAR JSON 对象，分别保留 SDK 返回的集合副本数量、迁出节点数量和迁入节点数量映射；`CONFIG` 为 VARCHAR JSON 配置对象，SDK 未提供时为 SQL NULL；`NODES` 为 VARCHAR JSON 节点数组。字段含义和状态由服务端决定，查询不等待迁移完成，也不从计数推算额外调度状态。列表与详情是独立快照。

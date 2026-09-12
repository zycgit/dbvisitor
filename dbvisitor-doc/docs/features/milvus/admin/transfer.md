---
id: transfer
sidebar_position: 6
title: TRANSFER NODES / REPLICAS
---

:::info[说明]
对应 SDK 方法：`transferNode`、`transferReplica`。
:::

## 节点与副本迁移 {#resource-group-transfers}

```sql
TRANSFER NODES 1 FROM RESOURCE GROUP source_group TO RESOURCE GROUP target_group;
TRANSFER REPLICAS 1 OF TABLE table_name FROM RESOURCE GROUP source_group TO RESOURCE GROUP target_group;
```

这两个命令分别调用 SDK `transferNode`、`transferReplica`。数量可用 `?` 绑定，必须为正整数；节点数量不超过 Integer.MAX_VALUE，副本数量保留 Long 范围。资源组名称和集合名称是 SQL 标识符，不是值参数。节点迁移是集群级操作，副本迁移使用当前 JDBC database 中的指定集合。

命令成功返回更新计数 0；SDK 不返回迁移任务 ID 或已迁移数量，驱动不会补造。SQL 不等待调度完成，不隐式创建资源组、修改配置或加载集合。资源不足、组不存在及不允许的迁移由服务端判断。可用 SHOW RESOURCE GROUP 观察迁入、迁出和副本信息，但这些是资源组快照，不是某次调用的独立任务状态。

迁移是运维写操作，可能影响查询容量和集合可用性。错误或连接中断不能保证操作没有生效，应先核查服务端状态再决定是否重提。

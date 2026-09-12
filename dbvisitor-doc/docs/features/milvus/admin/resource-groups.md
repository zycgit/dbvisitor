---
id: resource-groups
sidebar_position: 5
title: 资源组语句
---

:::info[说明]
对应 SDK 方法：`createResourceGroup`、`updateResourceGroups`、`dropResourceGroup`。
:::

## 资源组配置 {#resource-group-config}

```sql
CREATE RESOURCE GROUP rg_demo CONFIG '{"requests":{"nodeNum":0},"limits":{"nodeNum":0}}';
ALTER RESOURCE GROUP rg_demo CONFIG ?;
ALTER RESOURCE GROUPS CONFIG ?;
DROP RESOURCE GROUP rg_demo;
```

创建时 CONFIG 可省略，保留 SDK 的默认创建行为。CONFIG 支持 JSON 对象字符串（`setString`）、Map 或 JsonObject（`setObject`）。单组 CONFIG 是配置对象；复数 GROUPS 的 CONFIG 是“资源组名 → 配置对象”的非空 Map，驱动先验证全部配置，再通过一次 SDK `updateResourceGroups` 调用提交，不拆为逐组请求。

配置采用官方 ResourceGroupConfig 的 Protobuf JSON：`requests.nodeNum` 为期望节点数量、`limits.nodeNum` 为节点数量上限；`transferFrom` 和 `transferTo` 是含 `resourceGroup` 名称的对象数组；`nodeFilter.nodeLabels` 是字符串键值对象数组，例如 `[ {"key":"zone","value":"east"} ]`。未知字段、错误类型和超范围整数由官方 JSON 解析器拒绝。SHOW 返回的 CONFIG 使用同一格式，可再次绑定提交；省略的字段采用 Protobuf 默认值，不表示保留原配置，因此修改时应提交完整目标配置。

上述写命令成功返回更新计数 0，不返回节点迁移数量。修改配置可能触发服务端调度，SQL 不等待调度完成，也不承诺多组事务。删除是否允许由服务端检查，驱动不会隐式释放集合或移走节点。示例使用零节点配置；非零节点请求及标签过滤的调度效果取决于实际集群资源。

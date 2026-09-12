---
id: roles
sidebar_position: 9
title: 角色语句
---

:::info[说明]
对应 SDK 方法：`createRole`、`alterRole`、`dropRole`。
:::

## 角色管理

```text
CREATE ROLE [IF NOT EXISTS] role_name [WITH (description='...')];
DROP ROLE [IF EXISTS] role_name [WITH (force_drop=false)];
```

`force_drop` 是非 NULL 布尔值，可用 `setBoolean()` 绑定；省略时保持 SDK 的 false。`true` 直接设置 `DropRoleReq.forceDrop`，由服务端删除角色及其授权、成员关系。该操作可能撤销用户权限，请仅在确实需要强制删除时启用。在 Milvus 2.6.2 中，存在授权与成员时 false 失败；true 删除角色及其关联关系，但保留用户。驱动不逐条撤销权限兜底，不提供事务或失败重放。`IF EXISTS` 只处理角色不存在，不吞掉参数错误、权限错误或 SDK 失败；成功或不存在时返回更新计数 0。

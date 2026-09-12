---
id: privilege-groups
sidebar_position: 11
title: 权限组语句
---

:::info[说明]
对应 SDK 方法：`createPrivilegeGroup`、`dropPrivilegeGroup`、`addPrivilegesToGroup`、`removePrivilegesFromGroup`。
:::

## 权限组定义 {#privilege-groups}

```sql
CREATE PRIVILEGE GROUP app_query;
ALTER PRIVILEGE GROUP app_query ADD (Search, Query);
ALTER PRIVILEGE GROUP app_query DROP (Query);
DROP PRIVILEGE GROUP app_query;
```

分别调用 SDK 的 `createPrivilegeGroup`、`addPrivilegesToGroup`、`listPrivilegeGroups`、`removePrivilegesFromGroup`、`dropPrivilegeGroup`。`ALTER ... DROP (...)` 只移除列出的组内权限，`DROP PRIVILEGE GROUP` 才删除整个组。ADD/DROP 的列表至少包含一个 SQL 标识符；组名和权限名均不是 `?` 值参数。列表以一个 SDK 请求提交，驱动不拆成逐个权限的请求，也不据此承诺跨命令事务。

这些命令管理权限组定义，不创建角色、不自动授权，也不在删除组时自动撤销角色授权。要赋予角色某个权限组，请使用前面的 `GRANT PRIVILEGE ... ON DATABASE ... TABLE ...` 语法。已授权权限组的修改可能影响使用该组的角色；内置组保护、权限名称合法性及正在使用的组能否删除由服务端决定，失败作为 SQLException 返回。修改命令成功返回更新计数 0，表示 SDK 已确认，不是修改的权限数量。

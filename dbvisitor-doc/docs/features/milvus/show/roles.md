---
id: roles
sidebar_position: 21
title: SHOW ROLES / ROLE
---

:::info[说明]
对应 SDK 方法：`listRoles`、`describeRole`
:::

```text
SHOW ROLES;
SHOW ROLE role_name;
```

SHOW ROLE 调用 SDK `describeRole`，返回一行 ROLE、DESCRIPTION、GRANTS，均为 VARCHAR。GRANTS 为当前连接数据库范围内的直接授权 JSON 数组；没有授权时是 `[]`，仍保留角色详情行。数组保留 SDK 返回的 `objectType`、`objectName`、`privilege`、`grantor`、`dbName` 等字段，SDK 未返回的字段可能省略。说明信息按 SDK 返回值提供；空字符串或 NULL 不表示支持修改说明。该查询不展开角色成员或计算用户的全部有效权限。SDK 内部先读取授权再读取角色，两次读取不是事务快照。

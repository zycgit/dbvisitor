---
id: users
slug: /features/milvus/sql/security
sidebar_position: 20
title: SHOW USERS / USER
---

:::info[说明]
对应 SDK 方法：`listUsers`、`describeUser`
:::

```text
SHOW USERS;
SHOW USER username;
```

SHOW USER 返回一行 USER、ROLES、DESCRIPTION，均为 VARCHAR；ROLES 为 JSON 角色名称数组，无角色时是 `[]`。该命令读取 SDK 用户信息，不返回密码，也不将角色展开为有效权限。SDK/服务端未提供的说明信息不会由驱动补造。

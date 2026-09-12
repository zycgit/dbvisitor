---
id: privilege-groups
sidebar_position: 23
title: SHOW PRIVILEGE GROUPS
---

:::info[说明]
对应 SDK 方法：`listPrivilegeGroups`
:::

```text
SHOW PRIVILEGE GROUPS;
```

SHOW 返回 PRIVILEGE_GROUP、PRIVILEGES 两个 VARCHAR 列，每组一行；PRIVILEGES 是权限名称的 JSON 数组，空组为 `[]`。没有组时保留列元数据而不返回数据行。组和组内权限的顺序按 SDK 返回，不提供排序保证；JDBC `setMaxRows` 只限制返回的组行数，不截断组内权限数组。

---
id: privilege-groups
sidebar_position: 23
title: SHOW PRIVILEGE GROUPS
---

:::info[Note]
SDK methods: `listPrivilegeGroups`
:::

```text
SHOW PRIVILEGE GROUPS;
```

SHOW returns VARCHAR columns PRIVILEGE_GROUP and PRIVILEGES, with one row per group. PRIVILEGES is a JSON array of privilege names, or `[]` for an empty group. An empty group listing retains column metadata without returning data rows. Group and member ordering follows the SDK response without a sorting guarantee. JDBC `setMaxRows` limits group rows only, not the privileges within a group.

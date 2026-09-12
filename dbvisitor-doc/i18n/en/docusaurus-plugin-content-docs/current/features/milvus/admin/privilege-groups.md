---
id: privilege-groups
sidebar_position: 11
title: Privilege Group Statements
---

:::info[Note]
SDK methods: `createPrivilegeGroup`, `dropPrivilegeGroup`, `addPrivilegesToGroup`, `removePrivilegesFromGroup`.
:::

## Privilege Group Definitions {#privilege-groups}

```sql
CREATE PRIVILEGE GROUP app_query;
ALTER PRIVILEGE GROUP app_query ADD (Search, Query);
ALTER PRIVILEGE GROUP app_query DROP (Query);
DROP PRIVILEGE GROUP app_query;
```

These call SDK `createPrivilegeGroup`, `addPrivilegesToGroup`, `listPrivilegeGroups`, `removePrivilegesFromGroup`, and `dropPrivilegeGroup`, respectively. `ALTER ... DROP (...)` removes only the listed privileges; `DROP PRIVILEGE GROUP` deletes the group itself. ADD/DROP lists require at least one SQL identifier. Group and privilege names are not `?` value parameters. Each list is submitted in one SDK request, without splitting it into individual privilege requests or promising cross-command transactions.

These commands manage group definitions; they neither create roles nor grant privileges automatically, and dropping a group does not automatically revoke role grants. To assign a group to a role, use the preceding `GRANT PRIVILEGE ... ON DATABASE ... TABLE ...` syntax. Modifying an assigned group may affect roles that use it. The server determines built-in group protection, privilege-name validity, and whether an in-use group can be dropped; failures raise SQLException. Successful changes return update count 0 as SDK acknowledgment, not the number of privileges changed.

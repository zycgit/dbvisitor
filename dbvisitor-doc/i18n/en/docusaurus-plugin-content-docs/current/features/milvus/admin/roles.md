---
id: roles
sidebar_position: 9
title: Role Statements
---

:::info[Note]
SDK methods: `createRole`, `alterRole`, `dropRole`.
:::

## Role Management

```text
CREATE ROLE [IF NOT EXISTS] role_name [WITH (description='...')];
DROP ROLE [IF EXISTS] role_name [WITH (force_drop=false)];
```

`force_drop` is a non-NULL boolean, bindable with `setBoolean()`. Omitting it preserves the SDK default of false. Setting it to true passes `DropRoleReq.forceDrop` directly to the server to remove the role, its grants, and its memberships. This can revoke users' access, so enable it only when forced deletion is intended. On Milvus 2.6.2, false fails for a role with grants and members; true removes the role and its associations but keeps the user. The driver does not emulate this through individual revocations, provide a transaction, or replay failed operations. `IF EXISTS` handles only a missing role, not invalid parameters, permission errors, or SDK failures. Success or an absent role returns update count 0.

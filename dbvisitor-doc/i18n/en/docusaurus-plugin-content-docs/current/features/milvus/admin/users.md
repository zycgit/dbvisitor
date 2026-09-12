---
id: users
sidebar_position: 8
title: User Statements
---

:::info[Note]
SDK methods: `createUser`, `updatePassword`, `updateUser`, `dropUser`.
:::

## User Management

```text
CREATE USER [IF NOT EXISTS] username PASSWORD 'password' [WITH (description='...')];
ALTER USER username PASSWORD 'new-password' REPLACE 'old-password';
DROP USER [IF EXISTS] username;
```

The CREATE USER password and both ALTER USER passwords accept `?`, bound with `PreparedStatement.setString()`. Usernames remain SQL identifiers, not value parameters. ALTER USER binds the new password followed by the old password in SQL order, calls SDK `updatePassword` directly, and returns update count 0 on success. The server determines password constraints, authorization, and old-password validation. The driver does not implement local authentication or automatically replay a successful password change.

```java
try (PreparedStatement stmt = conn.prepareStatement(
        "ALTER USER app_user PASSWORD ? REPLACE ?")) {
    stmt.setString(1, newPassword);
    stmt.setString(2, oldPassword);
    stmt.executeUpdate();
}
```

ALTER USER optionally accepts `WITH (reset_connection=false, description='...')`, with parameter binding for both values. `reset_connection` is a boolean and preserves the SDK default of false. Setting it to true makes the SDK rebuild the current client connection as the modified user with the new password and clears the previous token, potentially switching the connection identity. It is not a side-effect-free refresh switch. `description` is a string passed directly to the SDK; support depends on the server version. Omitting it preserves the SDK default. Password modification and subsequent reconnection are not atomic: a reconnect failure does not mean the password remained unchanged. Other pooled connections are not updated automatically.


## User and Role Descriptions {#principal-descriptions}

Creation also accepts `WITH (description=?)`, passed to SDK `CreateUserReq.description` or `CreateRoleReq.description`. Descriptions must be non-NULL strings; empty strings are allowed. Omitting the option preserves the SDK empty-string default. User creation binds the password and then the description in SQL order. If `IF NOT EXISTS` finds an existing principal, it changes neither its password nor its description. Parameters are still consumed and validated, preserving the bindings of subsequent statements in the same execution.

```sql
CREATE USER app_user PASSWORD ? WITH (description=?);
CREATE ROLE reader WITH (description=?);
```

**Milvus 2.6.2 accepts these creation requests but returns empty descriptions through SHOW USER / SHOW ROLE**, as does the native SDK. Successful creation does not prove description persistence. The driver does not cache the input to populate DESCRIPTION. If stored descriptions are required, confirm native support in the deployed server version.

```sql
ALTER USER app_user WITH (description='Application query account');
ALTER ROLE reader WITH (description='Read-only access role');
ALTER USER app_user WITH (description=?);
```

These independent commands call SDK `updateUser` and `alterRole`, respectively, without calling password-change, role-recreation, or authorization APIs. WITH accepts only `description`, a non-NULL string; an empty string clears the description. Bind description values with `PreparedStatement.setString()`; names remain SQL identifiers. Successful SDK calls return update count 0, which does not mean that zero users or roles were changed.

**Milvus 2.6.2 does not support these two description updates**: its user update validates the request as a password change and returns a password-length error; its role update returns `UNIMPLEMENTED`. The driver propagates failures without resetting passwords or recreating roles to simulate support.

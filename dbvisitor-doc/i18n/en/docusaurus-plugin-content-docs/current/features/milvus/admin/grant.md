---
id: grant
sidebar_position: 10
title: GRANT / REVOKE
---

:::info[Note]
SDK methods: `grantRole`, `revokeRole`, `grantPrivilege`, `revokePrivilege`, `grantPrivilegeV2`, `revokePrivilegeV2`.
:::

## Grant & Revoke

```sql
GRANT ROLE role_name TO username;
REVOKE ROLE role_name FROM username;

GRANT Search ON Collection table_name TO ROLE role_name;
REVOKE Search ON Collection table_name FROM ROLE role_name;
GRANT Query ON Collection * TO ROLE role_name;
```

Use a connection identity authorized to perform grants. Even with authentication checks disabled, Milvus 2.6.2 needs the connection's authentication metadata to identify the grantor; anonymous connections may receive a missing-authorization error. The driver never supplies administrator credentials automatically. The ROLE column is taken from SDK role details when an individual grant does not contain the role name.

Privilege names and GRANT/REVOKE object types are forwarded unchanged to the SDK. The example object type is Collection, not TABLE; use server-recognized spelling and privileges. SHOW GRANTS instead uses this adapter's ON TABLE/USER/GLOBAL filter syntax.

Without ON, grants within the SDK query scope are returned, not an aggregate across every database. ON TABLE / USER filters by object type and exact name; ON GLOBAL returns only Global records. This lists grants, not effective permissions derived from global or wildcard grants.


## Privileges with an Explicit Database Scope {#scoped-privileges}

```sql
GRANT PRIVILEGE Search ON DATABASE app_db TABLE books TO ROLE reader;
REVOKE PRIVILEGE Search ON DATABASE app_db TABLE books FROM ROLE reader;
-- Explicitly grant access to all collections in app_db; the server interprets the privilege group
GRANT PRIVILEGE CollectionReadOnly ON DATABASE app_db TABLE * TO ROLE reader;
REVOKE PRIVILEGE CollectionReadOnly ON DATABASE app_db TABLE * FROM ROLE reader;
```

The `PRIVILEGE ... ON DATABASE ... TABLE ...` forms call SDK `grantPrivilegeV2` and `revokePrivilegeV2`, respectively, and are distinct from the legacy `GRANT Search ON Collection ...` commands above. Both database and collection scopes are required: the driver neither substitutes the current connection database nor inserts `*`. Either scope can explicitly be `*`; the server validates the effective scope and privilege-group names. Names and privileges are SQL identifiers, not values bindable with `?`.

Execution leaves the current connection database unchanged and returns update count 0 on success (SDK acknowledgment, not the number of grants). Use `SHOW ROLE reader ON DATABASE app_db` to inspect grants in a specific database, or explicitly use `ON DATABASE *` for the SDK wildcard database scope. Omitting ON DATABASE uses the connection database. Switching the current connection database with JDBC `setCatalog()` is neither needed nor supported. Grant records do not prove authorization enforcement. An unsupported V2 API or rejected operation raises SQLException without falling back to the legacy API that lacks an explicit database scope.

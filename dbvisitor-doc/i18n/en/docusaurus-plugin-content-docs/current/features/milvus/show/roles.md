---
id: roles
sidebar_position: 21
title: SHOW ROLES / ROLE
---

:::info[Note]
SDK methods: `listRoles`, `describeRole`
:::

```text
SHOW ROLES;
SHOW ROLE role_name;
```

SHOW ROLE calls SDK `describeRole` and returns one row with VARCHAR columns ROLE, DESCRIPTION, and GRANTS. GRANTS is a JSON array of direct grants scoped to the current connection database; an empty array (`[]`) still retains the role detail row. Entries preserve SDK fields such as `objectType`, `objectName`, `privilege`, `grantor`, and `dbName`; fields absent from the SDK response may be omitted. The description is returned as provided by the SDK; an empty string or NULL does not imply support for modifying descriptions. This query does not expand role membership or compute all effective privileges of a user. The SDK reads grants and then role information in separate requests, not a transactional snapshot.

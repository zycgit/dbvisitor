---
id: grants
sidebar_position: 22
title: SHOW GRANTS
---

:::info[Note]
SDK methods: `describeRole`
:::

```text
SHOW GRANTS FOR ROLE role_name;
SHOW GRANTS FOR ROLE role_name ON GLOBAL;
SHOW GRANTS FOR ROLE role_name ON TABLE table_name;
SHOW GRANTS FOR ROLE role_name ON USER username;
```

Returns the role's direct grants within the requested scope. It does not expand all effective permissions of a user.

Returned columns: DATABASE, ROLE, OBJECT, OBJECT_NAME, PRIVILEGE.

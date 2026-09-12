---
id: users
slug: /features/milvus/sql/security
sidebar_position: 20
title: SHOW USERS / USER
---

:::info[Note]
SDK methods: `listUsers`, `describeUser`
:::

```text
SHOW USERS;
SHOW USER username;
```

SHOW USER returns one row with VARCHAR columns USER, ROLES, and DESCRIPTION. ROLES is a JSON array of role names, or `[]` if there are none. This reads SDK user information; it does not return passwords or expand roles into effective privileges. The driver does not fabricate description information absent from the SDK/server.

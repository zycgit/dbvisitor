---
id: version
sidebar_position: 14
title: SHOW VERSION
---

:::info[Note]
SDK method: `getServerVersionV2`.
:::

```sql
SHOW VERSION;
```

| Statement | Returned columns |
| --- | --- |
| SHOW VERSION | VERSION, BUILD_TIME, GIT_COMMIT, GO_VERSION, DEPLOY_MODE: VARCHAR |

Use executeQuery() or execute() to read the ResultSet. This command reads server state without loading collections, flushing data or scanning entities.

Returns one row. Build details depend on the server version.

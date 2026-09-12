---
id: health
sidebar_position: 15
title: SHOW HEALTH
---

:::info[Note]
SDK method: `checkHealth`.
:::

```sql
SHOW HEALTH;
```

| Statement | Returned columns |
| --- | --- |
| SHOW HEALTH | IS_HEALTHY: BOOLEAN; REASONS, QUOTA_STATES: JSON array text / VARCHAR |

Use executeQuery() or execute() to read the ResultSet. This command reads server state without loading collections, flushing data or scanning entities.

IS_HEALTHY=false still returns reasons and quota states. Authentication or RPC failures raise SQLException.

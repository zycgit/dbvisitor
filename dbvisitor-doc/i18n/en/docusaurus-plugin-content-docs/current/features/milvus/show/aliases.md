---
id: aliases
sidebar_position: 6
title: SHOW ALIASES / ALIAS
---

:::info[Note]
SDK methods: `listAliases`, `describeAlias`.
:::

```text
SHOW ALIASES FROM [TABLE] table_name;
SHOW ALIAS alias_name;
SHOW ALIASES lists aliases for the specified collection, returning `ALIAS` and `TABLE` (VARCHAR) per row. An empty list retains column metadata. SHOW ALIAS describes one alias in a single row with `DATABASE`, `ALIAS` and `TABLE` (VARCHAR); missing aliases or permission errors produce SQLException. Names are not value parameters and cannot be replaced with `?`. The server determines list order; `Statement.setMaxRows()` can limit returned rows.
```

SHOW ALIASES lists aliases for the specified collection, returning `ALIAS` and `TABLE` (VARCHAR) per row. An empty list retains column metadata. SHOW ALIAS describes one alias in a single row with `DATABASE`, `ALIAS` and `TABLE` (VARCHAR); missing aliases or permission errors produce SQLException. Names are not value parameters and cannot be replaced with `?`. The server determines list order; `Statement.setMaxRows()` can limit returned rows.

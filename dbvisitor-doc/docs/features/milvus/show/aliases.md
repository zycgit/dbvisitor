---
id: aliases
sidebar_position: 6
title: SHOW ALIASES / ALIAS
---

:::info[说明]
对应 SDK 方法：`listAliases`、`describeAlias`。
:::

```text
SHOW ALIASES FROM [TABLE] table_name;
SHOW ALIAS alias_name;
SHOW ALIASES 列出指定集合的别名，每行返回 `ALIAS`、`TABLE`（VARCHAR）；无别名时返回保留列元数据的空结果集。SHOW ALIAS 描述一个别名，返回一行 `DATABASE`、`ALIAS`、`TABLE`（VARCHAR），不存在或无权限时返回 SQLException。名称不是值参数，不能用 `?` 替代。列表顺序由服务端决定，`Statement.setMaxRows()` 可限制返回行数。
```

SHOW ALIASES 列出指定集合的别名，每行返回 `ALIAS`、`TABLE`（VARCHAR）；无别名时返回保留列元数据的空结果集。SHOW ALIAS 描述一个别名，返回一行 `DATABASE`、`ALIAS`、`TABLE`（VARCHAR），不存在或无权限时返回 SQLException。名称不是值参数，不能用 `?` 替代。列表顺序由服务端决定，`Statement.setMaxRows()` 可限制返回行数。

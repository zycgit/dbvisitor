---
id: alter-database
sidebar_position: 2
title: ALTER DATABASE
---

:::info[说明]
对应 SDK 方法：`alterDatabaseProperties`、`dropDatabaseProperties`。
:::

## 修改数据库属性

```text
ALTER DATABASE db_name SET PROPERTIES ("key" = "value", ...);
ALTER DATABASE db_name DROP PROPERTIES ("key", ...);
```

SET 的值可使用 `?` 绑定，属性名不能绑定。DROP 只移除所列属性，不删除数据库。

---
id: databases
sidebar_position: 1
title: SHOW DATABASES / DATABASE
---

:::info[说明]
对应 SDK 方法：`listDatabases`、`describeDatabase`。
:::

## 查看数据库列表

```sql
SHOW DATABASES;
SHOW DATABASE db_name;
```

SHOW DATABASES 返回数据库名称列表；SHOW DATABASE 返回一行 `DATABASE`、`PROPERTIES`。PROPERTIES 是 JSON 对象文本，可用 `ResultSet.getString("PROPERTIES")` 读取后解析，包含服务端实际返回的属性；数据库不存在或无权限时返回 SQLException，不返回伪造的空属性。属性的名称、取值范围和动态生效条件由 Milvus 服务端决定。

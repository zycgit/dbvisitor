---
id: alter-table
sidebar_position: 5
title: ALTER TABLE
---

:::info[说明]
对应 SDK 方法：`renameCollection`、`addCollectionField`、`alterCollectionProperties`、`dropCollectionProperties`、`alterCollectionField`、`dropCollectionFieldProperties`。
:::

## 重命名表 {#rename}

```text
ALTER TABLE old_name RENAME TO new_name [IN DATABASE target_database];
```

未指定 `IN DATABASE` 时，在当前连接的数据库内重命名；指定时将 SDK `RenameCollectionReq.targetDbName` 设为目标数据库，由 Milvus 原生完成集合移动与重命名。源数据库始终取当前 JDBC 连接，不改变 `Connection.getCatalog()`，后续语句仍在原数据库执行。

```sql
ALTER TABLE books RENAME TO archived_books IN DATABASE archive;
```

目标数据库须已存在，并具有执行所需权限。这是同一 Milvus 实例/集群内的操作，不是跨集群复制；驱动不会自动建库、复制实体、重建索引、改写调用方 SQL 或调整授权。名称冲突、数据库不存在或服务端限制直接返回 SQLException，不进行替代操作。成功返回更新计数 0，而不是搬移的行数。

数据库和集合名使用 SQL 标识符，不支持 `?` 值参数。操作后请通过目标数据库的 JDBC URL 访问新名称；旧名称不自动成为别名。原生重命名接口见[官方 API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/renameCollection.md)。

权限继承、别名关联及加载状态要求由目标服务端决定；重命名前应确认这些对象的使用方式。


## 在线字段与属性变更

```text
ALTER TABLE table_name ADD [COLUMN] field_definition;
ALTER TABLE table_name [ALTER COLUMN field_name]
    SET PROPERTIES (property_name = property_value, ...);
ALTER TABLE table_name [ALTER COLUMN field_name]
    DROP PROPERTIES (property_name, ...);
```

省略 `ALTER COLUMN field_name` 时修改集合属性；包含该部分时修改指定字段属性。这里的 DROP PROPERTIES 不删除字段，不支持用此语法重命名字段或修改字段类型。

```sql
ALTER TABLE books ADD COLUMN priority INT64 NULL DEFAULT 7;
ALTER TABLE books ALTER COLUMN title SET PROPERTIES (max_length=1024);
ALTER TABLE books SET PROPERTIES ('collection.ttl.seconds'=3600);
ALTER TABLE books DROP PROPERTIES ('collection.ttl.seconds');
```

字段属性也可通过 `ALTER TABLE books ALTER COLUMN title DROP PROPERTIES ('key')` 移除。SET 接受标量参数值；NULL 不等同于删除属性，移除时使用 DROP PROPERTIES。

索引属性使用 [ALTER INDEX](create-index.md) 修改。

这些命令直接调用 SDK 的在线变更 API，不重建集合或复制数据。ADD COLUMN 复用建表字段定义，新增字段必须显式声明 NULL，不能新增主键或 AUTO_ID 字段。服务器拒绝字段类型、属性变更或加载状态时返回 SQLException，不提供模拟回退。DDL 成功返回更新计数 0，不表示受影响实体数。

---
id: alter-table
sidebar_position: 5
title: ALTER TABLE
---

:::info[Note]
SDK methods: `renameCollection`, `addCollectionField`, `alterCollectionProperties`, `dropCollectionProperties`, `alterCollectionField`, `dropCollectionFieldProperties`.
:::

## Rename Table {#rename}

```text
ALTER TABLE old_name RENAME TO new_name [IN DATABASE target_database];
```

Without `IN DATABASE`, the collection is renamed within the current connection's database. With it, the driver sets SDK `RenameCollectionReq.targetDbName`, and Milvus performs the native move and rename. The source database always comes from the JDBC connection. `Connection.getCatalog()` does not change, and subsequent statements still use the original database.

```sql
ALTER TABLE books RENAME TO archived_books IN DATABASE archive;
```

The destination database must exist, and the caller needs the required permissions. This is an operation within one Milvus instance/cluster, not cross-cluster copying. The driver does not create databases, copy entities, rebuild indexes, rewrite application SQL, or adjust grants. Name conflicts, missing databases, and server restrictions propagate as SQLException without fallback operations. Success returns update count 0, not the number of moved rows.

Database and collection names are SQL identifiers, not `?` value parameters. After the operation, use a JDBC URL targeting the destination database to access the new name. The old name does not automatically become an alias. See the [native rename API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/renameCollection.md).

Grant inheritance, alias associations and load-state requirements depend on the target server. Check how those objects are used before renaming.


## Online Field and Property Changes

```text
ALTER TABLE table_name ADD [COLUMN] field_definition;
ALTER TABLE table_name [ALTER COLUMN field_name]
    SET PROPERTIES (property_name = property_value, ...);
ALTER TABLE table_name [ALTER COLUMN field_name]
    DROP PROPERTIES (property_name, ...);
```

Omitting `ALTER COLUMN field_name` changes collection properties; including it changes properties of the specified field. DROP PROPERTIES does not delete the field, and this syntax does not rename fields or change their types.

```sql
ALTER TABLE books ADD COLUMN priority INT64 NULL DEFAULT 7;
ALTER TABLE books ALTER COLUMN title SET PROPERTIES (max_length=1024);
ALTER TABLE books SET PROPERTIES ('collection.ttl.seconds'=3600);
ALTER TABLE books DROP PROPERTIES ('collection.ttl.seconds');
```

Field properties can also be removed with `ALTER TABLE books ALTER COLUMN title DROP PROPERTIES ('key')`. SET accepts scalar parameter values; NULL does not remove a property. Use DROP PROPERTIES instead.

Use [ALTER INDEX](create-index.md) to change index properties.

These commands invoke native SDK online-change APIs without rebuilding collections or copying entities. ADD COLUMN reuses field definitions from CREATE TABLE; new fields must explicitly declare NULL and cannot be primary keys or AUTO_ID fields. Server rejection of a field type, property change, or load state becomes SQLException, with no emulated fallback. Successful DDL returns update count 0, not an affected-entity count.

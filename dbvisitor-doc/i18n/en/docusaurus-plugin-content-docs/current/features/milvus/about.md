---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Milvus
---

# Milvus

Use SQL-style commands and the Fluent API to operate Milvus collections and vectors. Connection setup is described in [JDBC Milvus](../../drivers/milvus/connection.mdx).


## dbVisitor Usage

- [Type Support](dbvisitor/types.md)：Choose Java types for stored values.
- [Pagination](dbvisitor/pagination.mdx)：Fetch a range and obtain totals.
- [Key Generation](dbvisitor/generated-keys.mdx)：Assign identifiers or read generated IDs.
- [Data Reads and Writes](dbvisitor/usage.mdx)：Bind commands and understand the database action.

## SQL Statements

### Language Basics

- [Notation and Comments](basics/notation.md)
- [Identifiers](basics/identifiers.md)
- [Literals and Parameters](basics/parameters.md)
- [Operators](basics/operators.md)
- [Hint Support](basics/hints.md)

### Data Types

- [Scalar Types](types/fields.md)
- [JSON](types/json.md)
- [ARRAY](types/array.md)
- [Length, Capacity and Dimensions](types/dimensions.md)
- [Field Constraints](types/defaults.md)
- [Vector Values](types/vectors.md)
- [JSON, ARRAY and Vector Binding](types/binding.md)

### Query Statements

- [SELECT](query/select.md)
- [COUNT / SELECT COUNT(*)](query/count.md)
- [HYBRID](query/hybrid.md)

### Write Statements

- [INSERT](write/insert.md)
- [UPSERT](write/upsert.md)
- [UPDATE](write/update.md)
- [DELETE](write/delete.md)
- [IMPORT](write/import.md)

### Definition Statements

- [CREATE DATABASE](ddl/create-database.md)
- [ALTER DATABASE](ddl/alter-database.md)
- [DROP DATABASE](ddl/drop-database.md)
- [CREATE TABLE](ddl/create-table.md)
- [ALTER TABLE](ddl/alter-table.md)
- [DROP TABLE](ddl/drop-table.md)
- [TRUNCATE TABLE](ddl/truncate-table.md)
- [CREATE INDEX](ddl/create-index.md)
- [ALTER INDEX](ddl/alter-index.md)
- [DROP INDEX](ddl/drop-index.md)
- [CREATE PARTITION](ddl/create-partition.md)
- [DROP PARTITION](ddl/drop-partition.md)
- [CREATE ALIAS](ddl/create-alias.md)
- [ALTER ALIAS](ddl/alter-alias.md)
- [DROP ALIAS](ddl/drop-alias.md)
- [FUNCTION Definitions](ddl/functions.md)
- [ALTER TABLE … FUNCTION](ddl/alter-function.md)

### SHOW Statements

- [SHOW DATABASES / DATABASE](show/databases.md)
- [SHOW TABLES / TABLE / CREATE TABLE](show/tables.md)
- [SHOW INDEXES / INDEX](show/indexes.md)
- [SHOW STATS](show/stats.md)
- [SHOW PARTITIONS / PARTITION](show/partitions.md)
- [SHOW ALIASES / ALIAS](show/aliases.md)
- [SHOW FLUSH ALL](show/flush.md)
- [SHOW REPLICAS](show/replicas.md)
- [SHOW RESOURCE GROUPS / GROUP](show/resource-groups.md)
- [SHOW COMPACTION](show/compaction.md)
- [SHOW PROGRESS](show/progress.md)
- [SHOW USERS / USER](show/users.md)
- [SHOW ROLES / ROLE](show/roles.md)
- [SHOW GRANTS](show/grants.md)
- [SHOW PRIVILEGE GROUPS](show/privilege-groups.md)
- [SHOW IMPORT / IMPORTS](show/import.md)
- [SHOW VERSION](show/version.md)
- [SHOW HEALTH](show/health.md)
- [SHOW PERSISTENT SEGMENTS](show/persistent-segments.md)
- [SHOW QUERY SEGMENTS](show/query-segments.md)

### Administration Statements

- [ANALYZE](admin/analyze.md)
- [LOAD](admin/load.md)
- [RELEASE](admin/release.md)
- [FLUSH](admin/flush.md)
- [Resource Group Statements](admin/resource-groups.md)
- [TRANSFER NODES / REPLICAS](admin/transfer.md)
- [COMPACT](admin/compact.md)
- [User Statements](admin/users.md)
- [Role Statements](admin/roles.md)
- [GRANT / REVOKE](admin/grant.md)
- [Privilege Group Statements](admin/privilege-groups.md)

<span id="database" />
<span id="table" />
<span id="index" />
<span id="user" />
<span id="dml" />
<span id="dql" />
<span id="extended" />
<span id="progress" />
<span id="jdbc-results" />
<span id="hint" />
<span id="collection-keys" />
<span id="truncate" />
<span id="rename" />
<span id="partition" />
<span id="alias" />
<span id="index-metadata" />
<span id="upsert" />
<span id="generated-keys" />
<span id="query-options" />
<span id="pagination" />
<span id="grouping" />
<span id="hybrid" />
<span id="functions" />
<span id="alter-functions" />
<span id="analyze" />
<span id="import" />
<span id="load" />
<span id="flush" />
<span id="replicas" />
<span id="diagnostics" />
<span id="resource-groups" />
<span id="resource-group-config" />
<span id="resource-group-transfers" />
<span id="compaction" />
<span id="principal-descriptions" />
<span id="scoped-privileges" />
<span id="privilege-groups" />

---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Milvus
---

# Milvus

通过 SQL 风格命令和构造器 API 操作 Milvus 集合与向量。 连接配置见 [JDBC Milvus](../../drivers/milvus/connection.mdx).


## dbVisitor 使用

- [类型支持](dbvisitor/types.md)：选择字段或值对应的 Java 类型。
- [分页查询](dbvisitor/pagination.mdx)：读取指定范围和查询总数。
- [主键生成](dbvisitor/generated-keys.mdx)：指定编号或读取生成的 ID。
- [数据读写](dbvisitor/usage.mdx)：绑定命令参数，了解 API 对应的数据库动作。

## SQL 语句

| 分类 | 语句与用法 |
| --- | --- |
| <span id="语法基础">语法基础</span> | [语法约定与注释](basics/notation.md) · [标识符](basics/identifiers.md) · [字面量与参数](basics/parameters.md) · [运算符](basics/operators.md) · [Hint 支持](basics/hints.md) |
| <span id="数据类型">数据类型</span> | [标量类型](types/fields.md) · [JSON](types/json.md) · [ARRAY](types/array.md) · [向量值](types/vectors.md)<br />[长度、容量与维度](types/dimensions.md) · [字段约束](types/defaults.md) · [JSON、ARRAY 与向量绑定](types/binding.md) |
| <span id="查询语句">查询语句</span> | [SELECT](query/select.md) · [COUNT / SELECT COUNT(*)](query/count.md) · [HYBRID](query/hybrid.md) |
| <span id="写入语句">写入语句</span> | [INSERT](write/insert.md) · [UPSERT](write/upsert.md) · [UPDATE](write/update.md) · [DELETE](write/delete.md) · [IMPORT](write/import.md) |
| <span id="定义语句">定义语句</span> | [CREATE DATABASE](ddl/create-database.md) · [ALTER DATABASE](ddl/alter-database.md) · [DROP DATABASE](ddl/drop-database.md)<br />[CREATE TABLE](ddl/create-table.md) · [ALTER TABLE](ddl/alter-table.md) · [DROP TABLE](ddl/drop-table.md) · [TRUNCATE TABLE](ddl/truncate-table.md)<br />[CREATE INDEX](ddl/create-index.md) · [ALTER INDEX](ddl/alter-index.md) · [DROP INDEX](ddl/drop-index.md)<br />[CREATE PARTITION](ddl/create-partition.md) · [DROP PARTITION](ddl/drop-partition.md)<br />[CREATE ALIAS](ddl/create-alias.md) · [ALTER ALIAS](ddl/alter-alias.md) · [DROP ALIAS](ddl/drop-alias.md)<br />[FUNCTION 定义](ddl/functions.md) · [ALTER TABLE … FUNCTION](ddl/alter-function.md) |
| <span id="show-语句">SHOW 语句</span> | [SHOW DATABASES / DATABASE](show/databases.md) · [SHOW TABLES / TABLE / CREATE TABLE](show/tables.md)<br />[SHOW INDEXES / INDEX](show/indexes.md) · [SHOW PARTITIONS / PARTITION](show/partitions.md) · [SHOW ALIASES / ALIAS](show/aliases.md)<br />[SHOW STATS](show/stats.md) · [SHOW PROGRESS](show/progress.md) · [SHOW IMPORT / IMPORTS](show/import.md) · [SHOW FLUSH ALL](show/flush.md)<br />[SHOW REPLICAS](show/replicas.md) · [SHOW RESOURCE GROUPS / GROUP](show/resource-groups.md)<br />[SHOW COMPACTION](show/compaction.md) · [SHOW PERSISTENT SEGMENTS](show/persistent-segments.md) · [SHOW QUERY SEGMENTS](show/query-segments.md)<br />[SHOW USERS / USER](show/users.md) · [SHOW ROLES / ROLE](show/roles.md) · [SHOW GRANTS](show/grants.md) · [SHOW PRIVILEGE GROUPS](show/privilege-groups.md)<br />[SHOW VERSION](show/version.md) · [SHOW HEALTH](show/health.md) |
| <span id="管理语句">管理语句</span> | [ANALYZE](admin/analyze.md) · [LOAD](admin/load.md) · [RELEASE](admin/release.md)<br />[FLUSH](admin/flush.md) · [COMPACT](admin/compact.md)<br />[资源组语句](admin/resource-groups.md) · [TRANSFER NODES / REPLICAS](admin/transfer.md)<br />[用户语句](admin/users.md) · [角色语句](admin/roles.md) · [GRANT / REVOKE](admin/grant.md) · [权限组语句](admin/privilege-groups.md) |

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

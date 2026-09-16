---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: MongoDB
---

使用 JdbcTemplate 或 Mapper 执行 MongoDB 命令。构造器 API 也可完成基础文档操作。连接配置见 [JDBC MongoDB](../../drivers/mongo/connection.mdx)。

## dbVisitor 使用

- [编程式 API](/docs/features/mongo/programmatic)：查询、写入、多结果及存储过程与函数差异。
- [Mapper API](dbvisitor/mapper.md)：方法注解、Mapper 读写及执行差异。
- [构造器 API](dbvisitor/builder.md)：可用操作、数据源差异与对应用法。
- [查询操作](dbvisitor/query.mdx)：执行查询、组合条件及映射结果。
- [数据写入](dbvisitor/write.mdx)：新增、修改、删除及写入返回值。
- [分页查询](dbvisitor/pagination.mdx)：读取指定范围和查询总数。
- [向量操作](dbvisitor/vectors.md)：向量字段映射、检索度量与支持范围。
- [主键生成](dbvisitor/generated-keys.mdx)：指定编号或读取生成的 ID。
- [参数与规则](dbvisitor/parameters.md)：绑定值及选择原生命令片段。
- [类型支持](dbvisitor/types.md)：选择字段或值对应的 Java 类型。
- [结果读取](dbvisitor/results.mdx)：读取结果列，映射返回的数据。
- [事务支持](dbvisitor/transactions.md)：事务 API 行为及隔离级别设置。

## 语法基础

[命令格式与参数](basics/commands.md) · [Hint 支持](basics/hints.md)

## 查询命令

[文档查询](query/find.md) · [计数与去重](query/count.md) · [聚合管道](query/aggregate.mdx)

## 写入命令

[插入文档](write/insert.md) · [更新与替换](write/update.md) · [删除文档](write/delete.md) · [批量命令](write/bulk.md)

## 管理命令

[集合与视图](admin/collections.md) · [数据库管理](admin/databases.md) · [索引管理](admin/indexes.md)<br />
[用户管理](admin/users.md) · [USE 与 SHOW](admin/other.md) · [数据库原生命令](admin/run-command.md)

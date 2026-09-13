---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: MongoDB
---

使用 JdbcTemplate 或 Mapper 执行 MongoDB 命令。构造器 API 也可完成基础文档操作。连接配置见 [JDBC MongoDB](../../drivers/mongo/connection.mdx)。

## dbVisitor 使用

- [类型支持](dbvisitor/types.md)
- [分页查询](dbvisitor/pagination.mdx)
- [主键生成](dbvisitor/generated-keys.mdx)
- [数据读写](dbvisitor/usage.mdx)
- [结果读取](dbvisitor/results.mdx)

## 语法基础

- [命令格式与参数](basics/commands.md)
- [Hint 支持](basics/hints.md)

## 查询命令

- [文档查询](query/find.md)
- [计数与去重](query/count.md)
- [聚合管道](query/aggregate.mdx)

## 写入命令

- [插入文档](write/insert.md)
- [更新与替换](write/update.md)
- [删除文档](write/delete.md)
- [批量命令](write/bulk.md)

## 管理命令

- [集合与视图](admin/collections.md)
- [数据库管理](admin/databases.md)
- [索引管理](admin/indexes.md)
- [用户管理](admin/users.md)
- [USE 与 SHOW](admin/other.md)
- [数据库原生命令](admin/run-command.md)

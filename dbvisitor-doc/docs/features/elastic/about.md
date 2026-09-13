---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Elasticsearch
---

使用 JdbcTemplate 或 Mapper 执行 REST 请求。构造器 API 也可完成基础文档操作。连接配置见 [JDBC Elasticsearch](../../drivers/elastic/connection.mdx)。

## dbVisitor 使用

- [类型支持](dbvisitor/types.md)
- [分页查询](dbvisitor/pagination.mdx)
- [主键生成](dbvisitor/generated-keys.mdx)
- [数据读写](dbvisitor/usage.mdx)
- [向量数据操作](dbvisitor/vectors.mdx)
- [结果读取](dbvisitor/results.mdx)

## 语法基础

- [请求格式与参数](basics/requests.md)
- [Hint 支持](basics/hints.md)
- [命令返回结果](basics/results.md)

## 查询命令

- [搜索与计数](query/search.md)
- [多路搜索与多文档读取](query/multiple.md)

- [读取文档与查询解释](query/document.md)

## 写入命令

- [文档写入与删除](write/documents.md)
- [按条件更新与删除](write/by-query.md)
- [写入结果](write/results.md)

## 管理命令

- [索引与映射](admin/indexes.md)
- [集群信息](admin/cluster.md)

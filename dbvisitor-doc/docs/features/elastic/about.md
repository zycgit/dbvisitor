---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Elasticsearch
---

使用 JdbcTemplate 或 Mapper 执行 REST 请求。构造器 API 也可完成基础文档操作。连接配置见 [JDBC Elasticsearch](../../drivers/elastic/connection.mdx)。

## dbVisitor 使用

- [编程式 API](/docs/features/elastic/programmatic)：查询、写入、多结果及存储过程与函数差异。
- [Mapper API](dbvisitor/mapper.md)：方法注解、Mapper 读写及执行差异。
- [构造器 API](dbvisitor/builder.md)：可用操作、数据源差异与对应用法。
- [查询操作](dbvisitor/query.mdx)：执行查询、组合条件及映射结果。
- [数据写入](dbvisitor/write.mdx)：新增、修改、删除及写入返回值。
- [分页查询](dbvisitor/pagination.mdx)：读取指定范围和查询总数。
- [向量操作](dbvisitor/vectors.mdx)：配置向量字段，进行相似度检索。
- [名称敏感性](dbvisitor/name-sensitivity.md)：配置索引名、字段名与结果列匹配。
- [主键生成](dbvisitor/generated-keys.mdx)：指定编号或读取生成的 ID。
- [参数与规则](dbvisitor/parameters.md)：绑定值及选择原生命令片段。
- [类型支持](dbvisitor/types.md)：选择字段或值对应的 Java 类型。
- [结果读取](dbvisitor/results.mdx)：读取结果列，映射返回的数据。
- [事务支持](dbvisitor/transactions.md)：事务 API 行为及隔离级别设置。

## 语法基础

[请求格式与参数](basics/requests.md) · [Hint 支持](basics/hints.md) · [命令返回结果](basics/results.md)

## 查询命令

[搜索与计数](query/search.md) · [多路搜索与多文档读取](query/multiple.md) · [读取文档与查询解释](query/document.md)

## 写入命令

[文档写入与删除](write/documents.md) · [按条件更新与删除](write/by-query.md) · [写入结果](write/results.md)

## 管理命令

[索引与映射](admin/indexes.md) · [集群信息](admin/cluster.md)

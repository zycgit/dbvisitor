---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: Elastic 特异性
description: ElasticSearch 数据源使用 dbVisitor 的全面介绍。以及使用限制。
---

dbVisitor 通过 [JDBC-Elastic](../../drivers/elastic/about) 驱动，实现基于 JDBC 协议对 ElasticSearch 数据源的访问。在使用时需要注意以下几点：
- 使用 ElasticSearch DSL 命令操作数据，可用的命令参考 [支持的命令列表](../../drivers/elastic/commands)。
- 支持使用 [JdbcTemplate](./exec_command)、[方法注解](./exec_annotation)、[Mapper 文件](./exec_file.mdx) 三种方式操作 ElasticSearch 数据源。
- 支持使用 [规则](../../rules/about)、[参数传递](../../args/about) 进行复杂命令的生成和参数传递。
- 支持使用 [ResultSetExtractor、RowMapper](../../result/about) 等接口接收查询结果。
- 支持在 Mapper File 中使用 [动态 SQL](../file/dynamic) 标签生成执行命令。
- 支持 [构造器 API](../lambda/about) 或 [通用 Mapper](../mapper/common)
- 支持 **对象映射**、**结果集映射**。
- 不支持 **JdbcTemplate** 的 **executeBatch** 方法
- 不支持 **JdbcTemplate** 的存储过程 API

## 概念类比

ElasticSearch 驱动提供的命令中，不同命令会有不同的执行结果，主要有三种（**更新数**、**单行结果**、**多行结果**）其中：
- 更新数，可以类比关系型数据库的 INSERT、UPDATE、DELETE 语句，需要使用 executeUpdate 方法执行并获取结果。
- 单行结果/多行结果：则相当于关系型数据库的在使用 SELECT 语句时获取查询结果集。在 ElasticSearch 驱动中所有读取类命令和部分无法通过更新数来返回的命令均使用结果集形式。
- 在获取结果集的时，第一列是 `_ID` 字段，第二列是 `_DOC` 字段。两个字段均是字符串格式。

## 使用指引 {#guide}

执行命令
- [命令方式](./exec_command)，使用 JdbcTemplate 执行原始的 ElasticSearch 命令并进行读写数据。
- [构造器 API](./exec_lambda)，使用 LambdaTemplate 进行类型安全的 ElasticSearch 操作。
- [BaseMapper](./exec_mapper)，使用 BaseMapper 接口简化 ElasticSearch 的 CRUD 操作。
- [注解方式](./exec_annotation)，在 Mapper 接口上使用 @Insert、@Update、@Delete 注解，定制化 ElasticSearch 的 CRUD 操作。
- [文件方式](./exec_file.mdx)，在 Mapper 文件中通过标签配置执行命令。

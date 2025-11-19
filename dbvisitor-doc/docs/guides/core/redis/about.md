---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: Redis 特异性
description: Redis 数据源使用 dbVisitor 的全面介绍。以及使用限制。
---

dbVisitor 通过 [JDBC-Redis](../../drivers/redis/about) 驱动，实现基于 JDBC 协议对 Redis 数据源的访问。在使用时需要注意以下几点：
- 使用 Redis 命令操作数据，可用的命令参考 [支持的命令列表](../../drivers/redis/commands)。
- 支持使用 [JdbcTemplate](./exec_command)、[方法注解](./exec_annotation)、[Mapper 文件](./exec_file.mdx) 三种方式操作 Redis 数据源。
- 支持使用 [规则](../../rules/about)、[参数传递](../../args/about) 进行复杂命令的生成和参数传递。
- 支持使用 [ResultSetExtractor、RowMapper](../../result/about) 等接口接收查询结果。
- 支持在 Mapper File 中使用 [动态 SQL](../file/dynamic) 标签生成执行命令。
- 不支持 **构造器 API** 或 **通用 Mapper** API
- 不支持 **对象映射**、**结果集映射**。
- 不支持 **JdbcTemplate** 的 **executeBatch** 方法
- 不支持 **JdbcTemplate** 的存储过程 API

:::tip[贴士]
虽然在使用 Redis 时 dbVisitor 不支持 **对象映射**，但仍然可以通过在建立对象映射后借助已有 API 实现关系型数据库和 Redis 之间的数据同步。
:::

## 概念类比

Redis 驱动提供的命令中，不同命令会有不同的执行结果，主要有三种（**更新数**、**单行结果**、**多行结果**）其中：
- 更新数，可以类比关系型数据库的 INSERT、UPDATE、DELETE 语句，需要使用 executeUpdate 方法执行并获取结果。
- 单行结果/多行结果：则相当于关系型数据库的 SELECT 语句，需要通过 query 系列方法来执行并获取结果，区别是结果是一行还是多行。

## 使用指引 {#guide}

执行命令
- [命令方式](./exec_command)，使用 JdbcTemplate 执行原始的 Redis 命令并进行读写数据。
- [注解方式](./exec_annotation)，在 Mapper 接口上使用 @Insert、@Update、@Delete 注解，将 Redis 操作业务化。
- [文件方式](./exec_file.mdx)，在 Mapper 文件中通过标签配置执行命令。

读写 Redis 数据类型
- [字符串](./redis_type#string)，使用 dbVisitor 读写 Redis 的 String 类型数据。
- [哈希](./redis_type#hash)，使用 dbVisitor 读写 Redis 的 Hash 类型数据。
- [列表](./redis_type#list)，使用 dbVisitor 读写 Redis 的 List 类型数据。
- [集合](./redis_type#set)，使用 dbVisitor 读写 Redis 的 Set 类型数据。
- [有序集合](./redis_type#sorted_set)，使用 dbVisitor 读写 Redis 的 Sorted Set 类型数据。

小技巧
- 使用 Map 写入多个 Key
- [BindTypeHandler 注解](./redis_type)，通过绑定特定 TypeHandler 是实现对象的序列化和反序列化，例如 JSON。
- 通过

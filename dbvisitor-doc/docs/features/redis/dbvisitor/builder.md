---
id: builder
slug: /features/redis/builder
sidebar_position: 30
title: 构造器 API
---

## 支持范围 {#support}

Redis 适配器没有构造器方言，因此不支持构造器的写入操作、写入冲突、查询操作、Map 查询模式、分页查询、条件构造器、条件参数、分组和排序。

这是构造器 API 的适用范围，不代表 Redis 命令不能通过 dbVisitor 执行。应通过 JdbcTemplate 或 Mapper 传递 Redis 命令：

```java
JdbcTemplate jdbc = new JdbcTemplate(dataSource);
jdbc.executeUpdate("SET ? ?", "user:1001:name", "Alice");
String name = jdbc.queryForObject("GET ?", new Object[] {"user:1001:name"}, String.class);
```

这里的 `?` 仍然是绑定参数；结果也可以映射到对象。不是将构造器的 `eq` 或 `in` 改写为 Redis 命令。

## 选择用法 {#alternatives}

- 数据读写：使用 `GET/SET`、`HGETALL/HSET` 等命令，见[查询操作](query.mdx)和[数据写入](write.mdx)。
- 数据遍历：使用 `SCAN` 游标，不是构造器的页码，见[分页查询](pagination.mdx)。
- 对象映射：通过命令读取后映射到 Java 对象，见[商品缓存](../scenarios/product-cache.md)。

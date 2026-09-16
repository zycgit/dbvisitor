---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Redis
---

使用 JdbcTemplate 或 Mapper 执行 Redis 命令。连接配置见 [JDBC Redis](../../drivers/redis/connection.mdx)。

## dbVisitor 使用

- [编程式 API](/docs/features/redis/programmatic)：查询、写入、多结果及存储过程与函数差异。
- [Mapper API](dbvisitor/mapper.md)：方法注解、Mapper 读写及执行差异。
- [构造器 API](dbvisitor/builder.md)：可用操作、数据源差异与对应用法。
- [查询操作](dbvisitor/query.mdx)：执行查询、组合条件及映射结果。
- [数据写入](dbvisitor/write.mdx)：新增、修改、删除及写入返回值。
- [分页查询](dbvisitor/pagination.mdx)：读取指定范围和查询总数。
- [键与编号](dbvisitor/generated-keys.mdx)：设置 Redis 键名和生成业务编号。
- [参数与规则](dbvisitor/parameters.md)：绑定值及选择原生命令片段。
- [类型支持](dbvisitor/types.md)：选择字段或值对应的 Java 类型。
- [结果读取](dbvisitor/results.mdx)：读取结果列，映射返回的数据。
- [结果接收](dbvisitor/result-handling.md)：查看三个结果处理接口在各 API 上的支持情况。
- [事务支持](dbvisitor/transactions.md)：事务 API 行为及隔离级别设置。

## 业务场景

[商品缓存](scenarios/product-cache.md) · [登录状态](scenarios/login-state.md) · [购物车](scenarios/shopping-cart.md)<br />
[浏览计数](scenarios/view-counter.md) · [用户点赞](scenarios/article-likes.md) · [积分排行榜](scenarios/points-ranking.md)

## 语法基础

[命令格式与参数](basics/commands.md) · [命令返回结果](basics/results.md)

## 命令语法

[String 字符串](string/about.md) · [Hash 散列](hash/about.md) · [List 列表](list/about.md) · [Set 集合](set/about.md) · [Sorted Set 有序集合](sorted-set/about.md)<br />
[键管理](keys/about.md) · [服务器命令](server/about.md)

---
slug: mysql_stream_read
title: MySQL 流式读取超大表
description: 详解如何使用 dbVisitor 实现 MySQL 亿级数据流式查询，避免 OOM。
authors: [ZhaoYongChun]
tags: [dbVisitor, JDBC, Streaming]
language: zh-cn
---

在处理海量数据（如导出报表、全表迁移、数据清洗）时，如果直接使用普通的 `SELECT *` 查询，JDBC 驱动默认会将所有结果集全部加载到内存中。对于几百万甚至上亿行的表，这几乎百分之百会导致 **OOM (Out Of Memory)** 异常。

本文将介绍如何在 dbVisitor 中利用 MySQL 的流式查询特性，优雅地解决超大表全量读取问题。

<!-- truncate -->

## 为什么需要流式查询？

通常我们查询数据有两种策略：

1.  **全量加载**：传统的 `List<T> list = template.query(...)`。
    *   *优点*：使用简单，连接快速释放。
    *   *缺点*：内存占用与数据量成正比，大数据量下必死。
2.  **分页查询**：使用 `LIMIT offset, size`。
    *   *优点*：内存可控。
    *   *缺点*：**深度分页性能灾难**。随着 `offset` 越来越大，数据库需要扫描并丢弃的行数以几何级数增加，越往后越慢。

**流式查询 (Streaming)** 是第三种选择。它通过维护一个长连接，让数据库像“流水”一样逐行（或分批）将数据推送给客户端。客户端处理完一行，丢弃一行，内存占用始终维持在一个极低的水平（仅缓存少量行数据），从而实现 **“单机处理亿级数据”**。

## MySQL 的特殊性

不同的数据库开启流式查询的方式不同（例如 PostgreSQL 需要关闭自动提交并设置 fetchSize）。而对于 **MySQL**，JDBC 驱动有着非常特殊的约定。

根据 [MySQL Connector/J 官方文档](https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-implementation-notes.html)，启用流式结果集必须满足以下**三个硬性条件**：

1.  `ResultSet.TYPE_FORWARD_ONLY`：只能向前遍历。
2.  `ResultSet.CONCUR_READ_ONLY`：只读结果集。
3.  **`fetchSize` 必须设置为 `Integer.MIN_VALUE`**。

如果不满足任何一个条件，驱动就会退化为全量加载模式。

## dbVisitor 实现方案

dbVisitor 的核心组件 `JdbcTemplate` 提供了极强的底层掌控力，允许我们通过 `PreparedStatementCreator` 来定制上述参数，同时配合 `RowCallbackHandler` 实现逐行消费。

### 1. 定制 Statement

我们需要接管 `Statement` 的创建过程，强制设置 `fetchSize`。

```java
// 使用 lambda 定义 PreparedStatement 创建逻辑
PreparedStatementCreator creator = con -> {
    // 1. 创建 Statement 时指定游标类型
    PreparedStatement ps = con.prepareStatement(
        "select id, name, age from huge_user_table", // 建议显式指定列名而非 *
        ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY
    );
    
    // 2. 开启 MySQL 流式黑魔法
    ps.setFetchSize(Integer.MIN_VALUE);
    return ps;
};
```

### 2. 定义行映射器

为了方便处理，我们通常需要将 `ResultSet` 的每一行转换为 Java 对象。dbVisitor 提供了高性能的映射器。

```java
// 自动将 ResultSet 映射为 User 对象
MappingRowMapper<User> rowMapper = new MappingRowMapper<>(User.class);
```

### 3. 逐行消费回调

由 `RowCallbackHandler` 接管每一行数据的处理。**注意：** 在这里面的逻辑必须是轻量级的，处理完一行，该行引用的对象就会变成垃圾回收的候选者。

```java
RowCallbackHandler handler = (rs, rowNum) -> {
    // 映射当前行
    User user = rowMapper.mapRow(rs, rowNum);
    
    // 业务逻辑：例如写入文件、发送 MQ、计算统计等
    processUser(user);
    
    // 进度日志：每处理 1万行打印一次
    if (rowNum % 10000 == 0) {
        System.out.println("Current processing: " + rowNum);
    }
};
```

### 4. 完整代码示例

将上述步骤组合起来：

```java
@Test
public void testStreamQuery() throws SQLException {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    // 1. 定制器：开启流式
    PreparedStatementCreator creator = con -> {
        PreparedStatement ps = con.prepareStatement(
            "select * from test_user",
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_READ_ONLY
        );
        ps.setFetchSize(Integer.MIN_VALUE);
        return ps;
    };

    // 2. 执行器：流式回调
    // executeCreator 方法会负责 Connection 的获取与释放
    jdbcTemplate.executeCreator(creator, (RowCallbackHandler) (rs, rowNum) -> {
        // 直接操作 ResultSet，或者使用 Mapper 转换
        String name = rs.getString("name");
        int age = rs.getInt("age");
        
        // 模拟业务处理
        // System.out.println("Processing: " + name);
    });
}
```

## 性能对比

假设我们有一张 500 万行的表，JVM 堆内存限制为 512MB。

| 查询方式 | 内存占用 | 耗时 | 结果 |
| :--- | :--- | :--- | :--- |
| **普通的 query()** | > 800MB | N/A | **OOM 崩溃** |
| **分页查询 (Size=1000)** | ~50MB | 极慢 | 随着页数增加，查询越来越慢，总耗时极长 |
| **dbVisitor 流式查询** | **< 30MB** | **快** | 稳定完成，GC 平稳 |

## 注意事项

1.  **连接独占**：流式查询期间，该数据库连接会被一直占用，直到所有数据遍历完毕。请确保 `Connection Pool` 足够大或查询处理够快。
2.  **并发问题**：由于一直占用连接，如果业务也是高并发的，建议使用独立的连接池或数据源来执行此类分析型任务，避免阻塞核心业务。
3.  **网络超时**：如果 `RowCallbackHandler` 处理逻辑非常耗时（例如每一行都要调个远程接口），可能会因为长时间未读取数据导致数据库服务端断开连接（`net_write_timeout`）。
    *   *建议*：如果是重业务逻辑，建议采用 "生产者-消费者" 模型。`handler` 只负责快速将数据放入阻塞队列，另起线程池慢慢处理。

通过 dbVisitor 提供的底层 API，我们可以轻松驾驭这种复杂的数据库特性，在保持代码整洁的同时，解决棘手的性能问题。

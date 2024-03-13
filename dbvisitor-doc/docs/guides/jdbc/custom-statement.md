---
id: custom_execute
sidebar_position: 8
title: 流式读取超大表
description: 使用 dbVisitor ORM 查询并处理一张超大表。
---

# 流式读取超大表

当查询一张超大表并获取它的结果集时要使用 `流式返回` 否则内存极易出现溢出。
不同的数据库开启流式返回的方式虽有差异，但都需要设置 `Statement/PreparedStatement` 的参数。

下面就以 MySQL 为例展示一下通过定制 `Statement` 实现流式查询的例子：

```java
// 定制 PreparedStatement
PreparedStatementCreator creator = con -> {
    PreparedStatement ps = con.prepareStatement(
        "select * from test_user",
        ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY
    );
    ps.setFetchSize(Integer.MIN_VALUE);
    return ps;
};

// 行读取工具
MappingRowMapper<TestUser> rowMapper = new MappingRowMapper<>(TestUser.class);

// 流式消费数据
RowCallbackHandler handler = (rs, rowNum) -> {
TestUser dto = rowMapper.mapRow(rs, rowNum);

};

// 执行流式处理
jdbcTemplate.executeCreator(creator, handler);
```

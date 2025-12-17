---
slug: mysql_stream_read
title: Streaming a Huge MySQL Table
description: Use dbVisitor ORM to query and process a very large table with streaming.
authors: [ZhaoYongChun]
tags: [Streaming]
language: en
---

When querying a huge table, enable streaming results; otherwise the result set can easily exhaust memory. Each database enables streaming differently, but all require configuring `Statement`/`PreparedStatement` parameters.

Here is a MySQL example using a customized `PreparedStatement` to stream the result set:

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

// Row mapper
MappingRowMapper<TestUser> rowMapper = new MappingRowMapper<>(TestUser.class);

// Stream and consume rows
RowCallbackHandler handler = (rs, rowNum) -> {
    TestUser dto = rowMapper.mapRow(rs, rowNum);
    // handle dto
};

// Execute streaming
jdbcTemplate.executeCreator(creator, handler);
```

---
slug: mysql_stream_read
title: MySQL Streaming Read for Huge Tables
description: Read large MySQL results row by row with dbVisitor and Connector/J to control application memory.
authors: [ZhaoYongChun]
tags: [dbVisitor, JDBC, Streaming]
language: en
---

A normal `SELECT *` loads all results into memory, easily causing OOM on huge tables. This article shows how to use dbVisitor's streaming query to read MySQL large tables with bounded result buffering.

<!-- truncate -->

## Why Streaming Query?

Usually, we have two strategies for querying data:

1.  **Full Load**: Traditional `List<T> list = template.queryForList(...)`.
    *   *Pros*: Simple to use, connection released quickly.
    *   *Cons*: Memory consumption is proportional to data volume; may exhaust memory for large results.
2.  **Pagination Query**: Using `LIMIT offset, size`.
    *   *Pros*: Controllable memory.
    *   *Cons*: Deep pagination overhead. As `offset` gets larger, the number of rows the database needs to scan and discard increases with the offset, becoming slower and slower.

**Streaming Query** is the third choice. It maintains a long connection, letting the database push data to the client row by row (or in batches) like "running water". The client processes one row and discards one row, avoiding retention of the full result set if the application does not collect rows. Actual memory usage and throughput depend on row size and consumption logic.

## MySQL Specifics

Different databases enable streaming queries in different ways (e.g., PostgreSQL requires turning off auto-commit and setting fetchSize). For **MySQL**, the JDBC driver has very specific conventions.

According to the [MySQL Connector/J Official Documentation](https://dev.mysql.com/doc/connector-j/en/connector-j-reference-implementation-notes.html), the following configuration enables row-by-row streaming:

1.  `ResultSet.TYPE_FORWARD_ONLY`: Can only traverse forward.
2.  `ResultSet.CONCUR_READ_ONLY`: Read-only result set.
3.  **`fetchSize` must be set to `Integer.MIN_VALUE`**.

This is not the only way to fetch batches. Connector/J also supports cursor fetching with useCursorFetch=true and a positive fetchSize; distinguish these modes.

## dbVisitor Implementation

dbVisitor's core component `JdbcTemplate` provides strong underlying control capability, allowing us to customize the above parameters through `PreparedStatementCreator`, while cooperating with `RowCallbackHandler` to achieve row-by-row consumption.

### 1. Customize Statement

We need to take over the creation process of `Statement` and forcibly set `fetchSize`.

```java
// Use lambda to define PreparedStatement creation logic
PreparedStatementCreator creator = con -> {
    // 1. Specify cursor type when creating Statement
    PreparedStatement ps = con.prepareStatement(
        "select id, name, age from huge_user_table", // Recommend explicitly specifying column names rather than *
        ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY
    );
    
    // 2. Enable Connector/J row-by-row streaming
    ps.setFetchSize(Integer.MIN_VALUE);
    return ps;
};
```

### 2. Define Row Mapper

To facilitate processing, we usually need to convert each row of `ResultSet` into a Java object. dbVisitor provides high-performance mappers.

```java
// Automatically map ResultSet to User object
BeanMappingRowMapper<User> rowMapper = new BeanMappingRowMapper<>(User.class);
```

### 3. Row-by-Row Consumption Callback

`RowCallbackHandler` takes over the processing of each row of data. **Note:** The logic here must be lightweight. Once a row is processed, the object referenced by that row becomes a candidate for garbage collection.

```java
RowCallbackHandler handler = (rs, rowNum) -> {
    // Map current row
    User user = rowMapper.mapRow(rs, rowNum);
    
    // Business logic: e.g., write to file, send MQ, calculate statistics, etc.
    processUser(user);
    
    // Progress log: print once every 10,000 rows processed
    if (rowNum % 10000 == 0) {
        System.out.println("Current processing: " + rowNum);
    }
};
```

### 4. Complete Code Example

Combining the above steps:

```java
public void streamUsers() throws SQLException {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    // 1. Creator: Enable Streaming
    PreparedStatementCreator creator = con -> {
        PreparedStatement ps = con.prepareStatement(
            "select * from test_user",
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_READ_ONLY
        );
        ps.setFetchSize(Integer.MIN_VALUE);
        return ps;
    };

    // 2. Executor: Streaming Callback
    // The executeCreator method is responsible for Connection acquisition and release
    jdbcTemplate.executeCreator(creator, (RowCallbackHandler) (rs, rowNum) -> {
        // Operate ResultSet directly, or use Mapper to convert
        String name = rs.getString("name");
        int age = rs.getInt("age");
        
        // Simulate business processing
        // System.out.println("Processing: " + name);
    });
}
```

## Performance and Resources

Streaming controls result buffering but does not guarantee a faster query. Choose row streaming, cursor fetching, or keyset pagination based on row width, network, indexes, and consumption speed; row count alone cannot predict memory usage.

## Precautions

1.  **Connection Monopoly**: During the streaming query, the database connection will be occupied until the result set is exhausted or closed; do not execute another query on the same connection before then. Ensure the `Connection Pool` is large enough or query processing is fast enough.
2.  **Concurrency Issues**: Since the connection is occupied continuously, if the business is also high-concurrency, it is recommended to use an independent connection pool or data source to execute such analytical tasks to avoid blocking core business.
3.  **Network Timeout**: If the `RowCallbackHandler` processing logic is very time-consuming (e.g., calling a remote interface for every row), it may cause the database server to disconnect (`net_write_timeout`) because data has not been read for a long time.
    *   *Suggestion*: If heavy business logic is involved, it is recommended to adopt the "Producer-Consumer" model. `handler` is only responsible for quickly putting data into a bounded blocking queue and a separate thread pool consumes it, with consumer errors propagated. An unbounded queue can accumulate the entire result; prolonged blocking on a bounded queue can still cause network timeouts.

Through the underlying API provided by dbVisitor, we can easily master this complex database feature, solving tricky performance problems while keeping the code clean.

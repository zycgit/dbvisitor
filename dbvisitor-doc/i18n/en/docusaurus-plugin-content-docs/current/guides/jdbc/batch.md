---
id: batch
sidebar_position: 3
title: 批量
description: dbVisitor ORM 工具针对 JDBC 的 batch 方法做了封装，现在可以更加方便的使用它。
---

# 批量

dbVisitor 提供提供了多个基于 SQL 批量操作接口。

## 批量执行 SQL 命令

批量执行 SQL 命令适用于不需要参数的语句，它们将会通过 `Statement` 接口来执行。比如下面这个批量执行 `insert`。

```java
int[] result = jdbcTemplate.executeBatch(new String[] {
    "insert into `test_user` values (11, 'david', 26, now())",
    "insert into `test_user` values (12, 'kevin', 26, now())"
});
```

```text title='执行结果'
[1, 1]
```

## 参数化 SQL 批量执行

批量执行带参的 SQL

```java
String querySql = "insert into `test_user` values (?,?,?,?)";
Object[][] queryArg = new Object[][] {
    new Object[] { 20, "david", 26, new Date() },
    new Object[] { 22, "kevin", 26, new Date() }
};

int[] result = jdbcTemplate.executeBatch(querySql, queryArg);
```

```text title='执行结果'
[1, 1]
```

## Map数组作为批量参数

批量执行带参的 SQL，使用 Map 作为入参

```java
String querySql = "update test_user set name = :name where id = :id";
Map<String, Object> record1 = new HashMap<>();
record1.put("name", "jack");
record1.put("id", 1);

Map<String, Object> record2 = new HashMap<>();
record2.put("name", "steve");
record2.put("id", 2);

Map<String, Object>[] queryArg = new Map[] { record1, record2 };
int[] result = jdbcTemplate.executeBatch(querySql, queryArg);
```

```text title='执行结果'
[1, 1]
```

## BatchPreparedStatementSetter

使用 BatchPreparedStatementSetter 接口进行参数批量设置

```java
String querySql = "delete from test_user where id = ?";
Object[][] queryArg = new Object[][] { new Object[] { 1 }, new Object[] { 2 } };

BatchPreparedStatementSetter batchSetter = new BatchPreparedStatementSetter() {
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        ps.setObject(1, queryArg[i][0]);
    }

    public int getBatchSize() {
        return queryArg.length;
    }
};

int[] result = jdbcTemplate.executeBatch(querySql, batchSetter);
```

```text title='执行结果'
[1, 1]
```
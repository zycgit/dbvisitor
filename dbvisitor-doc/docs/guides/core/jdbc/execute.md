---
id: execute
sidebar_position: 7
title: 脚本
description: 利用 JdbcTemplate 的 execute 方法，可以执行任意 SQL 语句。
---

# 脚本

利用 JdbcTemplate 的 execute 方法，可以执行任意 SQL 语句。

:::warning
`loadSQL`、`loadSplitSQL` 不提供绑定参数。执行需要参数的单条语句时，可使用 `execute(sql, args)`。`loadSplitSQL` 按分隔符切分，不应直接用于包含同样分隔符的字符串或存储过程正文。
:::

## 用法

- 执行 SQL 语句
    ```java
    jdbc.execute("insert into users (id, name) values(2, 'Alice')");
    ```
- 加载 SQL 资源
    ```java
    jdbc.loadSQL("scripts/mysql-script.sql");
    ```
    ```java
    jdbc.loadSQL(StandardCharsets.UTF_8, "scripts/mysql-script.sql");
    ```
    ```java
    jdbc.loadSQL(new FileReader("/home/users/my/scripts/mysql-script.sql"));
    ```
- 加载 SQL 资源，并根据 _**splitChars**_ 参数将其拆分执行。
    ```java
    jdbc.loadSplitSQL(";", "scripts/mysql-script.sql");
    ```
    ```java
    jdbc.loadSplitSQL(";", StandardCharsets.UTF_8, "scripts/mysql-script.sql");
    ```
    ```java
    jdbc.loadSplitSQL(";", new FileReader("/home/users/my/scripts/mysql-script.sql"));
    ```

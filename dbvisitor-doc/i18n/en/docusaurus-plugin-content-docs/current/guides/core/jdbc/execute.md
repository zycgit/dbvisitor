---
id: execute
sidebar_position: 7
title: Scripts
description: Use JdbcTemplate.execute to run arbitrary SQL statements or load SQL scripts.
---

# Scripts

Use JdbcTemplate's execute method to run arbitrary SQL statements.

:::warning
Script-loading methods (`loadSQL` / `loadSplitSQL`) do **not** bind arguments. The `execute(sql, args)` overload does support binding; see [Arguments](../../args/about).
:::

## Usage

- Execute inline SQL
    ```java
    jdbc.execute("insert into users (id, name) values(2, 'Alice')");
    ```
- Load SQL resources
    ```java
    jdbc.loadSQL("scripts/mysql-script.sql");
    ```
    ```java
    jdbc.loadSQL(StandardCharsets.UTF_8, "scripts/mysql-script.sql");
    ```
    ```java
    jdbc.loadSQL(new FileReader("/home/users/my/scripts/mysql-script.sql"));
    ```
- Load SQL resources and split by `splitChars`
    ```java
    jdbc.loadSplitSQL(";", "scripts/mysql-script.sql");
    ```
    ```java
    jdbc.loadSplitSQL(";", StandardCharsets.UTF_8, "scripts/mysql-script.sql");
    ```
    ```java
    jdbc.loadSplitSQL(";", new FileReader("/home/users/my/scripts/mysql-script.sql"));
    ```

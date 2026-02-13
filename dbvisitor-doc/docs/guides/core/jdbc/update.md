---
id: update
sidebar_position: 3
hide_table_of_contents: true
title: 更新
description: 用于执行无结果集的 SQL 语句，例如：INSERT、UPDATE、DELETE 或 DDL 操作。
---

# 更新

用于执行无结果集的 SQL 语句，例如：INSERT、UPDATE、DELETE 或 DDL 操作。

:::info
- dbVisitor 支持多种参数传递方式，为了便于理解下面执行 SQL 的案例中选择常用的 **无参数**、**位置参数**、**名称参数** 三种。
- 想要了解更多参数传递内容请到 **[参数传递](../../args/about)** 页面查看。
:::

## 用法

- 执行 **DDL**
    ```java
    int res = jdbc.executeUpdate("create table user_back(id bigint, name varchar(120));");
    ```
- 执行 **INSERT**、**UPDATE**、**DELETE**
    ```java title='不使用参数'
    int res = jdbc.executeUpdate("insert into users (id, name) values(2, 'Alice')");
    ```
    ```java title='使用位置参数'
    Object[] args = new Object[] { 2, "Alice" };
    int res = jdbc.executeUpdate("insert into users (id, name) values(?, ?)", args);
    ```
    ```java title='使用名称参数'
    Map<String, Object> args = new HashMap<>();
    args.put("id", 2);
    args.put("name", "Alice");
    
    int res = jdbc.executeUpdate("insert into users (id, name) values(:id, :name)", args);
    ```

## 返回值

**JdbcTemplate** 在执行 SQL 语句时是基于 **PreparedStatement** 或者 **Statement**。
该方法要求 SQL 必须是一个 SQL 数据操作语言（Data Manipulation Language，DML）语句。
- 比如：**INSERT**、**UPDATE** 或 **DELETE** 语句。
- 或者是无返回内容的 SQL 语句，比如 **DDL** 语句。

返回值会有如下两种情况：
- 执行 SQL 数据操作语言 (DML) 语句时：返回值表示受影响的行数。
- 对于无返回内容的 SQL 语句，比如 DDL。返回 0。

:::info
尽管根据 JDBC 规范行为应当如上述情况所描述，但在实际实践过程中开发者仍然需要注意 Driver 驱动程序对 JDBC 具体实现行为。

这部分资料需要参考应用程序所选择的驱动程序。
:::

---
id: update
sidebar_position: 3
title: 更新
description: 用于执行无结果集的 SQL 语句，例如：INSERT、UPDATE、DELETE 或 DDL 操作。
---

# 更新

`executeUpdate` 用于执行无结果集的 SQL 语句，例如 INSERT、UPDATE、DELETE 或 DDL。

## 先看场景

| 你的目标 | 推荐入口 |
| --- | --- |
| 执行 INSERT、UPDATE、DELETE | `executeUpdate` |
| 执行简单 DDL | `executeUpdate` 或 [脚本](./execute) |
| 多组参数批量写入 | [批量化](./batch) |
| 执行 SQL 文件或多条脚本 | [脚本](./execute) |
| 需要更复杂的参数绑定 | [参数传递](../../args/about) |

## 执行 INSERT

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

## 执行 UPDATE

```java title='使用位置参数'
Object[] args = new Object[] { "Alice", 2 };
int res = jdbc.executeUpdate("update users set name = ? where id = ?", args);
```

```java title='使用名称参数'
Map<String, Object> args = CollectionUtils.asMap("id", 2, "name", "Alice");
int res = jdbc.executeUpdate("update users set name = :name where id = :id", args);
```

## 执行 DELETE

```java title='使用位置参数'
int res = jdbc.executeUpdate("delete from users where id = ?", new Object[] { 2 });
```

## 执行 DDL

```java
int res = jdbc.executeUpdate("create table user_back(id bigint, name varchar(120));");
```

:::tip[提示]
如果要执行 SQL 文件、多条脚本或不需要参数的脚本资源，优先阅读 [脚本](./execute)。
:::

## 参数传递

`executeUpdate` 支持无参数、位置参数、名称参数，也支持自定义参数设置逻辑。以下示例展示常见用法，完整参数能力见 [参数传递](../../args/about)。

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

## 相关内容

- [批量化](./batch)：多组参数或多条 SQL 的批量写入。
- [脚本](./execute)：执行 SQL 脚本文件或多条脚本。
- [参数传递](../../args/about)：位置参数、名称参数、自定义参数来源。

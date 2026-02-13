---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: JdbcTemplate 类
description: 基于 SQL 字符串的数据库操作封装，自动处理连接管理和异常处理。
---

# JdbcTemplate

`JdbcTemplate` 是 dbVisitor 专门为 **SQL 字符串** 场景设计的数据库操作封装。它是无状态的，可随时创建和销毁。

```java title='创建和使用'
JdbcTemplate jdbc = new JdbcTemplate(dataSource);

// 查询
List<Map<String, Object>> rows = jdbc.queryForList("select * from users where age > ?", 18);

// 更新
int affected = jdbc.executeUpdate("update users set name = ? where id = ?", "alice", 1);
```

:::tip[提示]
JdbcTemplate 的获取方式取决于项目架构，详见 **[框架整合](../../yourproject/buildtools#integration)**。
:::

## 原理

JdbcTemplate 基于 Template 模式，在模板方法内部自动处理获取连接、释放连接、捕获异常。上层代码只需专注于使用 Connection。

```java title='核心模板方法'
T result = jdbc.execute((ConnectionCallback<T>) con -> {
   // 直接使用 Connection
});
```

## 使用指引 {#guide}

- [查询](./query)，执行 SELECT 或其它带返回结果的语句。
- [更新](./update)，执行 INSERT、UPDATE、DELETE 或 DDL。
- [批量化](./batch)，执行批量操作。
- [存储过程](./procedure)，调用存储过程/存储函数。
- [规则](../../rules/about)，通过规则赋予 SQL 动态能力。
- [多值](./multi)，执行多条语句的 SQL 并获取所有结果。
- [脚本](./execute)，执行 SQL 脚本文件或多条语句。
- [使用模板](./template)，通过模板方法直接操作 Connection。
- [高级特性](./feature)，JdbcTemplate 特有属性与功能。
- [参数传递](../../args/about)，了解不同的参数传递方式。
- [接收结果](../../result/about)，了解不同的结果接收方式。

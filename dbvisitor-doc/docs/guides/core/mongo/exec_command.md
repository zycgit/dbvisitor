---
id: exec_command
sidebar_position: 2
hide_table_of_contents: true
title: 执行命令
description: 使用 JdbcTemplate 执行原始的 MongoDB 命令并进行读写数据。
---

使用 JdbcTemplate 可以直接执行 MongoDB 命令并进行读写数据操作，在此之前请确保已经正确配置好 MongoDB 数据源，具体请参考 [MongoDB 驱动使用指南](../../drivers/mongo/usecase)。

:::tip[提示]
更多使用方式请参考 [JdbcTemplate 类](../jdbc/about#guide)，在使用过程中下面两个特性由于驱动原因无法支持：
- 批量化
- 存储过程
:::

## JdbcTemplate 接口

定义 JdbcTemplate，使用创建好的 DataSource 或 Connection 作为参数传入。

```java title='创建 JdbcTemplate'
// 2，创建 Session
JdbcTemplate jdbc = new JdbcTemplate(dataSource);
或者
JdbcTemplate jdbc = new JdbcTemplate(connection);
```

## 插入数据

```java title='插入数据'
// 直接命令方式
jdbc.execute("test.user_info.insert({name: 'mali', age: 26})");
// 参数化命令方式
jdbc.execute("test.user_info.insert(?)", new Object[] { "{name: 'mali', age: 26}" });
```

## 查询数据

```java title='查询列表'
// 查询所有
List<Map<String, Object>> list = jdbc.queryForList("test.user_info.find()");
```

```java title='条件查询'
// 查询特定条件
Map<String, Object> mali = jdbc.queryForMap("test.user_info.find({name: 'mali'})");
String json = (String) mali.get("JSON");
```

## 更新数据

```java title='更新数据'
// 更新年龄
jdbc.execute("test.user_info.update({name: 'mali'}, {$set: {age: 27}})");
```

## 删除数据

```java title='删除数据'
// 删除特定记录
jdbc.execute("test.user_info.remove({name: 'mali'})");
```

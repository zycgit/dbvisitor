---
id: multi
sidebar_position: 6
hide_table_of_contents: true
title: 多值
description: 使用 JdbcTemplate 类的 multipleExecute 系列方法可以解决 SQL 语句在产生多个值后难以辨识和获取的问题。
---

使用 **JdbcTemplate** 类的 **multipleExecute** 系列方法可以解决 SQL 语句在产生多个值后难以辨识和获取的问题。

## 产生条件
- 一次性将多条 **未经切分** 的 SQL 语句发给数据库进行执行。
  ```sql title="例如：将下列语句作为一个整体发送给 MySQL 进行执行"
  set @userName = convert(? USING utf8);
  select * from test_user where name  = @userName;
  select * from test_user where name != @userName;
  ```
  :::info
  - 这通常需要数据库支持，例如：MySQL 在连接字符串上需要设置 “allowMultiQueries=true” 参数。
  :::
- 在编写存储过程时，由于 **有意** 或 **无意** 的方式产生的多个结果集。
  ```sql title="例如：下列 MySQL 存储过程产生了 2 个结果集"
  create procedure proc_multi_result(in userName varchar(200))
  begin
    select * from test_user where name  = @userName;
    select * from test_user where name != @userName;
  end;
  ```

- 其它可能的情况。

## 用法

```java title='通过一个参数查询两个结果集。分别为：1.符合参数条件的、2.不符合参数条件的。'
String multipleSql = " set @userName = convert(? USING utf8);" +
                     " select * from test_user where name  = @userName;" +
                     " select * from test_user where name != @userName;";
Map<String, Object> result = jdbc.multipleExecute(multipleSql, "muhammad");
```

- dbVisitor 支持多种参数传递方式，想要了解更多参数传递内容请到 **[参数传递](../../args/about)** 页面查看。

## 返回值

- 默认情况下在多值查询中，所有返回的结果集会统一采用 **[List/Map](../../result/for_map)** 结构。
  即：上述查询会返回两个 Map 类型的数组，并存放在 List 结构中。
- dbVisitor 提供了一种能力，允许在 SQL 语句中通过规则指定何种方式处理对应语句的查询结果。

```sql title="1. 通过在语句中添加 ‘@{resultSet} 规则’ 指定结果集类型"
➊ set @userName = convert(? USING utf8);           ➋ @{resultUpdate,name=upd}
➌ select * from test_user where name  = @userName; ➍ @{resultSet,name=res1,javaType=net.demo.dto.User}
➎ select * from test_user where name != @userName; ➏ @{resultSet,name=res2,javaType=net.demo.dto.User}
```

上面例子中：
- ➊ 语句使用 ➋ 规则，规则为更新数设定了名称 `upd`。
- ➌ 语句使用 ➍ 规则，规则为结果集设定了名称 `res1` 和类型 `net.demo.dto.User`。
- ➎ 语句使用 ➏ 规则，规则为结果集设定了名称 `res2` 和类型 `net.demo.dto.User`。

```sql title="2. 执行含有 ‘@{resultSet} 规则’ 的查询并获取结果集"
String query = "set @userName = convert(? USING utf8);           @{resultUpdate,name=upd}" +
               "select * from test_user where name  = @userName; @{resultSet,name=res1,javaType=net.demo.dto.User}" +
               "select * from test_user where name != @userName; @{resultSet,name=res2,javaType=net.demo.dto.User}";
Map<String, Object> result = jdbcTemplate.multipleExecute(query, "muhammad");

List<User> result1 = result.get("res1"); // 对应 ➌ 语句的结果
List<User> result2 = result.get("res2"); // 对应 ➎ 语句的结果
```

:::info
更多关于 `@{resultSet,xxx}` 规则的用法请参考 **<span class="badge badge--warning">[RESULT 规则](../../rules/result_rule#result-set)</span>**。
:::

:::info
- **multipleExecute** 查询返回结果会使用 **LinkedCaseInsensitiveMap** 或 **LinkedHashMap** 来保证多个语句结果之间的顺序。
- 详细信息请参考 **[Map 大小写敏感性](./query#case)**。
:::

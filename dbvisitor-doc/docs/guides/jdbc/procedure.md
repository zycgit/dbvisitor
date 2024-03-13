---
id: execute_procedure
sidebar_position: 4
title: 存储过程
description: 使用 dbVisitor ORM 工具执行存储过程，并获取返回数据。
---

# 存储过程

dbVisitor 支持存储过程的调用。以 MySQL 为例，有下例存储。执行存储过程后会产生 `1` 个入参，`1` 个出参，`2` 个结果集

```sql
drop procedure if exists proc_select_table;
create procedure proc_select_table(in userName varchar(200),
                                   out outName varchar(200))
begin
    select * from test_user where name = userName;
    select * from test_user;
    set outName = concat(userName,'-str');
end;
```

**执行存储过程，并接收所有返回的数据**

```java
List<SqlParameter> parameters = new ArrayList<>();
parameters.add(SqlParameterUtils.withInput("dative", Types.VARCHAR));
parameters.add(SqlParameterUtils.withOutputName("outName", Types.VARCHAR));

String querySql = "{call proc_select_table(?,?)}";
Map<String, Object> result = jdbcTemplate.call(querySql, parameters);
```

执行结果中获取输出参数的方式如下：

```java
String outName = resultMap.get("outName");
```

执行结果中分别获取两个 select 结果的方式如下：

```java
// 第一个 select 的结果
List<Map<String, Object>> result1 = resultMap.get("#result-set-1");

// 第二个 select 的结果
List<Map<String, Object>> result2 = resultMap.get("#result-set-2");
```

:::tip
通过 `jdbcTemplate.call` 调用存储过程返回的结果集，只会以 `List/Map` 形式返回。
:::

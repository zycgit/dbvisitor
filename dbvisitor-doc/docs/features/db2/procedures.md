---
id: procedures
sidebar_position: 11
title: 存储过程调用
---

## 传入参数与读取返回值 {#parameters}

标量值可以通过 IN、OUT、INOUT 参数传递。例如创建一个计算两数之和的存储过程：

```sql
CREATE PROCEDURE add_numbers(IN a INT, IN b INT, INOUT result INT)
BEGIN SET result = a + b; END
```

将完整定义作为一条语句执行。调用时绑定输入值，并声明返回参数：

```java
Map<String, Object> result = jdbc.call(
        "CALL add_numbers(?, ?, ?)",
        new Object[] { 10, 5, SqlArg.asInOut("result", 0, Types.INTEGER) });
Integer sum = (Integer) result.get("result"); // 15
```

也支持命名参数、参数类型配置和多个标量返回值。不支持将返回参数配置为 JDBC REF_CURSOR 来映射游标；过程直接返回记录时，使用[结果集规则](../../guides/rules/result_rule.mdx#result-set)，不要声明 REF_CURSOR 参数。

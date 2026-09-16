---
id: functions
sidebar_position: 12
title: 函数查询
---

## 标量函数与表函数 {#functions}

ClickHouse 函数通过 SELECT 执行。SQL UDF 返回标量；表函数位于 FROM 中，返回多条记录。

```sql
CREATE FUNCTION add_numbers AS (a, b) -> a + b;
```

```java
Integer sum = jdbc.queryForObject("SELECT add_numbers(?, ?)",
        new Object[] { 10, 5 }, Integer.class);
List<Map<String, Object>> rows = jdbc.queryForList("SELECT number FROM numbers(5)");
```

位置参数、命名参数均可使用。结果通过 JdbcTemplate 查询方法接收；不支持 JDBC CallableStatement 回调，也不通过 OUT 参数返回记录。

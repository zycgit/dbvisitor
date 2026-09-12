---
id: operators
sidebar_position: 4
title: 运算符
---

## 标量表达式

| 表达式 | 执行语义 |
| --- | --- |
| `=`、`==`、`!=`、`<>`、`>`、`>=`、`<`、`<=` | 服务端比较；`=` 转为 `==`，`<>` 转为 `!=`。 |
| `AND`、`OR`、`NOT`、括号 | 组合过滤条件，保留 NOT 的作用范围；复杂逻辑请显式加括号。 |
| `field BETWEEN lower AND upper` | 转为包含两端的 `>=` 与 `<=`；不交换反向边界。NOT BETWEEN 转为 `< lower OR > upper`。 |
| `field IN [...]`、`field IN (...)`、`field IN ?` | 列表成员判断；NOT IN 为排除列表。包含占位符的列表作为一个 SDK 模板数组绑定。 |
| `field IS NULL`、`field IS NOT NULL` | 原生空值判断，不等价于 `= NULL`；字段类型须支持空值过滤。 |
| `field LIKE 'prefix%'` | 模式匹配，模式规则由 Milvus 决定。 |
| 算术表达式、`function(arguments)` | 在 WHERE 中传给服务端，不由 Java 计算；解析成功不表示服务端支持该函数或类型组合。 |

算术运算符两侧建议保留空格，避免连字符被识别为名称的一部分。SET 只支持值赋值，不支持 `SET age = age + 1`。SELECT 投影仅支持 `*` 或字段列表，`COUNT(*)` 使用独立计数形式；不支持 JOIN、AS 别名、任意投影表达式、关系型 GROUP BY 或标量 ORDER BY。

向量范围不是普通标量表达式，其距离算子及 AND/OR/NOT 组合限制见 [SELECT 与向量搜索](../query/select.md)。不提供任意 JSON 路径或所有原生表达式语法的自动透传。

### Milvus 2.6.2 参数限制

Milvus 2.6.2 不接受 LIKE 右侧的模板参数，因此 `LIKE ?` 虽可解析并通过 SDK 绑定，执行时仍会被服务端拒绝。固定 SQL 模式字面量可用；不要通过拼接不可信输入绕过限制。

该版本的整数模板比较 `NOT (age = ?)` 及双重 NOT 可触发 QueryNode 断言。普通 `age != ?`、字面量 NOT、`NOT (field IS NULL)` 不属于此问题。驱动不通过拼接参数或消除 NOT 模拟支持。

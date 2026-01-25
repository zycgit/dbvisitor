---
id: rule
sidebar_position: 5
hide_table_of_contents: true
title: 6.4 规则传参
description: 语句中通过 @{...} 写法，可以借助规则机制，优雅的处理一些常见动态 SQL 场景。
---

# 规则传参

在 SQL 中通过 `@{...}` 的方式传递参数并调用内置规则引擎处理。这种方式允许 SQL 语句根据参数值动态变化，而无需编写复杂的 `if/else` 或 XML 标签。

## 为什么使用规则

*   **简洁直观**：摆脱繁杂的 XML 标签（如 MyBatis 的 `<if>`, `<foreach>`），让 SQL 回归 SQL。
*   **智能处理**：自动处理空值判断、前缀/后缀处理（如自动去除多余的 AND/OR 或逗号）。
*   **功能强大**：内置了 `and`、`or`、`in`、`set`、`case` 等丰富规则，满足绝大多数动态 SQL 需求。

## 基本示例

以最常见的 `AND` 规则为例，它可以智能地根据参数 (`:name`) 是否为空来决定是否拼接查询条件：

```sql
-- 使用 @{and, ...} 规则
select * from users where id > :id @{and, name = :name}
```

*   如果 `name` 不为空：生成 `... where id > ? and name = ?`
*   如果 `name` 为空：规则自动忽略，生成 `... where id > ?`

## 更多规则

dbVisitor 提供了丰富的内置规则（如 `in` 集合查询、`set` 动态更新、`case` 分支判断等），甚至支持自定义规则。

- [语句生成规则](../rules/dynamic_rule) - 包含 `@{and}` `@{or}` `@{in}` `@{set}` `@{case}` 等。
- [结果处理规则](../rules/result_rule) - 对结果集进行后续处理的规则。
- [嵌套规则用法](../rules/nested_rule) - 介绍如何通过嵌套规则实现复杂逻辑。

👉 **[点击查阅 规则系统 详细文档](../rules/about)**

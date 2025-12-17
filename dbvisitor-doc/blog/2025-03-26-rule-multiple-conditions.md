---
slug: rule_multiple_conditions
title: 使用规则处理复杂条件
description: 使用 dbVisitor AND/OR 规则定义复杂一些的条件。
authors: [ZhaoYongChun]
tags: [Rule, DynamicSQL]
language: zh-cn
---

## AND/OR 规则进阶

因此规则还可稍微复杂一点

```xml
@{and, (age = :age and sex = '1') or (name = :name and id in (:ids)) }
```

对应的 SQL 为：

```sql
(age = ? and sex = '1') or (name = ? and id in (?, ?, ?))
```

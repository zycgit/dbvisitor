---
slug: rule_multiple_conditions
title: Using Rules for Complex Conditions
description: Define more complex conditions with dbVisitor AND/OR rules.
authors: [ZhaoYongChun]
tags: [Rule, DynamicSQL]
---

## AND/OR rule basics

Rules can be slightly more complex:

```xml
@{and, (age = :age and sex = '1') or (name = :name and id in (:ids)) }
```

This becomes the SQL:

```sql
(age = ? and sex = '1') or (name = ? and id in (?, ?, ?))
```

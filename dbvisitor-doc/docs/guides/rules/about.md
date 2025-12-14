---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: 规则
description: 在 SQL 语句中可以通过使用 @{...} 写法调用规则，基于规则可以有效的大幅减少动态 SQL 拼接时的工作量和复杂度。
---

在 SQL 语句中可以通过使用 `@{...}` 写法调用规则，基于规则可以有效的大幅减少动态 SQL 拼接时的工作量和复杂度。

```text title='规则使用写法'
@{<规则名> [, <启用条件OGNL> [, 规则内容 ]])
```

:::info
请注意规则不支持嵌套使用。
:::

## 使用指引

dbVisitor 内置了很多有用的规则，这些规则根据功能效果分为如下几个类别：

- [参数处理规则](./args_rule)，此类规则主要特点为定义或者处理数据。比如：计算MD5、生成 UUID、加密/解密等。
- [结果处理规则](./result_rule)，此类规则可以对结果集中的数据做处理。比如：加密/解密、替换、遮掩等。
- [动态语句规则](./dynamic_rule)，该类规则特点是可以完成部分不太复杂的的动态 SQL。比如：入参不为空时才会正式成为 SQL 参数。
- [宏规则](./macro_rule)，该类规则会改变发往数据库的最终 SQL 语句。
- [辅助规则](./assist_rule)，该类规则不会查询和结果产生任何直接效果，只有当在某些特定场景中才会起作用。比如：存储过程相关的辅助规则。

## 基本用法

有些规则支持通过名称来调用，例如：

```sql title='生成一个 32 个字符的 UUID 字符串作为参数'
update users set str_id = @{uuid32} where id = :id
```

一个规则当只有在满足某个条件才可以被使用时，那么可以如下写法：

```sql title='当参数 queryType 为 NAME 时，追加 name = :name 条件参数'
select * from users where status = 1 @{ifand, queryType == 'NAME', name = :name}
```

对于一个规则希望忽略启用条件直接调用，那么使用如下方式：

```sql title='方式1：将启用条件部分设置为空'
select * from users where status = 1 @{ifand, , name = :name}
```

```sql title='方式2：将启用条件部分设置为 true'
select * from users where status = 1 @{ifand, true, name = :name}
```

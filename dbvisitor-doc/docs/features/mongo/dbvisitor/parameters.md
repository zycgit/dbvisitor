---
id: parameters
slug: /features/mongo/parameters
sidebar_position: 70
title: 参数与规则
---

## 通用规则 {#general-rules}

参数绑定和条件展开在命令发送到 MongoDB 之前完成，不要求使用 SQL。JdbcTemplate、方法注解和 Mapper 文件都应书写原生命令并绑定参数，用法见[参数传递](../../../guides/args/about.md)。

例如，下面的原生命令通过参数传值，不拼接用户输入：

```text
test.user_info.count({id: #{id}, name: #{name}})
```

`@{if, 条件, 命令片段}` 可按条件选择命令或片段，只有选中的分支会绑定参数；展开后必须是合法的 MongoDB 命令。

## SQL 片段规则 {#sql-fragments}

`AND`、`OR`、`SET`、`IN` 规则生成 SQL 片段，不会自动转换成 MongoDB 命令。特别是 `@{in}` 生成带圆括号、逗号分隔的参数列表，不等于原生命令的集合参数。

应按原生命令语法绑定值，需要选择片段时使用通用条件规则。这是生成语法的适用范围，不是参数绑定或规则展开能力缺失。

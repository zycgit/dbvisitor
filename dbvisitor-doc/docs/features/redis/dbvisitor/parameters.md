---
id: parameters
slug: /features/redis/parameters
sidebar_position: 70
title: 参数与规则
---

## 空值参数 {#null-values}

位置参数可以绑定非空值，但 Redis 的 String、Hash 值不能保存 SQL NULL。传入 Java null 不等于写入一个“空值”；空字符串则可以保存，并与键不存在区分。

需要清空值时，删除键或 Hash 字段，再处理 GET/HGET 的未命中结果：

```java
jdbc.executeUpdate("DEL ?", "user:1001:status");
String status = jdbc.queryForObject("GET ?", "user:1001:status", String.class); // null
```

不要用字符串 `"null"` 替代，除非业务明确约定这种表示方式。

## 参数复用 {#parameter-reuse}

PreparedStatement 支持重新绑定参数后执行。限制在于 Redis 不存储 SQL NULL，而不是不能复用参数；需要清空值时使用上述删除方式。

## 通用规则 {#general-rules}

参数绑定和条件展开在命令发送到 Redis 之前完成，不要求使用 SQL。JdbcTemplate、方法注解和 Mapper 文件都应书写原生命令并绑定参数，用法见[参数传递](../../../guides/args/about.md)。

例如，下面的原生命令通过参数传值，不拼接用户输入：

```text
ZCARD #{key}
```

`@{if, 条件, 命令片段}` 可按条件选择命令或片段，只有选中的分支会绑定参数；展开后必须是合法的 Redis 命令。

## SQL 片段规则 {#sql-fragments}

`AND`、`OR`、`SET`、`IN` 规则生成 SQL 片段，不会自动转换成 Redis 命令。特别是 `@{in}` 生成带圆括号、逗号分隔的参数列表，不等于原生命令的集合参数。

应按原生命令语法绑定值，需要选择片段时使用通用条件规则。这是生成语法的适用范围，不是参数绑定或规则展开能力缺失。

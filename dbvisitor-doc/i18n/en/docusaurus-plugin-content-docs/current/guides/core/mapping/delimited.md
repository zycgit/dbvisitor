---
id: delimited
sidebar_position: 4
hide_table_of_contents: true
title: 名称敏感性
description: 使用 dbVisitor ORM 映射数据库表，处理大小写敏感性问题。
---

某些数据库的设置对于表名的拼写具有很强的敏感性，例如：users 和 USERS 在敏感性数据库中可能表示了不同的表。
在 dbVisitor 中通过 caseInsensitive 属性或者 useDelimited 属性可以解决这些问题。

## 名称大小写敏感

如果你的数据库在建表时允许出现大小写拼写不同的多个列，那么这就意味着在查询结果时需要开启结果集大小写敏感性。否则这些列会被认为是同一个列。

```sql
select 
    AGE, // 列1，名称为大写
    age  // 列2，名称为小写
from Users;
```

```java
@Table(caseInsensitive = false)
public class Users {
    @Column("age")
    private Integer age1; // 映射到 age 列
    @Column("AGE")
    private String  age2; // 映射到 AGE 列
    ...
}
```

## 关键字处理

通过 useDelimited 属性可以在使用 [构造器 API](../../core/lambda/about) 操作数据库生成 SQL 时对名称增加限定符来解决名称关键字问题。

```java
@Table(useDelimited = true)
public class Users {
    ...
    private Integer index;// 映射为 index 列，在 MySQL 中这是关键字
    ...
}
```

dbVisitor 已经可以自动识别并处理很多数据库的关键字，一旦识别匹配到将会自动为其添加限定符。

也可以在 classpath 中添加 `/META-INF/custom.keywords` 文本文件，每行填写一个关键字的形式进行补充定义。dbVisitor 会在启动时自动识别它们。

:::tip[已经支持的数据库]
- 支持列表：IBM DB2、Derby、达梦、H2、Hive、HSQL、Impala、Informix、MySql、Oracle、PostgreSQL、SqlLite、SQL SERVER、虚谷
- 支持的关键字位于：`META-INF/db-keywords/*.keywords` 相关文件。
:::

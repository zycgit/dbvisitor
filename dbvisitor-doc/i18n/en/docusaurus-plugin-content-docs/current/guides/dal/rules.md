---
id: rules
sidebar_position: 4
title: 规则
description: 通过 dbVisitor ORM 工具的 规则可以有效的大幅减少动态 SQL 拼接时的工作量和复杂度。
---

# 规则

规则可以有效的大幅减少动态 SQL 拼接时的工作量和复杂度

- 写法为 `@{<规则名> [, <启用条件OGNL> [, 规则内容 ]])`

## 基本用法

一个规则最简单的写法只需要指明规则名即可，例如：

```xml
@{ruleName}
```

一个规则可以满足某个条件才可以被使用，那么可以如下：

```xml
@{ruleName, age > 30}
```

有些规则本身在调用的时候要求穿入一些内容。那么使用的方式如下：

```xml
@{ruleName, age > 30, xxxxxxx}
```

还可以使用下面方法省略条件参数如下：

```xml
@{ruleName,, xxxxxxx}
```

## 规则函数

| 规则                        | 描述                                                                                  |
|---------------------------|-------------------------------------------------------------------------------------|
| `@{include, expr, sqlid}` | 效果和使用 `<include refid="sqlid"/>` 标签相同                                               |
| `@{ognl, expr, xxxx}`     | 对 `xxxx` 进行 OGNL 求值，并把执行结果加入到 SQL 参数中。类型读写请参考 **[类型映射](../types/type-handlers.md)** |
| `@{md5, expr, xxxx}`      | 对 `xxxx` 进行 OGNL 求值，值结果用 MD5 进行编码然后加入到 SQL 参数中                                      |
| `@{uuid32}`               | 产生一个 32 字符长度的 `UUID`，并加入到 SQL 参数中                                                   |
| `@{uuid36}`               | 产生一个 36 字符长度的 `UUID`，并加入到 SQL 参数中                                                   |
| `@{and, queryExpr}`       | `与`规则，详细看下面                                                                         |
| `@{or, queryExpr}`        | `或`规则，详细看下面                                                                         |
| `@{arg, expr, xxxx}`      | `xxxx` 的写法与 `#{...}` 中的内容相同。这是一个内置规则，`#{...}` 就是它的简化形式                              |
| `@{text, expr, xxxx}`     | 原样输出 `xxxx`，这是一个内置规则 dbVisitor 内部的一些机制会用到它。                                         |

## 与/或 规则

如下语句，当参数不为空时候才拼接 sql

```xml
<select id="queryUser">
    select * from `test_user`
    where 1 = 1
    <if test="age != null">
        and age = #{age}
    </if>
</select>
```

使用 `and` 规则简化

```xml
<select id="queryUser">
    select * from `test_user`
    @{and, age = :age}
</select>
```

例如如下 `foreach` 操作：

```xml
<select id="queryUser">
    select * from `test_user`
    where
    id in <foreach item="item" index="index" collection="list"
             open="(" separator="," close=")">
        #{item}
    </foreach>
</select>
```

使用 `and` 规则简化

```xml
<select id="queryUser">
    select * from `test_user`
    @{and, id in (:list)}
</select>
```

如果多个简单条件，规则将会极大的减少 Mapper 的工作量。

```xml
<select id="queryByNameAndAge">
    select * from `test_user`
    @{and, age = :age}
    @{and, name = :name}
    @{and, id in (:ids)}
</select>
```

## AND/OR 规则进阶

**原理**

规则的原理是利用 `net.hasor.dbvisitor.jdbc.core.ParsedSql` 工具类，将规则的条件表达式当作 SQL 片段进行解析。
这部分和 `JdbcTemplate` Map 传参是等价的。

当 `ParsedSql` 的 `buildValues` 方法返回的参数全部为 `null` 时规则就会失效，反之则为有效的 SQL 片段。

因此规则还可稍微复杂一点

```xml
@{and, (age = :age and sex = '1') or (name = :name and id in (:ids)) }
```

对应的 SQL 为：

```sql
(age = ? and sex = '1') or (name = ? and id in (?, ?, ?))
```

## 自定义规则

实现一个自定义的规则函数十分简单只需要实现 `net.hasor.dbvisitor.dal.dynamic.rule.SqlBuildRule` 接口并注册到系统中就可以了。

```java
public class MyRule implements SqlBuildRule {
    public void executeRule(Map<String, Object> data, DynamicContext context, 
                            SqlBuilder sqlBuilder, String activeExpr, 
                            String ruleValue) {
        ...
        SqlArg arg = new SqlArg(expr, value, sqlMode, jdbcType, javaType, typeHandler);
        sqlBuilder.appendSql("?", arg);
    }
}
```

```java title='注册规则'
RuleRegistry.DEFAULT.register("myrule", new MyRule());
```

最后就可以愉快的使用它了。

```xml
<select id="queryByNameAndAge">
    select * from `test_user`
    @{myrule, true, xxxx}
</select>
```
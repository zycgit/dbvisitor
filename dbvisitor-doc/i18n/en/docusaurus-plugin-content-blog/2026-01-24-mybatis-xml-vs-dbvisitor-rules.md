---
slug: mybatis-xml-vs-dbvisitor-rules
title: Simplify SQL Conditions with Dynamic Rules
authors: [ZhaoYongChun]
tags: [dbVisitor, MyBatis, ORM]
---

Dynamic SQL can be expressed with XML tags or written directly in SQL using dbVisitor rules. Both approaches require clear semantics for condition activation, parameter binding, and empty collections. This article covers common rule patterns and explains which forms are not interchangeable.

<!--truncate-->

## Optional Query Conditions

XML tags usually check parameters before appending conditions. The following is a statement fragment in a dbVisitor Mapper file:

```xml
<select id="queryUsers" resultType="com.example.User">
    SELECT * FROM tb_user
    <where>
        <if test="name != null">AND name = #{name}</if>
        <if test="age != null">AND age = #{age}</if>
    </where>
</select>
```

Using rules:

```sql
SELECT * FROM tb_user
    @{and, name = :name}
    @{and, age = :age}
```

With name="Tom" and age=null:

```sql
SELECT * FROM tb_user WHERE name = ?
```

A single-parameter `and` or `or` rule omits its content when the parameter is null; an empty string is still a valid value. A rule with multiple parameters is omitted only when all are null; individual comparisons are not removed.

Rules add WHERE, AND, or OR based on SQL already generated. Do not put a leading AND/OR inside the rule body. Add your own parentheses for complex OR groups.

## Explicit Condition Switches

`ifand` and `ifor` use OGNL expressions to decide whether to emit a condition:

```sql
SELECT * FROM tb_user
    @{ifand, !showAll, is_delete = 0}
```

The application must explicitly assign showAll. Conditions protecting authorization, tenant isolation, or deletion scope must not be disabled by untrusted parameters or silently omitted when parameters are missing.

## IN Lists

The `in` rule expands a collection into bound parameters. Use this optional condition only when business rules explicitly define an empty list as "do not filter":

```sql
SELECT * FROM tb_user
    @{ifand, idList != null && !idList.isEmpty(), id IN @{in, :idList}}
```

If an empty list means "no matching objects", return an empty result in application code or generate an always-false condition. Reject missing target collections for writes. Omitting an IN condition does not mean matching no rows.

## UPDATE and NULL

The `set` rule adds SET and column separators; it **does not skip NULL values**:

```sql
UPDATE tb_user
    @{set, name = :name}
    @{set, age = :age}
WHERE id = :id
```

When name=null, SQL NULL is written. This differs from selective updates using `<if test="name != null">`.

To update only non-null fields, use explicit conditions. The application must ensure at least one column participates and validate id:

```sql
UPDATE tb_user SET
    @{if, name != null, @{set, name = :name}}
    @{if, age != null, @{set, age = :age}}
WHERE id = :id
```

Do not append commas manually after rules. Put fixed columns before dynamic ones:

```sql
UPDATE tb_user SET fixed_col = 123
    @{set, name = :name}
    @{set, email = :email}
WHERE id = :id
```

## Branch Selection

`case` selects a branch by condition. For example, query by title first, then content, then a fixed fallback:

```sql
SELECT * FROM t_blog
@{ifand, true,
    @{case, ,
        @{when, title != null, title = #{title}},
        @{when, content != null, content = #{content}},
        @{else, owner = 'defaultOwner'}
    }
}
```

This uses `ifand, true` because the fallback contains no bound parameters. Since `and` checks parameter nullness, it cannot guarantee that this fixed branch is retained.

For authorization queries, determine the role and access scope on the server before generating conditions. Do not rely solely on rules that omit conditions when parameters are missing.

## Use Across APIs

Rules work in JdbcTemplate:

```java
Map<String, Object> args = new HashMap<>();
args.put("name", "Tom");
jdbcTemplate.queryForList("SELECT * FROM users @{and, name = :name}", args);
```

They also work in Mapper annotations:

```java
@Query("SELECT * FROM users @{and, name = :name}")
List<User> queryUsers(@Param("name") String name);
```

Or Mapper XML:

```xml
<select id="queryUsers" resultType="com.example.User">
    SELECT * FROM users
    @{and, name = :name}
    @{and, age = :age}
</select>
```

These are independent examples. Mapper files need the matching namespace and interface binding. Bind data values as parameters; table names, column names, and SQL text must not come from unvalidated input.

## Choosing an Approach

- Use Mapper XML when the team prefers tags or complex statements need separate files.
- Rules reduce tag nesting for optional conditions, IN expansion, and a few branches.
- Use dialect-supported Lambda builders for type-safe general CRUD.
- Preserve native syntax for database-specific queries, then choose suitable parameter binding and mapping.

See [Dynamic Rules](../docs/guides/rules/dynamic_rule) for syntax and [Nested Rules](../docs/guides/rules/nested_rule) for composition.

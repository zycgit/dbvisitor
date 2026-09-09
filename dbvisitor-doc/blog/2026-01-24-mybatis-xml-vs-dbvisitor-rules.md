---
slug: mybatis-xml-vs-dbvisitor-rules
title: 使用动态规则简化 SQL 条件
authors: [ZhaoYongChun]
tags: [dbVisitor, MyBatis, ORM]
---

动态 SQL 可以通过 XML 标签表达，也可以用 dbVisitor 的规则直接写在 SQL 中。两种方式都需要明确条件启用、参数绑定和空集合的语义。本文介绍如何用规则处理常见场景，以及哪些写法不能直接互换。

<!--truncate-->

## 可选查询条件

XML 标签通常先判断参数，再拼接条件。下面是 dbVisitor Mapper 文件中的语句片段：

```xml
<select id="queryUsers" resultType="com.example.User">
    SELECT * FROM tb_user
    <where>
        <if test="name != null">AND name = #{name}</if>
        <if test="age != null">AND age = #{age}</if>
    </where>
</select>
```

规则写法：

```sql
SELECT * FROM tb_user
    @{and, name = :name}
    @{and, age = :age}
```

只传入 name="Tom"、age=null 时，生成：

```sql
SELECT * FROM tb_user WHERE name = ?
```

单参数 `and`/ `or` 规则在参数为 null 时省略内容；空字符串仍是有效值。如果一个规则包含多个参数，则只有全部为空时才省略，不会逐个删除其中的比较表达式。

规则会依据已经生成的 SQL 补上 WHERE、AND 或 OR。规则体中不要再写前置 AND/OR，复杂 OR 分组应自行加括号。

## 显式条件开关

`ifand`、`ifor` 使用 OGNL 表达式决定是否生成条件：

```sql
SELECT * FROM tb_user
    @{ifand, !showAll, is_delete = 0}
```

这里的 showAll 应由应用明确赋值。用于授权、租户隔离或删除范围的条件不能由不可信参数关闭，也不应在缺失时被静默省略。

## IN 列表

`in` 规则把集合展开为多个绑定参数。只有在业务明确允许“空列表表示不筛选”时，才使用下面的可选条件：

```sql
SELECT * FROM tb_user
    @{ifand, idList != null && !idList.isEmpty(), id IN @{in, :idList}}
```

如果空列表表示“没有匹配对象”，应在应用层直接返回空结果，或生成永假条件；对于写操作，应拒绝缺失的目标集合。不要把省略 IN 条件误认为不命中任何行。

## UPDATE 与 NULL

`set` 规则补充 SET 和列间逗号，**不会跳过 NULL 值**：

```sql
UPDATE tb_user
    @{set, name = :name}
    @{set, age = :age}
WHERE id = :id
```

name=null 时，会写入 SQL NULL。这不同于 `<if test="name != null">` 的选择性更新。

若只更新非空字段，可以使用显式条件；应用必须保证至少有一列参与更新，并验证 id：

```sql
UPDATE tb_user SET
    @{if, name != null, @{set, name = :name}}
    @{if, age != null, @{set, age = :age}}
WHERE id = :id
```

不要在规则后手写逗号。混合固定列时，将固定列放在前面：

```sql
UPDATE tb_user SET fixed_col = 123
    @{set, name = :name}
    @{set, email = :email}
WHERE id = :id
```

## 分支选择

`case` 可以按条件选择一个分支。例如优先按 title 查询，否则按 content 查询，最后使用固定条件：

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

这里使用 `ifand, true`，因为兜底分支是没有绑定参数的固定条件；`and` 会依赖参数判空，不适合用它保证该固定分支一定生效。

权限查询应先由服务端确认角色和访问范围，再生成相应条件；不要用“参数缺失就省略”的规则作为唯一的权限保护。

## 在不同 API 中使用

规则可以用于 JdbcTemplate：

```java
Map<String, Object> args = new HashMap<>();
args.put("name", "Tom");
jdbcTemplate.queryForList("SELECT * FROM users @{and, name = :name}", args);
```

也可以用于 Mapper 注解：

```java
@Query("SELECT * FROM users @{and, name = :name}")
List<User> queryUsers(@Param("name") String name);
```

或 Mapper XML：

```xml
<select id="queryUsers" resultType="com.example.User">
    SELECT * FROM users
    @{and, name = :name}
    @{and, age = :age}
</select>
```

以上代码分别是独立示例；Mapper 文件需要对应 namespace 和接口绑定。数据值使用绑定参数，表名、列名及 SQL 文本不应来自未经校验的输入。

## 如何选择

- 团队更熟悉 XML 标签或复杂语句需要独立文件管理：使用 Mapper XML。
- 简单可选条件、IN 展开、少量分支：规则可减少标签层次。
- 类型安全的通用 CRUD：使用受方言支持的 Lambda 构造器。
- 数据库专有查询：保留其原生语法，再选择适合的参数与映射方式。

完整规则语法见[动态规则](../docs/guides/rules/dynamic_rule)；组合行为见[嵌套规则](../docs/guides/rules/nested_rule)。

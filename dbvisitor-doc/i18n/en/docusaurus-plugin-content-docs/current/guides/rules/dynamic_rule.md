---
id: dynamic_rule
sidebar_position: 3
hide_table_of_contents: true
title: 7.3 SQL 增强规则
description: 该类规则特点是可以完成部分不太复杂的的动态 SQL，比如：入参不为空时才会正式成为 SQL 参数。下面是本类中所含有的具体规则。
---

# SQL 增强规则

该类规则特点是可以完成部分不太复杂的的动态 SQL，比如：入参不为空时才会正式成为 SQL 参数。下面是本类中所含有的具体规则：

| 规则                                                    | 描述                                                                       |
|-------------------------------------------------------|--------------------------------------------------------------------------|
| [`@{and, queryExpr}`](./dynamic_rule#and)             | AND 规则，当 `queryExpr` 条件 SQL 片段中生成了不为空的条件参数时，条件表达式会被加入到最终 SQL 语句中。        |
| [`@{ifand, testExpr, queryExpr}`](./dynamic_rule#and) | 当 `testExpr` 条件表达式为真时 `queryExpr` 才会以 AND 规则方式处理 `queryExpr` 条件。         |
| [`@{or, queryExpr}`](./dynamic_rule#or)               | OR 规则，当 `queryExpr` 条件 SQL 片段中生成了不为空的条件参数时，条件表达式会被加入到最终 SQL 语句中。         |
| [`@{ifor, testExpr, queryExpr}`](./dynamic_rule#or)   | 当 `testExpr` 条件表达式为真时 `queryExpr` 才会以 OR 规则方式处理 `queryExpr` 条件。          |
| [`@{in, queryExpr}`](./dynamic_rule#in)               | IN 规则，在规则中 `queryExpr` 表达式中只能出现一个参数，当参数不为空时候会自动为其转换为 `(?,?,?,)` 形式的查询条件。 |
| [`@{ifin, testExpr, queryExpr}`](./dynamic_rule#in)   | 增强的 IN 规则，当 `testExpr` 条件表达式为真时才会以 IN 规则机制处理 `queryExpr` 条件语句。           |
| [`@{set, queryExpr}`](./dynamic_rule#set)             | SET 规则，可以简化在 `update` 语句中 `set` 字句部分里当有数据时候才进行更新的实现复杂问题。                 |
| [`@{ifset, testExpr, queryExpr}`](./dynamic_rule#set) | 增强的 SET 规则，当 `testExpr` 条件表达式为真时才会以 SET 规则机制处理 `queryExpr` 条件语句。         |

## AND、IFAND 规则 {#and}

借助 AND 规则可以实现，条件不为空时才将条件语句追加到 SQL 中，而 IFAND 增加了一个条件表达式作为判断条件。

例如，当一个参数不为空时才会被当作 SQL 条件的场景在使用规则前后不同用法下的差异：

```sql title='使用规则传参'
select * from users 
where status = :status      -- 条件 status 
      @{and, uid = :userId} -- 条件 uid
```

```xml title='等效代码：XML Mapper'
<select id="queryUser">
    select * from users
    where status = #{status}
    <if test="userId != null">
        and uid = #{userId}
    </if>
</select>

<!-- 即便是 Mapper 也可以使用规则哦 -->
<select id="queryUser">
  select * from users
  where status = #{status} @{and, uid = :userId}
</select>
```

```java title='等效代码：Java Code'
// 准备 SQL 和参数
String querySQL = "select * from users where status = ?";
Object queryArg = null;
if (userId != null) {
    querySQL = querySQL + " and uid = ?";
    queryArg = new Object[]{ 1, userId};
} else {
    queryArg = new Object[]{ 1};
}

// 执行查询
jdbc.queryForList(querySQL, queryArg);
```

使用 IFAND 规则允许通过一个表达式来判断是否使用这个规则，相比 AND 规则具有更高的灵活性。

```sql title='编程方式：规则中设置启用条件'
select * from users 
where status = :arg0 
      @{ifand, arg1 != null, uid = :arg1}
```

- 通过 `arg1 != null` 激活条件决定是否使用规则。

```xml title='XML 方式：规则中设置启用条件'
<select id="queryUser">
    select * from users
    where status = #{status} @{ifand, userId != null, uid = :userId}
</select>
```

- 通过 `userId != null` 激活条件决定是否使用规则。

:::info
`@{and}`、`@{ifand}` 规则可以自适应以下 SQL 语句场景。

```sql
select * from users @{and, uid = :userId} -- 自动补全 where 字句
select * from users where @{and, uid = :userId} -- 如果条件为空 where 不会被消除
select * from users where status = :status @{and, uid = :userId} -- 自动补全 and
select * from users where status = :status and @{and, uid = :userId} -- 如果条件为空 and 不会被消除
select * from users where status = :status or @{and, uid = :userId} -- 会自动降级为 or 规则
```
:::

## OR、IFOR 规则 {#or}

- OR、IFOR 和 AND、IFAND 规则非常相似，不同的是条件的前后连接使用 `or` 关键字作为连接。具体用法可以参考 AND 规则用法。

## IN、IFIN 规则 {#in}

利用 IN 规则可以简化 SQL 中 `in` 语句拼接的需要，而 IFIN 增加了一个条件表达式作为判断条件。

例如，生成带有多个参数的 in 语句在不同方式下使用规则的前后对比：

```sql title='使用规则生成 in 语句'
select * from users
where status = :arg0
      @{in,and id in :arg1} -- 生成 id in (?,?,?)
```

```xml title='等效代码：XML Mapper'
<select id="queryUser">
    select * from users
    where status = #{status}
    <if test="ids != null && ids.length > 0">
        and id in 
        <foreach collection="ids" item="item" index="index" open="(" separator="," close=")">
            #{item}
        </foreach>
    </if>
</select>

<!-- 即便是 Mapper 也可以使用规则 -->
<select id="queryUser">
    select * from users 
    where status = #{status} @{in,and id in :ids}
</select>
```

```java title='等效代码：Java Code'
// 准备 SQL 和参数
StringBuilder querySQL = new StringBuilder("select * from user_info where status =?");
List<Object> queryArgs = new ArrayList<>();
queryArgs.add("1");

Object[] ids = new Object[] { 1, 2, 3 };
if (ids != null && ids.length > 0) {
    querySQL.append("and id in (");
    for (int i = 0; i < ids.length; i++) {
        if (i == 0) {
            querySQL.append("?");
        } else {
            querySQL.append(", ?");
        }
        queryArgs.add(ids[i]);
    }
    querySQL.append(")");
}

// 执行查询
jdbc.queryForList(querySQL.toString(), queryArgs.toArray());
```

IFIN 规则允许通过一个表达式来判断是否使用这个规则，相比 IN 规则具有更高的灵活性。

```sql title='在编程方式中使用 IFIN 规则'
select * from users
where status = :arg0
      @{ifin, arg1.size() > 2 , and id in :arg1}
```

- 只有当条件参数数量大于 2 时才激活 IN 规则添加 in 条件。

```xml title='在 XML 方式中使用 IFIN 规则'
<select id="queryUser">
    select * from users 
    where status = #{status} @{in, ids.size() > 2 ,and id in :ids}
</select>
```

- 只有当条件参数数量大于 2 时才激活 IN 规则添加 in 条件。

:::info
IN 规则限制是：
- 一次只能处理一个 in 条件。
- IN 规则不支持自动补全 and、or、where 等可能缺失的查询关键字，如：
  - `select * from users @{in,id in :ids}` 将会产生错误的 SQL。
:::

:::info
IN 规则可以处理的参数类型有：
- 单个对象
- 通过 SqlArg 包装的集合或数组参数
- 数组
- 任何派生自 Iterable 接口的子类，包括：Collection、List、Set 类型。
:::

## SET、IFSET 规则 {#set}

利用 SET 规则可以简化在 `update` 语句中 `set` 字句部分里更新字段如果为空时不参与更新的实现逻辑，而 IFSET 则增加了一个条件表达式作为判断条件。

下面是在使用不用方式中使用规则的前后对比：

```sql title='使用规则生成：SET 字句'
update users 
set update_time = now()    -- 更新字段 update_time
    @{set, status = :arg0} -- 更新字段 status，当 arg0 不为空时有效（规则会自动处理逗号问题）
where uid = :arg1
```

```xml title='等效代码：XML Mapper'
<update id="updateUser">
    update users 
    set update_time = now()
        <if test="status != null">, status = #{status}</if>
    where
        uid = #{uid}
</update>

<!-- 即便是 Mapper 也可以使用规则哦 -->
<update id="updateUser">
    update users 
    set update_time = now() 
        @{set, status = :status}
    where
        uid = #{uid}
</update>
```

```java title='等效代码：Java Code'
// 准备 SQL 和参数
StringBuilder querySQL = new StringBuilder("update users set update_time = now()");
List<Object> queryArgs = new ArrayList<>();

if (status != null) {
    querySQL.append(", status = ?");
    queryArgs.add(status);
}
querySQL.append(" where uid =?");
queryArgs.add(uid);

// 执行查询
jdbc.executeUpdate(querySQL.toString(), queryArg.toArray());
```

IFSET 规则允许通过一个表达式来判断是否使用这个规则，相比 SET 规则具有更高的灵活性。

```sql title='在编程方式中使用 IFIN 规则'
update users
set update_time = now() 
    @{ifset, arg0 != 2 ,status = :arg0}
where uid = :arg1
```

- 只有当条件参数 `status` 不等于 2 时才会被当作更新项目。

```xml title='在 XML 方式中使用 IFIN 规则'
<update id="queryUser">
    update users
    set update_time = now()
        @{ifset, status != 2 ,status = :status}
    where
        uid = #{uid}
</update>
```

- 只有当条件参数 `status` 不等于 2 时才会被当作更新项目。

---
sidebar_position: 2
title: 动态 SQL
description: dbVisitor ORM 工具提供提供基于 XML 动态 SQL 配置，并且采用了大家熟悉的 MyBatis 风格。
---

# 动态 SQL

dbVisitor 提供基于XML 动态 SQL 配置，并且采用了大家熟悉的 MyBatis 风格。

## if

`if` 是最常用的一个用法，用于判断某个条件满足之后拼接对应的 SQL。

```xml
<select id="queryUser">
    select * from `test_user`
    where state = 'ACTIVE'
    <if test="age != null">
        and age = #{age}
    </if>
</select>
```

`if` 标签含有以下属性

| 属性名    | 描述                                                     |
|--------|--------------------------------------------------------|
| `test` | 必选，一个 ognl 表达式，表达式值是 boolean 表示判断是否成立，若判断成立则执行 SQL 生成。 |

## choose (when, otherwise)

同 Java 语言中的 switch 语句一样，`choose` 可以设置一组标签。
当有条件被满足之后会生成对应 `when` 中的 SQL，如果有多个条件同时匹配，只有第一个会生效。若没有匹配任何 `when` 那么 `otherwise` 会生效。

```xml
<select id="queryUser">
    select * from `test_user`
    where state = 'ACTIVE'
    <choose>
        <when test="title != null">and title = #{title}</when>
        <when test="content != null">and content = #{content}</when>
        <otherwise>and owner = "owner1"</otherwise>
    </choose>
</select>
```

`choose` 标签和 `otherwise` 标签不含有任何属性。`when` 标签与 `if `标签相同

| 属性名    | 描述                                                     |
|--------|--------------------------------------------------------|
| `test` | 必选，一个 ognl 表达式，表达式值是 boolean 表示判断是否成立，若判断成立则执行 SQL 生成。 |

## trim (where, set)

`trim`、`where`、`set` 三个标签可以帮助我们在生成特定 SQL 语句时不会造成纰漏。在介绍它们三个之前看一下这个例子：

```xml
<select id="queryUser">
    select * from `test_user`
    where
    <if test="age != null">
        and age = #{age}
    </if>
</select>
```

当 `age` 属性为空时会生成如下 SQL：

```sql
select * from `test_user`
where
```

这是一个无效 SQL 若想避免此类问题可以选择 `where` 标签。当 `if` 条件成立 `where` 标签中会输出有效内容，一旦出现有效内容 `where` 标签会自动增加 where 语句。

```xml
<select id="queryUser">
    select * from `test_user`
    <where>
        <if test="age != null">
            age = #{age}
        </if>
    </where>
</select>
```

生成的语句将会是下列两种：

```sql
select * from `test_user`;
select * from `test_user` where and age = ?
```

例如下面这个例子 `where` 中多个条件，如果第一个条件匹配失败有可能出现 and 作为开头的情况。
而 `where` 标签将会自动检测如果是 `and` 或 `or` 开头的则自动去掉它们

```xml
<select id="queryUser">
    select * from `test_user`
    <where>
        <if test="age != null">
            age = #{age}
        </if>
        <if test="name != null">
            and name = #{name}
        </if>
    </where>
</select>
```

如果 `where` 并未按照与其的那样生成 SQL 可以使用 `trim` 标签来自定义它。下列 trim 标签用法与 where 是等价的

```xml
<trim prefix="where" prefixOverrides="and | or">
    ...
</trim>
```

对于动态更新列语句可以选择 `set` 标签，例如：

```xml
<update id="queryUser">
    update `test_user`
    <set>
        <if test="age != null">age=#{age},</if>
        <if test="name != null">name=#{name},</if>
    </set>
    where id=#{id}
</update>
```

以下是使用 `trim` 标签和 `set` 标签等价的方式

```xml
<trim prefix="set" suffixOverrides=",">
    ...
</trim>
```

`where` 标签和 `set` 标签都不含有任何属性。`trim` 标签的可选属性如下：

| 属性名               | 描述                                                     |
|-------------------|--------------------------------------------------------|
| `prefix`          | 可选，trim 在生成 SQL 时候的前缀。                                 |
| `suffix`          | 可选，trim 在生成 SQL 时候的尾缀。                                 |
| `prefixOverrides` | 可选，当生成的 SQL 前几个字符匹配这个属性时会被自动裁剪掉，若要配置多个匹配项。需要用 竖线 来分割。  |
| `suffixOverrides` | 可选，当生成的 SQL 后面几个字符匹配这个属性时会被自动裁剪掉，若要配置多个匹配项。需要用 竖线 来分割。 |

## foreach

`foreach` 标签是常用的一个标签，通常是用来构建 `in` 语句或者 `values` 的值列表。

```xml
<select id="queryByIds">
  select * from `test_user`
  where id in
  <foreach item="item" index="index" collection="list"
           open="(" separator="," close=")">
    #{item}
  </foreach>
</select>
```

`foreach` 标签的可选属性如下：

| 属性名          | 描述                                             |
|--------------|------------------------------------------------|
| `collection` | 必选，要遍历的数据，可以是数组、集合。                            |
| `item`       | 必选，在遍历数据过程中，用于标识当前元素的变量名。在标签中可以通过使用这个变量名访问到元素。 |
| `open`       | 可选，在开始遍历时候的输出到动态 SQL 的前缀部分。                    |
| `close`      | 可选，在开始遍历时候的输出到动态 SQL 的尾缀部分。                    |
| `separator`  | 可选，在遍历的过程中，用于区分每个元素的间隔字符。                      |

## bind

`bind` 标签允许通过 `ognl` 表达式创建一个变量并将其绑定到上下文。例如:

```xml
<select id="queryByLike">
    <bind name="pattern" value="'%' + _parameter.getTitle() + '%'" />
    select * from `test_user`
    where title like #{pattern}
</select>
```

`bind` 标签的可选属性如下：

| 属性名 | 描述 |
| ----- | --- |
| `name` | 必选，要遍历的数据，可以是数组、集合。 |
| `value` | 必选，在遍历数据过程中，用于标识当前元素的变量名。在标签中可以通过使用这个变量名访问到元素。 |

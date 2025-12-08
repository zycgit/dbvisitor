---
id: named
sidebar_position: 3
hide_table_of_contents: true
title: 6.2 名称参数
description: 在 SQL 语句使用 :name、&name 或 #{...} 写法，可以将 SQL 中的参数进行名称化。
---

# 名称参数

在 SQL 语句使用 `:name`、`&name` 或 `#{...}` 写法，可以将 SQL 中的参数进行名称化。

```text
select * from users where id > :id   and name = :name
select * from users where id > &id   and name = &name
select * from users where id > #{id} and name = #{name}
```

## 基本用法

```java title='例1：使用 Map 作为参数容器'
Map<String, Object> args = CollectionUtils.asMap(
        "id", 2,
        "Dave", true
);
jdbcTemplate.queryForList("select * from users where id > :id and name = :name", args);
```

```java title='例2：使用 Bean 对象作为参数容器'
public class User {
  private int     id;
  private String  name;

  public User(int id, String name) {
    this.id = id;
    this.name = name;
  }
  // getters and setters omitted
}

User args = new User(2, "Dave");
jdbcTemplate.queryForList("select * from users where id > :id and name = :name", args);
```

## OGNL 取值 {#ognl}

通过 OGNL 表达式可以获取对象的属性和调用对象的方法，进而实现更加复杂的参数提取。

```json title='例：提取嵌套结构中参数'
{
  "p": {
    "name": "Dave",
    "cfg_id": {
      "array": [
        {"age": 10},
        {"age": 40}  <<< 使用该属性作为参数
      ]
    }
  }
}
```

使用 OGNL 表达式 `p.cfg_id.array[1].age` 对上面结构的数据进行参数提取。

```text title='写法 1'
select * from user_table where age > :p.cfg_id.array[1].age order by id
```

```text title='写法 2'
select * from user_table where age > #{p.cfg_id.array[1].age} order by id
```

## 参数选项

```text title='例1：为参数指定 TypeHandler'
select * from users where
  id   > #{id,typeHandler=net.hasor.dbvisitor.types.handler.number.LongTypeHandler}
  and
  name = #{name,typeHandler=net.hasor.dbvisitor.types.handler.string.StringTypeHandler}
```

```text title='例2：接收存储过程的 OUT 参数'
{call proc_bigint(#{out,mode=out,jdbcType=bigint})}
```

:::info[拓展信息]
- 使用 `:name` 或 `&name` 写法不支持参数选项。
- 有关更多参数选项的信息请到 **[参数选项](./options)** 查看。
:::

## 忽略规则

在 SQL 中如果冒号 `:` 后面紧跟空白字符（如空格、换行、制表符等），dbVisitor 会忽略该冒号的参数解析，将其视为普通字符。
这一特性使得在 SQL 中可以直接书写 JSON 或类似 MongoDB 的查询语句，而无需对冒号进行转义。

```sql
-- 冒号后面有空格，不会被识别为参数
select * from table where config = '{ "key": "value" }'
```

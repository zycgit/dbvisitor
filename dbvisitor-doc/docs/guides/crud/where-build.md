---
id: where_builder
sidebar_position: 2
title: 条件构造器
description: 使用 dbVisitor ORM 查询工具的条件构造器查询数据库。
---

# 条件构造器

## 前言

使用 Java 开发程序最痛苦的事情之一就是要处理动态生成 SQL 的问题，虽然通过 Mapper 文件可以规划生成的 SQL语句。
但在遇到复杂情况时代码方式依然是最佳选择。

:::tip
- 条件构造器可以用于 `query`、`update`、`delete` 三类操作中，下面以 `query` 为例展示不同方法对应的的 sql 语句。
- 下面代码样例采用 `TestUser` 实体为参数，这就需要预先定义映射类。实际使用中还可以采用 `catalog/schema/table` 三元组的形式免于定义实体类。
:::

## 条件

### 等于 (==)

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.eq(TestUser::getAge, 32)
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE age = ?
```

### 不等于 (!=)

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.ne(TestUser::getAge, 32)
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE age <> ?
```

### 大于 (>)

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.gt(TestUser::getAge, 32)
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE age > ?
```

### 大于等于 (>=)

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.ge(TestUser::getAge, 32)
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE age >= ?
```

### 小于 (&lt;)

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.lt(TestUser::getAge, 32)
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE age < ?
```

### 小于等于 (&lt;=)

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.le(TestUser::getAge, 32)
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE age <= ?
```

### 模糊匹配 (like)

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.like(TestUser::getAge, "001")
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE name LIKE CONCAT('%', ? ,'%')
```

### 左半边匹配

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.likeLeft(TestUser::getAge, "001")
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE name LIKE CONCAT('%', ?)
```

### 右半边匹配

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.likeRight(TestUser::getAge, "001")
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE name LIKE CONCAT(?, '%')
```

### 排除模糊匹配 (not like)

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.notLike(TestUser::getAge, "001")
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE name NOT LIKE CONCAT('%', ? ,'%')
```

### 排除左半边匹配

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.notLikeLeft(TestUser::getAge, "001")
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE name NOT LIKE CONCAT('%', ?)
```

### 排除右半边匹配

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.notLikeRight(TestUser::getAge, "001")
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE name NOT LIKE CONCAT(?, '%')
```

### 为空 (is null)

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.isNull(TestUser::getAge)
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE age IS NULL
```

### 不为空 (is not null)

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.isNotNull(TestUser::getAge)
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE age IS NOT NULL
```

### 包含 (in)

```java {3}
List<Integer> argsIn = Arrays.asList(22, 32);
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query10.in(TestUser::getAge, argsIn)
                               .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE age IN ( ? , ? )
```

### 排除 (not in)

```java {3}
List<Integer> argsNotIn = Arrays.asList(22, 32);
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query10.notIn(TestUser::getAge, argsNotIn)
                               .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE age NOT IN ( ? , ? )
```

### 范围内 (between)

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.between(TestUser::getAge, 20, 30)
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE age BETWEEN ? AND ?
```

### 范围外 (not between)

```java {2}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.notBetween(TestUser::getAge, 20, 30)
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE age NOT BETWEEN ? AND ?
```

## 关系式

当拥有多个条件进行查询时，就会用到关系式。

### 与关系

`与` 是默认关系，因此当多个条件之间都是 `与` 关系时，可以不指明关系式。例如：

```java {2-3}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.eq(TestUser::getName, "123")
                             .eq(TestUser::getAge, 12)
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE name = ? AND age = ?
```

也可以展开写成：

```java {2-3}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.and().eq(TestUser::getName, "123")
                             .and().eq(TestUser::getAge, 12)
                             .queryForList();
```

### 或关系

表示多个条件之间是或关系使用如下方式：

```java {3}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.eq(TestUser::getName, "123")
                             .or()
                             .eq(TestUser::getAge, 12)
                             .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE name = ? OR age = ?
```

### 条件嵌套

在一些较为复杂的条件中可以使用嵌套关系，例如：

```java {3,5}
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
List<TestUser> result = query.and(qc -> {
        qc.like(TestUser::getName, "123").eq(TestUser::getAge, 12);
    }).or(qc -> {
        qc.eq(TestUser::getId, 1);
    }).queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM test_user WHERE 
    ( name LIKE CONCAT('%', ? ,'%') AND age = ? )
    OR
    ( id = ? )
```

:::tip
条件嵌套可以多层
:::
